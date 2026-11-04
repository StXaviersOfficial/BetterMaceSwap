package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.feature.Misc;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Combat {
    private static int originalSlot = -1;
    private static int swapBackTimer = 0;
    private static boolean isBreachSwapped = false;
    static int lastSelectedSlot = -1;
    static int swordSlotBeforeAxe = -1;

    public static void onHit(Player player, Entity target) {
        if (player == null || target == null)
            return;
        if (StunSlam.isInSlamCombo())
            return;
        if (StunSlam.isAttacking())
            return;
        ModConfig cfg = ModConfig.get();
        if (Misc.isLungeSwapPending())
            return;

        boolean isPlayer = target instanceof Player;
        boolean isMob = target instanceof Mob || target instanceof Animal;
        if (isPlayer && !cfg.combatTargetPlayers)
            return;
        if (isMob && !cfg.combatTargetMobs)
            return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();
        ItemStack held = player.getMainHandItem();

        boolean isWeapon = isSword(held) || isAxe(held) || held.is(Items.MACE);
        boolean allowedByMode = isWeapon || (cfg.attributeSwapMode == ModConfig.AttributeSwapMode.ALL);
        boolean slamAllowed = cfg.autoStunSlamEnabled
                && (cfg.stunSlamTriggerMode == ModConfig.StunSlamTriggerMode.ALL || isWeapon);
        if (!allowedByMode && !slamAllowed)
            return;

        if (slamAllowed && target instanceof LivingEntity tp) {
            if (tp.isBlocking() && player.distanceTo(target) <= 3.0) {
                int axeSlot = findItem(player, "axe");
                boolean wantDensity = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY;
                int maceSlot = findBestMace(player, wantDensity);
                int returnAfterSlam = (swordSlotBeforeAxe != -1) ? swordSlotBeforeAxe : startingSlot;
                swordSlotBeforeAxe = -1;

                if (axeSlot != -1 && maceSlot != -1) {
                    Minecraft mc = Minecraft.getInstance();
                    StunSlam.setInSlamCombo(true);
                    try {
                        if (!isAxe(held)) {
                            inv.setSelectedSlot(axeSlot);
                            mc.gameMode.attack(player, target);
                            player.swing(InteractionHand.MAIN_HAND);
                        }
                    } finally {
                        StunSlam.setInSlamCombo(false);
                    }
                    StunSlam.setSlamReturnSlotQueued(returnAfterSlam);
                    int slamDelay = switch (cfg.stunSlamMode) {
                        case SAFE -> 1;
                        case UNSAFE, NORMAL -> 0;
                    };
                    StunSlam.queueMaceSlam(target, maceSlot, cfg.stunSlamReturnSlot ? returnAfterSlam : maceSlot,
                            slamDelay);
                    return;
                }

                if (axeSlot != -1) {
                    inv.setSelectedSlot(axeSlot);
                    StunSlam.setAxeSwingDelay(cfg.noMaceAxeHitDelay);
                    StunSlam.setSlamTarget(target);
                    StunSlam.setSlamReturnSlotQueued(returnAfterSlam);
                    if (maceSlot != -1) {
                        StunSlam.setAxeSwingHasMace(true);
                        StunSlam.setSlamMaceSlot(maceSlot);
                    } else {
                        StunSlam.setAxeSwingHasMace(false);
                    }
                    return;
                }

                if (isAxe(held)) {
                    if (maceSlot != -1) {
                        StunSlam.queueMaceSlam(target, maceSlot,
                                cfg.stunSlamReturnSlot ? returnAfterSlam : StunSlam.getSlamMaceSlot(), 2);
                    } else {
                        StunSlam.queueSlotReturn(cfg.stunSlamReturnSlot ? returnAfterSlam : -1, 3);
                    }
                    return;
                }
            }
        }
        if (!allowedByMode)
            return;
        if (Misc.isLungeSwapPending())
            return;

        if (isBreachSwapped && swapBackTimer <= 0) {
            forceReset((LocalPlayer) player);
        }

        if (cfg.autoBreachSwapEnabled && !isBreachSwapped) {
            boolean wantDensity;
            if (cfg.smartSwitchEnabled) {
                wantDensity = !player.onGround() && player.getDeltaMovement().y < -0.3;
            } else {
                wantDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
            }
            int maceSlot = findBestMace(player, wantDensity);
            if (maceSlot != -1 && maceSlot != startingSlot) {
                originalSlot = startingSlot;
                inv.setSelectedSlot(maceSlot);
                isBreachSwapped = true;
                swapBackTimer = cfg.breachSwapBackTicks + (Math.random() < 0.5 ? 1 : 0);
            }
        }
    }

    public static void tick(LocalPlayer p) {
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        int currentSlot = inv.getSelectedSlot();
        if (!p.getInventory().getItem(currentSlot).is(Items.ENDER_PEARL)) {
            lastSelectedSlot = currentSlot;
        }

        if (isBreachSwapped && --swapBackTimer <= 0)
            forceReset(p);
    }

    private static void forceReset(LocalPlayer player) {
        if (originalSlot != -1)
            ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        isBreachSwapped = false;
        originalSlot = -1;
    }

    public static void reset(LocalPlayer player) {
        forceReset(player);
        lastSelectedSlot = -1;
        swordSlotBeforeAxe = -1;
    }

    // --- Shared item finders (used across categories) ---

    public static boolean isSword(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).toString().contains("sword");
    }

    public static boolean isAxe(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().endsWith("_axe");
    }

    public static boolean isSpear(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().endsWith("_spear");
    }

    public static int findSpear(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().endsWith("_spear"))
                return i;
        }
        return -1;
    }

    public static int findItem(Player p, String name) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && BuiltInRegistries.ITEM.getKey(s.getItem()).toString().contains(name))
                return i;
        }
        return -1;
    }

    /** Hotbar first (single swap click), then the rest of the inventory (9-35). Returns inventory index or -1. */
    public static int findChestplate(LocalPlayer p) {
        for (int i = 0; i < 36; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().endsWith("chestplate"))
                return i;
        }
        return -1;
    }

    public static int findBestMace(Player p, boolean wantDensity) {
        int fallback = -1;
        String search = wantDensity ? "density" : "breach";
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(Items.MACE)) {
                if (fallback == -1)
                    fallback = i;
                if (hasEnchantByName(s, search))
                    return i;
            }
        }
        return fallback;
    }

    public static boolean hasEnchantByName(ItemStack s, String name) {
        ItemEnchantments enc = s.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        String needle = name.toLowerCase();
        return enc.keySet().stream().anyMatch(e -> e.getRegisteredName().contains(needle));
    }

    public static boolean isValidWindItem(ItemStack s) {
        if (!s.has(DataComponents.CUSTOM_NAME))
            return true;
        String name = s.get(DataComponents.CUSTOM_NAME).getString().toLowerCase();
        return name.equals("wind charge") || name.equals("firework rocket");
    }

    public static int getLastSelectedSlot() {
        return lastSelectedSlot;
    }
}
