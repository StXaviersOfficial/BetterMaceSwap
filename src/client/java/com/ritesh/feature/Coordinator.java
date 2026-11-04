package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class Coordinator {

    // Called every render frame (from BettermaceswapClient or mixin)
    public static void onRender(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        if (!cfg.aimAssistEnabled) return;
        AimAssist.applyRotation(p, cfg);
    }

    // Called every game tick
    public static void tick(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();

        // Combat (breach swap, last-slot tracking)
        Combat.tick(p);

        // Auto chestplate swap: must run BEFORE trigger bot, and works even if aim assist is off
        AimAssist.tickAutoChestplate(client, cfg);

        // Aim Assist
        if (cfg.aimAssistEnabled) {
            AimAssist.tick(client, cfg);
            AimAssist.applyRotation(p, cfg);
        }

        // Trigger Bot: independent of Aim Assist (only needs the crosshair on a target in range)
        AimAssist.tickTriggerBot(client, cfg);

        // Quick Gear Up click queue
        GearUp.tick(client);

        // Pearl Catch
        Pearl.tick(client);

        // Stun Slam
        StunSlam.tick(client);

        // Misc (lunge swap, wind charge, elytra boost, air pots)
        Misc.tick(client);
    }

    // Called when the player hits an entity
    public static void onHit(Player player, Entity target) {
        Combat.onHit(player, target);
    }

    // Called when the player attacks (miss / block hit)
    public static void onAttack(Player player) {
        if (player instanceof LocalPlayer cp)
            Misc.onAttack(cp);
    }

    // Called on block hit (for lunge swap)
    public static void onBlockHit(LocalPlayer p) {
        Misc.onBlockHit(p);
    }

    // Called when an ender pearl is thrown
    public static void onPearlThrown(LocalPlayer player) {
        Pearl.onPearlThrown(player);
    }

    // Called when a splash potion is thrown
    public static void onSplashPotThrown(LocalPlayer player) {
        Misc.onSplashPotThrown(player);
    }

    // Called when elytra boost keybind is pressed
    public static void armElytraBoost() {
        Misc.armElytraBoost();
    }

    // Called when a pearl hits an entity
    public static void onPearlHitEntity(Entity target) {
    }

    // Aim lock stubs (kept for keybind compatibility)
    public static void toggleAimLock(LocalPlayer player, Minecraft client) {
    }

    public static boolean isAimLockActive() {
        return false;
    }

    // Full reset on disconnect / world change
    public static void fullReset(LocalPlayer player) {
        Combat.reset(player);
        StunSlam.reset();
        Pearl.reset();
        AimAssist.reset();
        AimAssist.resetTriggerBot();
        Misc.reset();
        GearUp.reset();
    }

    // Lunge swap delegation (used by LungeAttackMixin)
    public static boolean isLungeSwapPending() {
        return Misc.isLungeSwapPending();
    }

    public static void startLungeSwap(int returnSlot) {
        Misc.startLungeSwap(returnSlot);
    }
}
