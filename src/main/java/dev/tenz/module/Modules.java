package dev.tenz.module;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;

/** All toggles live here; saved to config/tenz.properties. */
public final class Modules {
    public static final Module FPS       = new Module("FPS", "Show FPS counter", "HUD", true);
    public static final Module KEYS      = new Module("Keystrokes", "WASD keys", "HUD", true);
    public static final Module AUCTION   = new Module("Auction", "Stream auction overlay", "Auction", true);
    public static final Module SKY       = new Module("Custom sky", "Galaxy / Orbit / Iris / Velvet / Twilight / Silver", "Visuals", false);
    public static final Module SMOOTH    = new Module("Smooth placing", "Animated block placement", "Visuals", true);
    public static final Module ALIEN     = new Module("Alien skins", "Turns every player into an alien", "Fun", false);
    public static final Module PAYMENTS  = new Module("Payment animations", "Master switch for everything below", "Fun", true);
    public static final Module FEED      = new Module("Payment feed", "+$ cards slide in on the right", "Fun", true);
    public static final Module BANNER    = new Module("Donation banner", "Top banner for $100M+", "Fun", true);
    public static final Module LEGEND    = new Module("Legendary popup", "Crown, glow and confetti for $1B+", "Fun", true);
    public static final Module RAIN      = new Module("Money rain", "Falling $ for $50M+", "Fun", true);
    public static final Module SLOTS     = new Module("Slot jackpot", "Slot machine for $250M-$1B", "Fun", true);
    public static final Module WANTED    = new Module("Wanted poster", "New biggest payment, $500M+", "Fun", true);
    public static final List<Module> ALL = List.of(FPS, KEYS, AUCTION, SKY, SMOOTH, ALIEN, PAYMENTS, FEED, BANNER, LEGEND, RAIN, SLOTS, WANTED);
    public static String skyPreset = "Galaxy";
    public static int themeIndex = 0;
    public static int uiScale = 100; // menu size in percent: 100 / 125 / 150
    public static boolean alienHooked = false;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("tenz.properties");

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (var in = Files.newInputStream(FILE)) {
            Properties p = new Properties(); p.load(in);
            for (Module m : ALL) m.enabled = Boolean.parseBoolean(p.getProperty(m.name, String.valueOf(m.enabled)));
            skyPreset = p.getProperty("skyPreset", skyPreset);
            try { uiScale = Integer.parseInt(p.getProperty("uiScale", "100")); } catch (NumberFormatException ignored3) {}
            if (uiScale != 100 && uiScale != 125 && uiScale != 150) uiScale = 100;
            try { themeIndex = Integer.parseInt(p.getProperty("theme", "0")); } catch (NumberFormatException ignored2) {}
        } catch (IOException ignored) {}
    }
    public static void save() {
        Properties p = new Properties();
        for (Module m : ALL) p.setProperty(m.name, String.valueOf(m.enabled));
        p.setProperty("skyPreset", skyPreset);
        p.setProperty("theme", String.valueOf(themeIndex));
        p.setProperty("uiScale", String.valueOf(uiScale));
        try (var out = Files.newOutputStream(FILE)) { p.store(out, "Tenz"); } catch (IOException ignored) {}
    }
}
