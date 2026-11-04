package com.ritesh;

import com.ritesh.config.ModConfig;
import com.ritesh.feature.Coordinator;
import com.ritesh.feature.GearUp;
import com.ritesh.command.BmsCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.ritesh.keybind.KeyBindings;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;

public class BettermaceswapClient implements ClientModInitializer {
    private boolean wasBreachSwapPressed = false;
    private boolean wasStunSlamPressed = false;
    private boolean wasMaceCyclePressed = false;
    private boolean wasPearlCatchPressed = false;
    private boolean wasAimAssistPressed = false;
    private boolean wasElytraBoostArmPressed = false;
    private boolean wasTriggerBotPressed = false;
    private boolean wasLungeSwapPressed = false;
    private boolean wasAimModePressed = false;
    private boolean wasGearUpPressed = false;
    private boolean wasGearUp2Pressed = false;
    private boolean wasGearUp3Pressed = false;

    public void onInitializeClient() {

        ClientCommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess) -> BmsCommand.register(dispatcher));

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        KeyBindings.register();

        // Per-frame aim assist (same as 1.21.11): HudRenderCallback was removed, HudElementRegistry replaces it.
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("bettermaceswap", "aim_assist"),
                (graphics, tickCounter) -> Coordinator.onRender(Minecraft.getInstance()));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && player == client.player) {
                if (player.getItemInHand(hand).is(Items.ENDER_PEARL)) {
                    Coordinator.onPearlThrown((LocalPlayer) player);
                }
                if (player.getItemInHand(hand).is(Items.SPLASH_POTION)) {
                    Coordinator.onSplashPotThrown((LocalPlayer) player);
                }
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ModConfig cfg = ModConfig.get();

                boolean breachPressed = KeyBindings.BREACH_SWAP_TOGGLE.isDown();
                if (!breachPressed && wasBreachSwapPressed) {
                    cfg.autoBreachSwapEnabled = !cfg.autoBreachSwapEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Attribute Swap: ").append(
                                Component.literal(cfg.autoBreachSwapEnabled ? "ON" : "OFF")
                                        .withStyle(cfg.autoBreachSwapEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)),
                                false);
                }
                wasBreachSwapPressed = breachPressed;

                boolean stunPressed = KeyBindings.STUN_SLAM_TOGGLE.isDown();
                if (!stunPressed && wasStunSlamPressed) {
                    cfg.autoStunSlamEnabled = !cfg.autoStunSlamEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Stun Slam: ").append(
                                Component.literal(cfg.autoStunSlamEnabled ? "ON" : "OFF")
                                        .withStyle(cfg.autoStunSlamEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)),
                                false);
                }
                wasStunSlamPressed = stunPressed;

                boolean cyclePressed = KeyBindings.MACE_MODE_CYCLE.isDown();
                if (!cyclePressed && wasMaceCyclePressed) {
                    cfg.maceMode = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY
                            ? ModConfig.MaceEnchantMode.BREACH
                            : ModConfig.MaceEnchantMode.DENSITY;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Mace Mode: ").append(
                                Component.literal(cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY ? "Density" : "Breach")
                                        .withStyle(cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY ? ChatFormatting.YELLOW
                                                : ChatFormatting.DARK_PURPLE)),
                                false);
                }
                wasMaceCyclePressed = cyclePressed;

                boolean pearlPressed = KeyBindings.PEARL_CATCH_TOGGLE.isDown();
                if (!pearlPressed && wasPearlCatchPressed) {
                    cfg.pearlCatchEnabled = !cfg.pearlCatchEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Pearl Catch: ").append(
                                Component.literal(cfg.pearlCatchEnabled ? "ON" : "OFF")
                                        .withStyle(cfg.pearlCatchEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)),
                                false);
                }
                wasPearlCatchPressed = pearlPressed;

                boolean elytraArmPressed = KeyBindings.ELYTRA_BOOST_ARM.isDown();
                if (!elytraArmPressed && wasElytraBoostArmPressed) {
                    if (cfg.elytraBoostEnabled) {
                        Coordinator.armElytraBoost();
                        if (cfg.showActionBarMessages)
                            client.gui.hud.setOverlayMessage(Component.literal("Elytra Boost: ").append(
                                    Component.literal("ARMED").withStyle(ChatFormatting.GREEN)), false);
                    }
                }
                wasElytraBoostArmPressed = elytraArmPressed;

                boolean lungePressed = KeyBindings.LUNGE_SWAP_TOGGLE.isDown();
                if (!lungePressed && wasLungeSwapPressed) {
                    cfg.lungeSwapEnabled = !cfg.lungeSwapEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Lunge Swap: ").append(
                                Component.literal(cfg.lungeSwapEnabled ? "ON" : "OFF")
                                        .withStyle(cfg.lungeSwapEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)),
                                false);
                }
                wasLungeSwapPressed = lungePressed;

                boolean assistPressed = KeyBindings.AIM_ASSIST_TOGGLE.isDown();
                if (!assistPressed && wasAimAssistPressed) {
                    cfg.aimAssistEnabled = !cfg.aimAssistEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Aim Assist: ").append(
                                Component.literal(cfg.aimAssistEnabled ? "ON" : "OFF")
                                        .withStyle(cfg.aimAssistEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)),
                                false);
                }
                wasAimAssistPressed = assistPressed;

                boolean triggerPressed = KeyBindings.TRIGGER_BOT_TOGGLE.isDown();
                if (!triggerPressed && wasTriggerBotPressed) {
                    cfg.triggerBotEnabled = !cfg.triggerBotEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Trigger Bot: ").append(
                                Component.literal(cfg.triggerBotEnabled ? "ON" : "OFF")
                                        .withStyle(cfg.triggerBotEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)),
                                false);
                }
                wasTriggerBotPressed = triggerPressed;

                boolean aimModePressed = KeyBindings.AIM_MODE_TOGGLE.isDown();
                if (!aimModePressed && wasAimModePressed) {
                    cfg.aimMode = cfg.aimMode == ModConfig.AimMode.WHILE_FALLING
                            ? ModConfig.AimMode.ALWAYS
                            : ModConfig.AimMode.WHILE_FALLING;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.gui.hud.setOverlayMessage(Component.literal("Aim Mode: ").append(cfg.aimMode.asText()), false);
                }
                wasAimModePressed = aimModePressed;

                boolean gearUpPressed = KeyBindings.GEAR_UP.isDown();
                if (!gearUpPressed && wasGearUpPressed) {
                    GearUp.start(client);
                }
                wasGearUpPressed = gearUpPressed;

                boolean gearUp2Pressed = KeyBindings.GEAR_UP_2.isDown();
                if (!gearUp2Pressed && wasGearUp2Pressed) {
                    GearUp.start(client, 2);
                }
                wasGearUp2Pressed = gearUp2Pressed;

                boolean gearUp3Pressed = KeyBindings.GEAR_UP_3.isDown();
                if (!gearUp3Pressed && wasGearUp3Pressed) {
                    GearUp.start(client, 3);
                }
                wasGearUp3Pressed = gearUp3Pressed;

                Coordinator.tick(client);
            }
        });
    }
}