package com.ritesh.feature;

import net.minecraft.world.item.component.SwingAnimation;
import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;

public class StunSlam {
    private static Entity slamTarget = null;
    private static int slamMaceSlot = -1;
    private static boolean slamPending = false;
    private static int slamMaceDelay = 0;
    private static boolean isAttacking = false;
    private static boolean isInSlamCombo = false;
    private static boolean slamReturnPending = false;
    private static int slamReturnSlotQueued = -1;
    private static int slamReturnDelay = 0;
    private static int axeSwingDelay = 0;
    private static boolean axeSwingHasMace = false;
    private static boolean slamFollowUpPending = false;
    private static Entity slamFollowUpTarget = null;
    private static int slamFollowUpSlot = -1;
    private static int slamFollowUpDelay = 0;

    public static void tick(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        if (slamPending && slamTarget != null) {
            if (slamMaceDelay > 0) {
                slamMaceDelay--;
            } else {
                isAttacking = false;
                slamPending = false;
                inv.setSelectedSlot(slamMaceSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(slamMaceSlot));
                if (!isAttacking && slamTarget != null) {
                    isAttacking = true;
                    client.gameMode.attack(p, slamTarget);
                    p.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                    isAttacking = false;
                }
                slamReturnDelay = 5;
                slamReturnPending = true;
                slamTarget = null;
            }
        }

        if (slamReturnPending && --slamReturnDelay <= 0) {
            slamReturnPending = false;
            if (slamReturnSlotQueued != -1) {
                inv.setSelectedSlot(slamReturnSlotQueued);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(slamReturnSlotQueued));
            }
            slamReturnSlotQueued = -1;
        }

        if (axeSwingDelay > 0 && --axeSwingDelay == 0) {
            if (slamTarget != null && slamTarget.isAlive()) {
                if (axeSwingHasMace) {
                    int captured = slamReturnSlotQueued;
                    queueMaceSlam(slamTarget, slamMaceSlot, cfg.stunSlamReturnSlot ? captured : slamMaceSlot, 0);
                } else {
                    if (cfg.stunSlamReturnSlot) {
                        queueSlotReturn(slamReturnSlotQueued, 4);
                    }
                }
            }
            slamTarget = null;
        }

        if (slamFollowUpPending && slamFollowUpDelay > 0 && --slamFollowUpDelay == 0) {
            slamFollowUpPending = false;
            if (slamFollowUpTarget != null) {
                inv.setSelectedSlot(slamFollowUpSlot);
                if (!isAttacking) {
                    isAttacking = true;
                    client.gameMode.attack(p, slamFollowUpTarget);
                    p.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                    isAttacking = false;
                }
                slamFollowUpTarget = null;
            } else {
                inv.setSelectedSlot(slamFollowUpSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(slamFollowUpSlot));
            }
        }
    }

    public static void queueMaceSlam(Entity target, int maceSlot, int returnSlot, int delayTicks) {
        slamTarget = target;
        slamMaceSlot = maceSlot;
        slamReturnSlotQueued = returnSlot;
        slamPending = true;
        slamMaceDelay = delayTicks;
    }

    public static void queueSlotReturn(int returnSlot, int delayTicks) {
        if (returnSlot == -1) return;
        slamFollowUpSlot = returnSlot;
        slamFollowUpTarget = null;
        slamFollowUpDelay = delayTicks;
        slamFollowUpPending = true;
    }

    public static void reset() {
        slamPending = false;
        slamTarget = null;
        slamMaceDelay = 0;
        slamReturnPending = false;
        slamReturnSlotQueued = -1;
        slamReturnDelay = 0;
        isAttacking = false;
        isInSlamCombo = false;
        axeSwingDelay = 0;
        axeSwingHasMace = false;
        slamFollowUpPending = false;
        slamFollowUpTarget = null;
        slamFollowUpDelay = 0;
    }

    // --- Getters / setters used by Combat.java ---
    public static boolean isInSlamCombo() { return isInSlamCombo; }
    public static void setInSlamCombo(boolean v) { isInSlamCombo = v; }
    public static boolean isAttacking() { return isAttacking; }
    public static void setAttacking(boolean v) { isAttacking = v; }
    public static int getSlamMaceSlot() { return slamMaceSlot; }
    public static void setSlamMaceSlot(int v) { slamMaceSlot = v; }
    public static void setSlamTarget(Entity v) { slamTarget = v; }
    public static void setSlamReturnSlotQueued(int v) { slamReturnSlotQueued = v; }
    public static void setAxeSwingDelay(int v) { axeSwingDelay = v; }
    public static void setAxeSwingHasMace(boolean v) { axeSwingHasMace = v; }
}