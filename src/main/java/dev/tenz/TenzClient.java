package dev.tenz;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.*;
import dev.tenz.fun.Payments;
import dev.tenz.gui.*;
import dev.tenz.hud.*;
import dev.tenz.module.Modules;
import java.util.regex.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class TenzClient implements ClientModInitializer {
    private static final Pattern PAID = Pattern.compile("(\\w{3,16}) paid you \\$([\\d.,]+)\\s*([KMBTkmbt]?)");

    @Override
    public void onInitializeClient() {
        Modules.load();
        Payments.loadHistory();

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tenz", "main"));
        KeyMapping open = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.open", InputConstants.KEY_RIGHT_SHIFT, cat));
        KeyMapping auction = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.auction", InputConstants.KEY_K, cat));
        KeyMapping donors = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.donors", InputConstants.KEY_O, cat));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (open.consumeClick()) mc.setScreenAndShow(new TenzScreen());
            while (auction.consumeClick()) mc.setScreenAndShow(new AuctionScreen());
            while (donors.consumeClick()) mc.setScreenAndShow(new DonorsScreen());
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("tenz", "overlays"),
            (graphics, tick) -> Overlays.render(graphics));

        // Real payments: chat lines like "Name paid you $10M"
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            Matcher m = PAID.matcher(message.getString());
            if (m.find()) {
                try { Payments.add(m.group(1), Payments.parse(m.group(2), m.group(3))); } catch (NumberFormatException ignored) {}
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((d, ctx) -> d.register(
            ClientCommands.literal("tenz")
              .then(ClientCommands.literal("dump").executes(c -> {
                  try {
                      var path = ApiDump.run();
                      c.getSource().sendFeedback(net.minecraft.network.chat.Component.literal("Saved: " + path));
                  } catch (Exception e) {
                      c.getSource().sendFeedback(net.minecraft.network.chat.Component.literal("Dump failed: " + e));
                  }
                  return 1; }))
              .then(ClientCommands.literal("demo").executes(c -> { Payments.demo(); return 1; }))
              .then(ClientCommands.literal("pay")
                .then(ClientCommands.argument("name", StringArgumentType.word())
                  .then(ClientCommands.argument("amount", StringArgumentType.word()).executes(c -> {
                      String a = StringArgumentType.getString(c, "amount").replace("$", "");
                      Matcher m = Pattern.compile("([\\d.,]+)([KMBTkmbt]?)").matcher(a);
                      if (!m.matches()) return 0;
                      Payments.add(StringArgumentType.getString(c, "name"), Payments.parse(m.group(1), m.group(2)));
                      return 1; }))))
              .then(ClientCommands.literal("auction")
                .then(ClientCommands.argument("seconds", IntegerArgumentType.integer(5, 3600))
                  .then(ClientCommands.argument("item", StringArgumentType.greedyString()).executes(c -> {
                      AuctionState.start(StringArgumentType.getString(c, "item"), IntegerArgumentType.getInteger(c, "seconds"));
                      return 1; }))))
              .then(ClientCommands.literal("bid")
                .then(ClientCommands.argument("name", StringArgumentType.word())
                  .then(ClientCommands.argument("amount", DoubleArgumentType.doubleArg(0)).executes(c -> {
                      AuctionState.bid(StringArgumentType.getString(c, "name"), DoubleArgumentType.getDouble(c, "amount"));
                      return 1; }))))));
    }
}
