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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
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

        HudRenderCallback.EVENT
                .register((drawContext, renderTickCounter) -> Coordinator.onRender(MinecraftClient.getInstance()));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && player == client.player) {
                if (player.getStackInHand(hand).isOf(Items.ENDER_PEARL)) {
                    Coordinator.onPearlThrown((ClientPlayerEntity) player);
                }
                if (player.getStackInHand(hand).isOf(Items.SPLASH_POTION)) {
                    Coordinator.onSplashPotThrown((ClientPlayerEntity) player);
                }
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ModConfig cfg = ModConfig.get();

                boolean breachPressed = KeyBindings.BREACH_SWAP_TOGGLE.isPressed();
                if (!breachPressed && wasBreachSwapPressed) {
                    cfg.autoBreachSwapEnabled = !cfg.autoBreachSwapEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Attribute Swap: ").append(
                                Text.literal(cfg.autoBreachSwapEnabled ? "ON" : "OFF")
                                        .formatted(cfg.autoBreachSwapEnabled ? Formatting.GREEN : Formatting.RED)),
                                true);
                }
                wasBreachSwapPressed = breachPressed;

                boolean stunPressed = KeyBindings.STUN_SLAM_TOGGLE.isPressed();
                if (!stunPressed && wasStunSlamPressed) {
                    cfg.autoStunSlamEnabled = !cfg.autoStunSlamEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Stun Slam: ").append(
                                Text.literal(cfg.autoStunSlamEnabled ? "ON" : "OFF")
                                        .formatted(cfg.autoStunSlamEnabled ? Formatting.GREEN : Formatting.RED)),
                                true);
                }
                wasStunSlamPressed = stunPressed;

                boolean cyclePressed = KeyBindings.MACE_MODE_CYCLE.isPressed();
                if (!cyclePressed && wasMaceCyclePressed) {
                    cfg.maceMode = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY
                            ? ModConfig.MaceEnchantMode.BREACH
                            : ModConfig.MaceEnchantMode.DENSITY;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Mace Mode: ").append(
                                Text.literal(cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY ? "Density" : "Breach")
                                        .formatted(cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY ? Formatting.YELLOW
                                                : Formatting.DARK_PURPLE)),
                                true);
                }
                wasMaceCyclePressed = cyclePressed;

                boolean pearlPressed = KeyBindings.PEARL_CATCH_TOGGLE.isPressed();
                if (!pearlPressed && wasPearlCatchPressed) {
                    cfg.pearlCatchEnabled = !cfg.pearlCatchEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Pearl Catch: ").append(
                                Text.literal(cfg.pearlCatchEnabled ? "ON" : "OFF")
                                        .formatted(cfg.pearlCatchEnabled ? Formatting.GREEN : Formatting.RED)),
                                true);
                }
                wasPearlCatchPressed = pearlPressed;

                boolean elytraArmPressed = KeyBindings.ELYTRA_BOOST_ARM.isPressed();
                if (!elytraArmPressed && wasElytraBoostArmPressed) {
                    if (cfg.elytraBoostEnabled) {
                        Coordinator.armElytraBoost();
                        if (cfg.showActionBarMessages)
                            client.player.sendMessage(Text.literal("Elytra Boost: ").append(
                                    Text.literal("ARMED").formatted(Formatting.GREEN)), true);
                    }
                }
                wasElytraBoostArmPressed = elytraArmPressed;

                boolean lungePressed = KeyBindings.LUNGE_SWAP_TOGGLE.isPressed();
                if (!lungePressed && wasLungeSwapPressed) {
                    cfg.lungeSwapEnabled = !cfg.lungeSwapEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Lunge Swap: ").append(
                                Text.literal(cfg.lungeSwapEnabled ? "ON" : "OFF")
                                        .formatted(cfg.lungeSwapEnabled ? Formatting.GREEN : Formatting.RED)),
                                true);
                }
                wasLungeSwapPressed = lungePressed;

                boolean assistPressed = KeyBindings.AIM_ASSIST_TOGGLE.isPressed();
                if (!assistPressed && wasAimAssistPressed) {
                    cfg.aimAssistEnabled = !cfg.aimAssistEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Aim Assist: ").append(
                                Text.literal(cfg.aimAssistEnabled ? "ON" : "OFF")
                                        .formatted(cfg.aimAssistEnabled ? Formatting.GREEN : Formatting.RED)),
                                true);
                }
                wasAimAssistPressed = assistPressed;

                boolean triggerPressed = KeyBindings.TRIGGER_BOT_TOGGLE.isPressed();
                if (!triggerPressed && wasTriggerBotPressed) {
                    cfg.triggerBotEnabled = !cfg.triggerBotEnabled;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Trigger Bot: ").append(
                                Text.literal(cfg.triggerBotEnabled ? "ON" : "OFF")
                                        .formatted(cfg.triggerBotEnabled ? Formatting.GREEN : Formatting.RED)),
                                true);
                }
                wasTriggerBotPressed = triggerPressed;

                boolean aimModePressed = KeyBindings.AIM_MODE_TOGGLE.isPressed();
                if (!aimModePressed && wasAimModePressed) {
                    cfg.aimMode = cfg.aimMode == ModConfig.AimMode.WHILE_FALLING
                            ? ModConfig.AimMode.ALWAYS
                            : ModConfig.AimMode.WHILE_FALLING;
                    ModConfig.save();
                    if (cfg.showActionBarMessages)
                        client.player.sendMessage(Text.literal("Aim Mode: ").append(cfg.aimMode.asText()), true);
                }
                wasAimModePressed = aimModePressed;

                boolean gearUpPressed = KeyBindings.GEAR_UP.isPressed();
                if (!gearUpPressed && wasGearUpPressed) {
                    GearUp.start(client);
                }
                wasGearUpPressed = gearUpPressed;

                boolean gearUp2Pressed = KeyBindings.GEAR_UP_2.isPressed();
                if (!gearUp2Pressed && wasGearUp2Pressed) {
                    GearUp.start(client, 2);
                }
                wasGearUp2Pressed = gearUp2Pressed;

                boolean gearUp3Pressed = KeyBindings.GEAR_UP_3.isPressed();
                if (!gearUp3Pressed && wasGearUp3Pressed) {
                    GearUp.start(client, 3);
                }
                wasGearUp3Pressed = gearUp3Pressed;

                Coordinator.tick(client);
            }
        });
    }
}