package dev.tenz.fun;

import dev.tenz.module.Modules;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Knife models. When one is picked, the item in your hand is drawn with one of the Tenz knife models
 * (assets/tenz/items/&lt;knife&gt;_&lt;finish&gt;.json). Only you see it; nothing is sent to the server.
 */
public final class KnifeSkins {
    public static final String[] MODELS = {"None", "Karambit", "Butterfly", "Bayonet", "Tanto", "Stiletto", "Bowie"};
    public static final String[] FINISHES = {"Steel", "Gold", "Blood", "Frost", "Void", "Emerald"};
    public static final String[] APPLY = {"Swords & knives", "Any item"};

    private static ItemStack lastIn = null, lastOut = null;
    private static String lastKey = "";

    /** Returns the stack to draw instead, or null to leave the item alone. */
    public static ItemStack swap(ItemStack stack, ItemDisplayContext ctx, LivingEntity entity) {
        String model = Modules.knifeModel;
        if (model == null || model.equals("None")) return null;
        if (stack == null || stack.isEmpty()) return null;
        if (ctx != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && ctx != ItemDisplayContext.FIRST_PERSON_LEFT_HAND
            && ctx != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND && ctx != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || entity != mc.player) return null;
        if (stack.getItem() != mc.player.getMainHandItem().getItem()) return null;   // main hand only
        if (!"Any item".equals(Modules.knifeApply) && !looksLikeKnife(stack)) return null;

        String key = model.toLowerCase(Locale.ROOT) + "_" + Modules.knifeFinish.toLowerCase(Locale.ROOT);
        if (stack == lastIn && key.equals(lastKey) && lastOut != null) return lastOut;
        ItemStack copy = stack.copy();
        copy.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("tenz", key));
        lastIn = stack; lastOut = copy; lastKey = key;
        return copy;
    }

    private static boolean looksLikeKnife(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.contains("sword") || path.contains("knife") || path.contains("dagger")) return true;
        String n = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
        return n.contains("knife") || n.contains("karambit") || n.contains("dagger") || n.contains("blade")
            || n.contains("bayonet") || n.contains("tanto") || n.contains("sword") || n.contains("shiv");
    }

    private KnifeSkins() {}
}
