package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

public class AimAssist {
    // --- Aim Assist state ---
    private static Entity lockedAimTarget = null;
    private static int losGraceTicks = 0;
    private static long lastAimTimeMs = 0L;
    private static int aimWarmupTicks = 0;
    private static float lastYawGCD = 0.0F;
    private static float lastPitchGCD = 0.0F;

    // --- Trigger Bot state (Aim Assist category in ModConfig) ---
    private static Entity triggerBotTarget = null;
    private static int triggerBotHitsThisFall = 0;
    private static int triggerBotCooldown = 0;
    private static boolean triggerBotArmed = false;
    private static int triggerBotFireDelay = 0;
    private static boolean wasInAir = false;

    // --- Auto Chestplate (elytra -> chestplate swap) state ---
    private static final double CHEST_SWAP_LEAD_TICKS = 5.0;   // extra range = closing speed * this
    private static final double CHEST_SWAP_LATENCY_TICKS = 3.0; // ticks the swap takes; we keep moving meanwhile
    private static final double CHEST_SWAP_CARRY_BLOCKS = 1.0;  // extra momentum carry on top of that
    private static final double CHEST_SWAP_MAX_RANGE = 45.0;
    private static final double CHEST_SWAP_SCAN_RANGE = 40.0;
    private static final double CHEST_SWAP_REARM_MARGIN = 4.0; // blocks beyond range before it re-arms
    // Fixed swap distance: either 4 or 5 blocks, picked per approach (re-picked on re-arm)
    private static final double CHEST_FIXED_MIN = 4.0;
    private static final double CHEST_FIXED_MAX = 5.0;
    private static double chestFixedRange = pickFixedRange();
    private static final int CHEST_SWAP_MAX_ATTEMPTS = 3;
    private static boolean chestWasInAir = false;
    private static boolean chestplateEquipped = false;
    private static int chestSwapAttempts = 0;
    private static int chestSwapVerifyTicks = 0;
    private static double chestLastDist = -1.0;
    private static Entity chestLastTarget = null;

    // --- Aim speed tuning ---
    private static final float AIM_SPEED_BOOST = 2.0F;   // multiplier on max turn rate
    private static final double AIM_SKIP_CHANCE = 0.10;  // chance to skip a rotation step (was 0.35)

    public static void tick(Minecraft client, ModConfig cfg) {
        LocalPlayer p = client.player;
        if (p == null || !cfg.aimAssistEnabled) {
            if (!cfg.aimAssistEnabled) reset();
            return;
        }

        double velocityY = p.getDeltaMovement().y;
        boolean inAir = !p.onGround();
        boolean falling = inAir && velocityY < -0.1;

        if (cfg.aimMode != ModConfig.AimMode.ALWAYS && !falling) {
            reset();
            return;
        }

        double maxDistSq = (double) cfg.aimIntensity * cfg.aimIntensity;

        if (lockedAimTarget != null) {
            boolean valid = lockedAimTarget.isAlive()
                    && !lockedAimTarget.isSpectator()
                    && p.distanceToSqr(lockedAimTarget) <= maxDistSq
                    && inFov(client, p, lockedAimTarget);
            if (valid && !hasLineOfSight(p, client, lockedAimTarget)) {
                if (losGraceTicks > 0) {
                    losGraceTicks--;
                } else {
                    lockedAimTarget = null;
                    lastAimTimeMs = 0L;
                }
            } else if (!valid) {
                lockedAimTarget = null;
                losGraceTicks = 0;
                lastAimTimeMs = 0L;
            } else {
                losGraceTicks = 3;
            }
        }

        if (lockedAimTarget == null && client.level != null) {
            double bestDist = maxDistSq;
            for (Entity e : client.level.entitiesForRendering()) {
                if (e == p || !e.isAlive() || e.isSpectator()) continue;
                boolean isPlayer = e instanceof Player;
                boolean isMob = e instanceof Mob || e instanceof Animal;
                if ((!isPlayer || cfg.aimTargetPlayers) && (!isMob || cfg.aimTargetMobs) && (isPlayer || isMob)) {
                    double dx = e.getX() - p.getX();
                    double dz = e.getZ() - p.getZ();
                    double yawRad = Math.toRadians(p.getYRot());
                    double dot = -Math.sin(yawRad) * dx + Math.cos(yawRad) * dz;
                    if (dot > 0 && inFov(client, p, e) && hasLineOfSight(p, client, e)) {
                        double d = p.distanceToSqr(e);
                        if (d < bestDist) {
                            bestDist = d;
                            lockedAimTarget = e;
                        }
                    }
                }
            }
            losGraceTicks = 3;
            lastAimTimeMs = 0L;
            if (lockedAimTarget != null) {
                aimWarmupTicks = 1 + (int) (Math.random() * 2);
            }
        }
    }

