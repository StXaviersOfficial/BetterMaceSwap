package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.phys.Vec3;

public class Pearl {
    private static boolean pearlThrown = false;
    private static float pearlSavedYaw = 0f;
    private static int pearlWindDelay = 0;
    private static boolean pearlRedirecting = false;
    private static boolean pearlExpMode = false;
    private static boolean pearlExpDropDetected = false;
    private static Vec3 pearlSpawnPos = null;
    private static Vec3 pearlInitialVel = null;
    private static int pearlSimTick = 0;
    private static int pearlCatchTimer = 0;
    private static int pearlReturnSlot = -1;
    static boolean cameraLocked = false;
    static boolean isPearlCatchFire = false;

    public static void tick(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        if (pearlThrown && --pearlCatchTimer <= 0) {
            pearlThrown = false;
            int slot = Combat.findItem(p, "wind_charge");
            boolean offhand = slot == -1 && p.getOffhandItem().is(Items.WIND_CHARGE);
            if ((slot != -1 || offhand) && cameraLocked) {
                Misc.setWindSlot(offhand ? -1 : slot);
                Misc.setWindReturnSlot(pearlReturnSlot);
                if (!offhand) {
                    inv.setSelectedSlot(slot);
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
                }
                isPearlCatchFire = true;
                Misc.setWindFireDelay(2);
            } else {
                cameraLocked = false;
                Misc.setWindSlot(-1);
                Misc.setWindReturnSlot(-1);
                Misc.setWindFireDelay(0);
            }
        }

        if (pearlRedirecting) {
            if (pearlExpMode) {
                Vec3 eyePos = p.getEyePosition();

                if (!pearlExpDropDetected) {
                    Vec3 catchTarget = null;
                    for (int t = 1; t <= 5; t++) {
                        Vec3 simPos = simulatePearlPos(pearlSpawnPos, pearlInitialVel, t);
                        if (eyePos.distanceTo(simPos) <= 3.1) {
                            catchTarget = simPos;
                            break;
                        }
                    }

                    if (catchTarget == null) {
                        pearlRedirecting = false;
                        pearlExpMode = false;
                        pearlSimTick = 0;
                        pearlSpawnPos = null;
                        pearlInitialVel = null;
                    } else {
                        Vec3 biasedTarget;
                        if (!p.isSprinting()) {
                            biasedTarget = catchTarget.add(0.0, 0.05, 0.0);
                        } else {
                            biasedTarget = catchTarget.add(0.0, 0.08, 0.0);
                        }
                        Vec3 dir = biasedTarget.subtract(eyePos).normalize();
                        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
                        float pitch = (float) Math.toDegrees(-Math.asin(dir.y));

                        float correctedPitch;
                        if (!p.isSprinting() && p.isFallFlying()) {
                            if (!(p.getXRot() < -75.0F)) {
                                if (p.isShiftKeyDown()) {
                                    correctedPitch = pitch + 4.0F;
                                } else if (p.getDeltaMovement().y > 0.0F) {
                                    correctedPitch = pitch + 4.0F;
                                } else {
                                    correctedPitch = pitch + 4.0F;
                                }
                            } else {
                                correctedPitch = pitch + 4.0F;
                            }
                        } else {
                            correctedPitch = pitch + 4.0F;
                        }

                        p.setXRot(correctedPitch);
                        if (client.getConnection() != null) {
                            client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                    p.getYRot(), correctedPitch, p.onGround(), false));
                        }
                        pearlExpDropDetected = true;
                    }
                } else {
                    pearlRedirecting = false;
                    pearlExpMode = false;
                    pearlExpDropDetected = false;
                    pearlSimTick = 0;
                    pearlSpawnPos = null;
                    pearlInitialVel = null;
                    int slot = Combat.findItem(p, "wind_charge");
                    boolean offhand = slot == -1 && p.getOffhandItem().is(Items.WIND_CHARGE);
                    if (slot != -1 || offhand) {
                        if (!offhand) {
                            inv.setSelectedSlot(slot);
                            if (client.getConnection() != null)
                                client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
                        }
                        Misc.setWindSlot(offhand ? -1 : slot);
                        Misc.setWindReturnSlot(pearlReturnSlot);
                        isPearlCatchFire = true;
                        Misc.fireWindCharge(client);
                    } else {
                        if (cfg.pearlCatchReturnAngle) {
                            float returnPitch = -(float) cfg.pearlReturnAngle;
                            p.setXRot(returnPitch);
                            if (client.getConnection() != null)
                                client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                        p.getYRot(), returnPitch, p.onGround(), false));
                        }
                    }
                }
            } else {
                p.setXRot(-90f);
                p.setYRot(pearlSavedYaw);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            pearlSavedYaw, -90f, p.onGround(), false));
                }
                if (pearlWindDelay > 0) {
                    pearlWindDelay--;
                } else {
                    pearlRedirecting = false;
                    int slot = Combat.findItem(p, "wind_charge");
                    boolean offhand = slot == -1 && p.getOffhandItem().is(Items.WIND_CHARGE);
                    if (slot != -1 || offhand) {
                        if (!offhand) {
                            inv.setSelectedSlot(slot);
                            if (client.getConnection() != null)
                                client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
                        }
                        Misc.setWindSlot(offhand ? -1 : slot);
                        Misc.setWindReturnSlot(pearlReturnSlot);
                        isPearlCatchFire = true;
                        Misc.fireWindCharge(client);
                    } else {
                        if (cfg.pearlCatchReturnAngle) {
                            float returnPitch = -(float) cfg.pearlReturnAngle;
                            p.setXRot(returnPitch);
                            if (client.getConnection() != null) {
                                client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                        pearlSavedYaw, returnPitch, p.onGround(), false));
                            }
                        }
                    }
                }
            }
        }

        if (cameraLocked)
            p.setXRot(-90.0F);
    }

    public static void onPearlThrown(LocalPlayer player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.pearlCatchEnabled) return;
        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        pearlReturnSlot = inv.getSelectedSlot();
        pearlSavedYaw = player.getYRot();
        pearlRedirecting = true;
        if (cfg.experimentalPearlCatch && player.getXRot() > -80.0F) {
            pearlExpMode = true;
            pearlExpDropDetected = false;
            pearlSimTick = 0;
            pearlSpawnPos = player.getEyePosition();
            Vec3 look = player.getViewVector(1.0F);
            Vec3 playerVel = player.getDeltaMovement();
            pearlInitialVel = look.scale(1.5).add(playerVel);
        } else {
            pearlExpMode = false;
            pearlRedirecting = false;
            pearlThrown = true;
            pearlCatchTimer = cfg.pearlCatchDelayTicks;
            pearlReturnSlot = switch (cfg.pearlReturnMode) {
                case SWORD -> Combat.findItem(player, "sword");
                case AXE -> Combat.findItem(player, "_axe");
                case ELYTRA -> Combat.findItem(player, "elytra");
                default -> Combat.getLastSelectedSlot() != -1 ? Combat.getLastSelectedSlot() : inv.getSelectedSlot();
            };
            if (pearlReturnSlot == -1)
                pearlReturnSlot = Combat.getLastSelectedSlot() != -1 ? Combat.getLastSelectedSlot() : inv.getSelectedSlot();
            boolean hasWindCharge = Combat.findItem(player, "wind_charge") != -1
                    || player.getOffhandItem().is(Items.WIND_CHARGE);
            if (hasWindCharge && player.getXRot() <= -60.0F) {
                player.setXRot(-90.0F);
                cameraLocked = true;
            }
        }
    }

    private static Vec3 simulatePearlPos(Vec3 spawnPos, Vec3 initialVel, int ticksAfterSpawn) {
        Vec3 pos = spawnPos;
        Vec3 vel = initialVel;
        for (int i = 0; i < ticksAfterSpawn; i++) {
            pos = pos.add(vel);
            vel = vel.add(0, -0.03, 0).scale(0.99);
        }
        return pos;
    }

    public static void reset() {
        pearlRedirecting = false;
        pearlExpMode = false;
        pearlExpDropDetected = false;
        pearlWindDelay = 0;
        pearlSimTick = 0;
        pearlSpawnPos = null;
        pearlInitialVel = null;
        pearlSavedYaw = 0f;
        pearlThrown = false;
        pearlCatchTimer = 0;
        pearlReturnSlot = -1;
        cameraLocked = false;
        isPearlCatchFire = false;
    }

    public static boolean isCameraLocked() { return cameraLocked; }
    public static boolean isPearlCatchFire() { return isPearlCatchFire; }
    public static void setPearlCatchFire(boolean v) { isPearlCatchFire = v; }
}
