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
    private static final Pattern PAID = Pattern.compile("(\\w{3,16}) (?:paid|sent|gave) you \\$([\\d.,]+)\\s*([KMBTkmbt]?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAID2 = Pattern.compile("you (?:received|were paid|got) \\$([\\d.,]+)\\s*([KMBTkmbt]?) from (\\w{3,16})", Pattern.CASE_INSENSITIVE);

    // "You have $ 13,619,634"  /  "Balance: $1.2M"
    private static final Pattern BAL = Pattern.compile("(?:you have|balance:?|bal:?)\\s*\\$\\s*(\\d+(?:[.,]\\d+)*)\\s*([KMBTkmbt]?)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public void onInitializeClient() {
        Modules.load();
        Payments.loadHistory();
        // Writes tenz-api-dump.txt into the game folder once, so the sky / placement hooks can be fitted.
        try {
            if (!java.nio.file.Files.exists(net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("tenz-api-dump.txt"))) ApiDump.run();
        } catch (Throwable ignored) {}

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tenz", "main"));
        KeyMapping open = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.open", InputConstants.KEY_RSHIFT, cat));
        KeyMapping auction = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.auction", InputConstants.KEY_K, cat));
        KeyMapping donors = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.donors", InputConstants.KEY_O, cat));
        KeyMapping trick = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.trick", InputConstants.KEY_R, cat));
        KeyMapping trickRandom = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenz.trick_random", InputConstants.KEY_V, cat));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            dev.tenz.fun.BalancePoll.tick(mc);
            while (trick.consumeClick()) dev.tenz.fun.Tricks.play(Modules.knifeTrick);
            while (trickRandom.consumeClick()) dev.tenz.fun.Tricks.play("Random");
            while (open.consumeClick()) mc.setScreenAndShow(new TenzScreen());
            while (auction.consumeClick()) mc.setScreenAndShow(new AuctionScreen());
            while (donors.consumeClick()) mc.setScreenAndShow(new DonorsScreen());
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("tenz", "overlays"),
            (graphics, tick) -> Overlays.render(graphics));

        // Balance reply to our automatic /bal: read it, then hide it from chat
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) return true;
            try {
                Matcher mb = BAL.matcher(message.getString());
                if (mb.find()) {
                    dev.tenz.fun.Balance.set(Payments.parse(mb.group(1), mb.group(2)));
                    if (dev.tenz.fun.BalancePoll.recentlySent()) return false;
                }
            } catch (NumberFormatException ignored) {}
            return true;
        });

        // Real payments: chat lines like "Name paid you $10M"
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            String msg = message.getString();
            try {
                Matcher mb = BAL.matcher(msg);
                if (mb.find()) { dev.tenz.fun.Balance.set(Payments.parse(mb.group(1), mb.group(2))); return; }
            } catch (NumberFormatException ignored) {}
            try {
                Matcher m = PAID.matcher(msg);
                if (m.find()) { Payments.add(m.group(1), Payments.parse(m.group(2), m.group(3))); return; }
                Matcher m2 = PAID2.matcher(msg);
                if (m2.find()) Payments.add(m2.group(3), Payments.parse(m2.group(1), m2.group(2)));
            } catch (NumberFormatException ignored) {}
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
              .then(ClientCommands.literal("auto")
                .then(ClientCommands.argument("every", StringArgumentType.word()).executes(c -> {
                    String v = StringArgumentType.getString(c, "every").toLowerCase();
                    Modules.autoBal = switch (v) { case "30", "30s" -> "30s"; case "60", "1m", "1" -> "1 min"; case "120", "2m", "2" -> "2 min"; default -> "Off"; };
                    dev.tenz.fun.BalancePoll.reset(); Modules.save();
                    c.getSource().sendFeedback(net.minecraft.network.chat.Component.literal("Auto balance: " + Modules.autoBal));
                    return 1; })))
              .then(ClientCommands.literal("balcmd")
                .then(ClientCommands.argument("name", StringArgumentType.word()).executes(c -> {
                    Modules.balCmd = StringArgumentType.getString(c, "name").replace("/", "");
                    dev.tenz.fun.BalancePoll.reset(); Modules.save();
                    c.getSource().sendFeedback(net.minecraft.network.chat.Component.literal("Balance command: /" + Modules.balCmd));
                    return 1; })))
              .then(ClientCommands.literal("bal")
                .then(ClientCommands.argument("amount", StringArgumentType.word()).executes(c -> {
                    Matcher m = Pattern.compile("([\\d.,]+)([KMBTkmbt]?)").matcher(StringArgumentType.getString(c, "amount").replace("$", ""));
                    if (!m.matches()) return 0;
                    try { dev.tenz.fun.Balance.set(Payments.parse(m.group(1), m.group(2))); } catch (NumberFormatException e) { return 0; }
                    return 1; })))
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