    /** True if the entity is on screen (inside the player's FOV setting, horizontally and vertically). */
    private static boolean inFov(Minecraft client, LocalPlayer p, Entity e) {
        Vec3 eye = p.getEyePosition();
        double dx = e.getX() - eye.x;
        double dy = (e.getY() + e.getBbHeight() * 0.5) - eye.y;
        double dz = e.getZ() - eye.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horiz));
        float yawDiff = Mth.wrapDegrees(targetYaw - p.getYRot());
        float pitchDiff = Mth.wrapDegrees(targetPitch - p.getXRot());
        double vFov = Math.toRadians(client.options.fov().get());
        double aspect = (double) client.getWindow().getWidth() / Math.max(1, client.getWindow().getHeight());
        double hFov = 2.0 * Math.atan(Math.tan(vFov / 2.0) * aspect);
        return Math.abs(yawDiff) <= Math.toDegrees(hFov) / 2.0 && Math.abs(pitchDiff) <= Math.toDegrees(vFov) / 2.0;
    }

    public static void applyRotation(LocalPlayer p, ModConfig cfg) {
        if (lockedAimTarget == null || p == null) return;
        if (aimWarmupTicks > 0) { aimWarmupTicks--; return; }
        if (Math.random() < AIM_SKIP_CHANCE) return;

        long now = System.currentTimeMillis();
        float deltaSeconds = lastAimTimeMs == 0L ? 0.05F : (float) (now - lastAimTimeMs) / 1000.0F;
        deltaSeconds = Math.min(deltaSeconds, 0.15F);
        lastAimTimeMs = now;

        double targetY = switch (cfg.aimBone) {
            case CHEST -> lockedAimTarget.getY() + lockedAimTarget.getBbHeight() * 0.65;
            case LEGS -> lockedAimTarget.getY() + lockedAimTarget.getBbHeight() * 0.2;
            default -> lockedAimTarget.getEyeY();
        };

        double dx = lockedAimTarget.getX() - p.getX();
        double dy = targetY - p.getEyeY();
        double dz = lockedAimTarget.getZ() - p.getZ();
        double diffXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, diffXZ)));
        float currentYaw = p.getYRot();
        float currentPitch = p.getXRot();

        float yawDiff = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - currentPitch);

        float yawSpeed = cfg.aimSpeed + (float) (Math.random() * 8.0 - 4.0);
        float pitchSpeed = cfg.aimSpeed * 0.75F + (float) (Math.random() * 6.0 - 3.0);
        float yawMax = yawSpeed * deltaSeconds * AIM_SPEED_BOOST;
        float pitchMax = pitchSpeed * deltaSeconds * AIM_SPEED_BOOST;

        float smoothing = 0.22F + (float) (Math.random() * 0.08);
        float rawYawStep = Mth.clamp(yawDiff * smoothing, -yawMax, yawMax);
        float rawPitchStep = Mth.clamp(pitchDiff * smoothing, -pitchMax, pitchMax);

        if (Math.abs(yawDiff) > 15.0F) rawPitchStep *= 0.3F;

        float sens = cfg.aimSpeed / 100.0F;
        float gcdX = (float) Math.pow(sens * 0.6, 2.0) * 0.15F;
        float gcdY = (float) Math.pow(sens * 0.6, 2.0) * 0.15F;
        if (gcdX > 0.001F) rawYawStep = Math.round(rawYawStep / gcdX) * gcdX;
        if (gcdY > 0.001F) rawPitchStep = Math.round(rawPitchStep / gcdY) * gcdY;

        float yawNoise = (float) (Math.random() * 0.02 - 0.01);
        float pitchNoise = (float) (Math.random() * 0.015 - 0.0075);

        p.setYRot(currentYaw + rawYawStep + yawNoise);
        p.setXRot(Mth.clamp(currentPitch + rawPitchStep + pitchNoise, -90.0F, 90.0F));
    }

    // --- Trigger Bot (Aim Assist category in ModConfig) ---

    public static void tickTriggerBot(Minecraft client, ModConfig cfg) {
        LocalPlayer p = client.player;
        if (p == null) return;

        boolean inAir = !p.onGround();
        if (wasInAir && !inAir) {
            triggerBotHitsThisFall = 0;
        }
        wasInAir = inAir;

        if (triggerBotCooldown > 0) triggerBotCooldown--;

        if (cfg.triggerBotEnabled
                && triggerBotHitsThisFall < 2
                && triggerBotCooldown == 0
                && !triggerBotArmed
                && !isChestSwapPending(p, cfg)) {

            // Trigger Bot only attacks while falling
            boolean fallingCheck = !p.onGround() && p.getDeltaMovement().y < -0.1;

            if (fallingCheck) {
                if (client.hitResult instanceof EntityHitResult ehr) {
                    Entity t = ehr.getEntity();
                    double dist = p.distanceTo(t);
                    boolean inRange = dist <= cfg.aimIntensity;
                    boolean valid = (t instanceof Player && cfg.combatTargetPlayers)
                            || ((t instanceof Mob || t instanceof Animal) && cfg.combatTargetMobs);
                    if (valid && inRange) {
                        triggerBotArmed = true;
                        triggerBotTarget = t;
                        // Auto chestplate on: hit the same tick as the swap or exactly 1 tick after.
                        // Off: original random 1-3 delay.
                        triggerBotFireDelay = cfg.autoChestplateOnTrigger
                                ? 1 + (int) (Math.random() * 2)
                                : 1 + (int) (Math.random() * 3);
                    }
                }
            }
        }

        if (triggerBotArmed && isChestSwapPending(p, cfg)) {
            // wait for the chestplate to land before hitting
        } else if (triggerBotArmed && --triggerBotFireDelay <= 0) {
            triggerBotArmed = false;
            if (triggerBotTarget != null && triggerBotTarget.isAlive()) {
                client.gameMode.attack(p, triggerBotTarget);
                p.swing(InteractionHand.MAIN_HAND);
                triggerBotHitsThisFall++;
                triggerBotCooldown = 3 + (int) (Math.random() * 2);
                triggerBotTarget = null;
            }
        }
    }

    // --- Auto Chestplate (elytra -> chestplate before the hit) ---
    // Called every tick from Coordinator, independent of aim assist being on.

    public static void tickAutoChestplate(Minecraft client, ModConfig cfg) {
        LocalPlayer p = client.player;
        if (p == null || client.gameMode == null) return;

        if (!cfg.autoChestplateOnTrigger) return; // independent of trigger bot

        // Verify the last swap actually landed; retry if not
        if (chestSwapVerifyTicks > 0) {
            if (--chestSwapVerifyTicks == 0) {
                if (isChestplate(p.getItemBySlot(EquipmentSlot.CHEST))
                        || chestSwapAttempts >= CHEST_SWAP_MAX_ATTEMPTS) {
                    chestplateEquipped = true; // done (or gave up for this target)
                }
            }
            return;
        }

        // Everything below is decided by the target entity, not by the ground.
        Entity target = (cfg.aimAssistEnabled && lockedAimTarget != null && lockedAimTarget.isAlive())
                ? lockedAimTarget
                : nearestSwapTarget(client, p, cfg);
        if (target == null) {
            if (chestplateEquipped && !isChestplate(p.getItemBySlot(EquipmentSlot.CHEST))) rearmChestSwap();
            chestLastDist = -1.0;
            chestLastTarget = null;
            return;
        }

        // Closing speed = how much the distance shrank since last tick
        double dist = p.distanceTo(target);
        double closing = (chestLastTarget == target && chestLastDist >= 0.0)
                ? Math.max(0.0, chestLastDist - dist) : 0.0;
        chestLastDist = dist;
        chestLastTarget = target;

        boolean inAir = !p.onGround();
        boolean airborne = p.isFallFlying() || (inAir && p.getDeltaMovement().y < -0.1);
        boolean exact = true; // swap distance is always the random 4-5 block range
        boolean fallingCheck = true; // chestplate swap is distance based, not tied to falling
        // Swap at 4 or 5 blocks (picked at random each approach)
        double range = chestFixedRange;

        if (chestplateEquipped) {
            // Re-arm once the entity is far away and the elytra is back on, so the next approach swaps again
            if (dist > range + CHEST_SWAP_REARM_MARGIN && !isChestplate(p.getItemBySlot(EquipmentSlot.CHEST))) {
                rearmChestSwap();
            }
            return;
        }

        // Already wearing one
        if (isChestplate(p.getItemBySlot(EquipmentSlot.CHEST))) {
            chestplateEquipped = true;
            return;
        }

        if (p.containerMenu != p.inventoryMenu) return;

        if (!fallingCheck || dist > range) return;

        int idx = Combat.findChestplate(p);
        if (idx == -1) return;

        int syncId = p.inventoryMenu.containerId;
        if (idx < 9) {
            // hotbar: one swap click
            client.gameMode.handleContainerInput(syncId, 6, idx, ContainerInput.SWAP, p);
        } else {
            // main inventory: pick up, place on chest slot, put the elytra back where the chestplate was
            client.gameMode.handleContainerInput(syncId, idx, 0, ContainerInput.PICKUP, p);
            client.gameMode.handleContainerInput(syncId, 6, 0, ContainerInput.PICKUP, p);
            client.gameMode.handleContainerInput(syncId, idx, 0, ContainerInput.PICKUP, p);
        }
        chestSwapAttempts++;
        chestSwapVerifyTicks = 1;
    }

    private static Entity nearestSwapTarget(Minecraft client, LocalPlayer p, ModConfig cfg) {
        if (client.level == null) return null;
        Entity best = null;
        double bestDistSq = CHEST_SWAP_SCAN_RANGE * CHEST_SWAP_SCAN_RANGE;
        for (Entity e : client.level.entitiesForRendering()) {
            if (e == p || !e.isAlive() || e.isSpectator()) continue;
            boolean isPlayer = e instanceof Player;
            boolean isMob = e instanceof Mob || e instanceof Animal;
            boolean valid = (isPlayer && cfg.aimTargetPlayers) || (isMob && cfg.aimTargetMobs);
            if (!valid) continue;
            double d = p.distanceToSqr(e);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = e;
            }
        }
        return best;
    }

    /** True while auto chestplate is on, not worn yet, and one is available to swap in. */
    private static boolean isChestSwapPending(LocalPlayer p, ModConfig cfg) {
        if (!cfg.autoChestplateOnTrigger || chestplateEquipped) return false;
        if (isChestplate(p.getItemBySlot(EquipmentSlot.CHEST))) return false;
        return Combat.findChestplate(p) != -1;
    }

    private static boolean isChestplate(ItemStack s) {
        return !s.isEmpty() && BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().endsWith("chestplate");
    }

    /** Allow another swap without wiping the closing-speed tracking. */
    private static void rearmChestSwap() {
        chestplateEquipped = false;
        chestSwapAttempts = 0;
        chestFixedRange = pickFixedRange();
    }

    private static double pickFixedRange() {
        return Math.random() < 0.5 ? CHEST_FIXED_MIN : CHEST_FIXED_MAX;
    }

    private static void resetChestSwap() {
        chestplateEquipped = false;
        chestFixedRange = pickFixedRange();
        chestSwapAttempts = 0;
        chestSwapVerifyTicks = 0;
        chestLastDist = -1.0;
        chestLastTarget = null;
    }

    public static void resetTriggerBot() {
        triggerBotHitsThisFall = 0;
        triggerBotCooldown = 0;
        triggerBotArmed = false;
        triggerBotFireDelay = 0;
        wasInAir = false;
        chestWasInAir = false;
        resetChestSwap();
        triggerBotTarget = null;
    }

    public static Entity getLockedTarget() { return lockedAimTarget; }
    public static boolean hasTarget() { return lockedAimTarget != null; }

    public static void reset() {
        lockedAimTarget = null;
        losGraceTicks = 0;
        lastAimTimeMs = 0L;
        aimWarmupTicks = 0;
        lastYawGCD = 0.0F;
        lastPitchGCD = 0.0F;
    }

    public static boolean hasLineOfSight(LocalPlayer p, Minecraft mc, Entity target) {
        if (mc.level == null) return false;
        Vec3 eyes = p.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        return mc.level.clip(new ClipContext(
                eyes, targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, p
        )).getType() == HitResult.Type.MISS;
    }
}
