package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class Pearl {
    private static boolean pearlThrown = false;
    private static float pearlSavedYaw = 0f;
    private static int pearlWindDelay = 0;
    private static boolean pearlRedirecting = false;
    private static boolean pearlExpMode = false;
    private static boolean pearlExpDropDetected = false;
    private static Vec3d pearlSpawnPos = null;
    private static Vec3d pearlInitialVel = null;
    private static int pearlSimTick = 0;
    private static int pearlCatchTimer = 0;
    private static int pearlReturnSlot = -1;
    static boolean cameraLocked = false;
    static boolean isPearlCatchFire = false;

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        if (pearlThrown && --pearlCatchTimer <= 0) {
            pearlThrown = false;
            int slot = Combat.findItem(p, "wind_charge");
            boolean offhand = slot == -1 && p.getOffHandStack().isOf(Items.WIND_CHARGE);
            if ((slot != -1 || offhand) && cameraLocked) {
                Misc.setWindSlot(offhand ? -1 : slot);
                Misc.setWindReturnSlot(pearlReturnSlot);
                if (!offhand) {
                    inv.setSelectedSlot(slot);
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
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
                Vec3d eyePos = p.getEyePos();

                if (!pearlExpDropDetected) {
                    Vec3d catchTarget = null;
                    for (int t = 1; t <= 5; t++) {
                        Vec3d simPos = simulatePearlPos(pearlSpawnPos, pearlInitialVel, t);
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
                        Vec3d biasedTarget;
                        if (!p.isSprinting()) {
                            biasedTarget = catchTarget.add(0.0, 0.05, 0.0);
                        } else {
                            biasedTarget = catchTarget.add(0.0, 0.08, 0.0);
                        }
                        Vec3d dir = biasedTarget.subtract(eyePos).normalize();
                        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
                        float pitch = (float) Math.toDegrees(-Math.asin(dir.y));

                        float correctedPitch;
                        if (!p.isSprinting() && p.isGliding()) {
                            if (!(p.getPitch() < -75.0F)) {
                                if (p.isSneaking()) {
                                    correctedPitch = pitch + 4.0F;
                                } else if (p.getVelocity().y > 0.0F) {
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

                        p.setPitch(correctedPitch);
                        if (client.getNetworkHandler() != null) {
                            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                                    p.getYaw(), correctedPitch, p.isOnGround(), false));
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
                    boolean offhand = slot == -1 && p.getOffHandStack().isOf(Items.WIND_CHARGE);
                    if (slot != -1 || offhand) {
                        if (!offhand) {
                            inv.setSelectedSlot(slot);
                            if (client.getNetworkHandler() != null)
                                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                        }
                        Misc.setWindSlot(offhand ? -1 : slot);
                        Misc.setWindReturnSlot(pearlReturnSlot);
                        isPearlCatchFire = true;
                        Misc.fireWindCharge(client);
                    } else {
                        if (cfg.pearlCatchReturnAngle) {
                            float returnPitch = -(float) cfg.pearlReturnAngle;
                            p.setPitch(returnPitch);
                            if (client.getNetworkHandler() != null)
                                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                                        p.getYaw(), returnPitch, p.isOnGround(), false));
                        }
                    }
                }
            } else {
                p.setPitch(-90f);
                p.setYaw(pearlSavedYaw);
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                            pearlSavedYaw, -90f, p.isOnGround(), false));
                }
                if (pearlWindDelay > 0) {
                    pearlWindDelay--;
                } else {
                    pearlRedirecting = false;
                    int slot = Combat.findItem(p, "wind_charge");
                    boolean offhand = slot == -1 && p.getOffHandStack().isOf(Items.WIND_CHARGE);
                    if (slot != -1 || offhand) {
                        if (!offhand) {
                            inv.setSelectedSlot(slot);
                            if (client.getNetworkHandler() != null)
                                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                        }
                        Misc.setWindSlot(offhand ? -1 : slot);
                        Misc.setWindReturnSlot(pearlReturnSlot);
                        isPearlCatchFire = true;
                        Misc.fireWindCharge(client);
                    } else {
                        if (cfg.pearlCatchReturnAngle) {
                            float returnPitch = -(float) cfg.pearlReturnAngle;
                            p.setPitch(returnPitch);
                            if (client.getNetworkHandler() != null) {
                                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                                        pearlSavedYaw, returnPitch, p.isOnGround(), false));
                            }
                        }
                    }
                }
            }
        }

        if (cameraLocked)
            p.setPitch(-90.0F);
    }

    public static void onPearlThrown(ClientPlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.pearlCatchEnabled) return;
        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        pearlReturnSlot = inv.getSelectedSlot();
        pearlSavedYaw = player.getYaw();
        pearlRedirecting = true;
        if (cfg.experimentalPearlCatch && player.getPitch() > -80.0F) {
            pearlExpMode = true;
            pearlExpDropDetected = false;
            pearlSimTick = 0;
            pearlSpawnPos = player.getEyePos();
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d playerVel = player.getVelocity();
            pearlInitialVel = look.multiply(1.5).add(playerVel);
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
                    || player.getOffHandStack().isOf(Items.WIND_CHARGE);
            if (hasWindCharge && player.getPitch() <= -60.0F) {
                player.setPitch(-90.0F);
                cameraLocked = true;
            }
        }
    }

    private static Vec3d simulatePearlPos(Vec3d spawnPos, Vec3d initialVel, int ticksAfterSpawn) {
        Vec3d pos = spawnPos;
        Vec3d vel = initialVel;
        for (int i = 0; i < ticksAfterSpawn; i++) {
            pos = pos.add(vel);
            vel = vel.add(0, -0.03, 0).multiply(0.99);
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
