package dev.tenz;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/** /tenz dump: writes the real 26.3 sky / player-model class layouts to tenz-api-dump.txt so the mod can be fitted exactly. */
public final class ApiDump {
    private static boolean wanted(String n) {
        if (!n.endsWith(".class")) return false;
        String s = n.substring(n.lastIndexOf('/') + 1);
        if (n.startsWith("net/fabricmc/fabric/api/client/rendering/")) return true;
        if (!n.startsWith("net/minecraft/client/")) return false;
        return s.contains("Sky") || s.equals("PlayerModel.class") || s.equals("HumanoidModel.class")
            || s.equals("AvatarRenderer.class") || s.equals("AvatarRenderState.class")
            || s.equals("LevelRenderer.class") || s.equals("LevelRenderState.class") || s.equals("ModelPart.class")
            || s.equals("ClientLevel.class") || s.equals("MultiPlayerGameMode.class")
            || s.equals("BlockRenderDispatcher.class") || s.equals("ModelBlockRenderer.class")
            || s.equals("SectionRenderDispatcher.class") || s.equals("SectionCompiler.class")
            || s.equals("FogRenderer.class") || s.equals("Camera.class") || s.equals("LevelTargetBundle.class")
            || s.equals("CameraRenderState.class") || s.equals("BlockModelPart.class");
    }

    private static void scan(Class<?> anchor, StringBuilder sb, List<String> names) throws Exception {
        var src = anchor.getProtectionDomain().getCodeSource();
        if (src == null) return;
        Path jar = Path.of(src.getLocation().toURI());
        sb.append("source: ").append(jar).append('\n');
        if (Files.isRegularFile(jar)) {
            try (ZipFile z = new ZipFile(jar.toFile())) {
                z.stream().map(e -> e.getName()).filter(ApiDump::wanted).sorted().forEach(names::add);
            }
        }
    }

    public static Path run() throws Exception {
        StringBuilder sb = new StringBuilder("Minecraft + Fabric rendering API scan\n");
        List<String> names = new ArrayList<>();
        scan(Minecraft.class, sb, names);
        scan(net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.class, sb, names);
        sb.append("classes found: ").append(names.size()).append("\n\n");
        for (String n : names) {
            String cn = n.substring(0, n.length() - 6).replace('/', '.');
            try {
                Class<?> c = Class.forName(cn, false, ApiDump.class.getClassLoader());
                sb.append("== ").append(cn).append(" extends ").append(c.getSuperclass() == null ? "-" : c.getSuperclass().getName()).append('\n');
                for (Field f : c.getDeclaredFields()) sb.append("  F ").append(Modifier.toString(f.getModifiers())).append(' ').append(f.getType().getSimpleName()).append(' ').append(f.getName()).append('\n');
                for (Method m : c.getDeclaredMethods()) {
                    StringJoiner sj = new StringJoiner(", ", "(", ")");
                    for (Class<?> p : m.getParameterTypes()) sj.add(p.getName());
                    sb.append("  M ").append(Modifier.toString(m.getModifiers())).append(' ').append(m.getReturnType().getSimpleName()).append(' ').append(m.getName()).append(sj).append('\n');
                }
            } catch (Throwable t) {
                sb.append("== ").append(cn).append(" (could not load: ").append(t.getClass().getSimpleName()).append(")\n");
            }
            if (sb.length() > 400_000) { sb.append("...truncated\n"); break; }
        }
        Path out = FabricLoader.getInstance().getGameDir().resolve("tenz-api-dump.txt");
        Files.writeString(out, sb.toString());
        return out;
    }
}
