package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public class Misc {
    // --- Lunge Swap (Misc category) ---
    private static boolean lungeSwapPending = false;
    private static int lungeReturnSlot = -1;
    private static int lungeSwapDelay = 0;

    // --- Wind Charge shared state (Misc category) ---
    static int windFireDelay = 0;
    static int windSlot = -1;
    static int windReturnSlot = -1;
    private static boolean wasUsePressed = false;
    private static int windActionCooldown = 0;
    private static int windReturnDelay = 0;

    // --- Auto Jump Wind Charge (Misc category) ---
    private static int jumpDelay = 0;

    // --- Air Pots (Misc category) ---
    private static boolean airPotThrown = false;
    private static int airPotWindDelay = 0;

    // --- Elytra Boost (Misc category) ---
    private static boolean elytraBoostArmed = false;
    private static int elytraBoostCooldown = 0;
    private static int elytraBoostFiredMessageClear = 0;
    private static int alienWcRedirectDelay = 0;
    private static float alienWcRedirectAnglePending = 0f;

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        // Elytra boost armed HUD message
        if (elytraBoostArmed && p.isOnGround()) {
            elytraBoostArmed = false;
        } else if (elytraBoostArmed && cfg.showActionBarMessages) {
            p.sendMessage(net.minecraft.text.Text.literal("Elytra Boost: ")
                    .append(net.minecraft.text.Text.literal("ARMED")
                            .formatted(net.minecraft.util.Formatting.GREEN)),
                    true);
        }

        if (elytraBoostFiredMessageClear > 0 && --elytraBoostFiredMessageClear == 0) {
            if (cfg.showActionBarMessages)
                p.sendMessage(net.minecraft.text.Text.literal(""), true);
        }

        // Alien WC redirect
        if (alienWcRedirectDelay > 0 && --alienWcRedirectDelay == 0) {
            p.setPitch(alienWcRedirectAnglePending);
            if (client.getNetworkHandler() != null)
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        p.getYaw(), alienWcRedirectAnglePending, p.isOnGround(), false));
        }

        // Lunge Swap
        if (lungeSwapPending) {
            if (lungeSwapDelay > 0) {
                lungeSwapDelay--;
            } else {
                lungeSwapPending = false;
                if (lungeReturnSlot != -1) {
                    inv.setSelectedSlot(lungeReturnSlot);
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(lungeReturnSlot));
                    lungeReturnSlot = -1;
                }
            }
        }

        // Auto Jump Wind Charge (Misc category)
        if (jumpDelay > 0 && --jumpDelay == 0)
            p.jump();

        if (cfg.autoJumpWindCharge && p.isOnGround() && windActionCooldown <= 0 && p.getPitch() > 70.0F) {
            int wcSlot = Combat.findItem(p, "wind_charge");
            if (wcSlot != -1 && client.options.useKey.isPressed()) {
                ItemStack wc = p.getInventory().getStack(wcSlot);
                if (!Combat.isValidWindItem(wc)) return;
                ItemStack held = p.getMainHandStack();
                if (Combat.isSword(held) || Combat.isAxe(held) || held.isOf(Items.MACE)) {
                    int delay = 3 + (int) (Math.random() * 2);
                    windReturnSlot = inv.getSelectedSlot();
                    windSlot = wcSlot;
                    inv.setSelectedSlot(wcSlot);
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(wcSlot));
                    windActionCooldown = 3;
                    windFireDelay = delay;
                    jumpDelay = delay;
                }
            }
        }

        // Wind on right click (Misc category)
        if (cfg.windOnRightClickEnabled)
            tickWindRightClick(p, client, inv, cfg);

        // Wind fire delay
        if (windFireDelay > 0 && --windFireDelay == 0)
            fireWindCharge(client);

        // Wind return delay
        if (windReturnDelay > 0 && --windReturnDelay == 0) {
            if (windReturnSlot != -1) {
                inv.setSelectedSlot(windReturnSlot);
                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(windReturnSlot));
                windReturnSlot = -1;
            }
        }

        // Air Pots (Misc category)
        if (airPotThrown && --airPotWindDelay <= 0) {
            airPotThrown = false;
            fireAirPotWindCharge(client);
        }

        // Wind action cooldown
        if (windActionCooldown > 0) windActionCooldown--;

        // Elytra Boost (Misc category)
        tickElytraBoost(client);
    }

    private static void tickWindRightClick(ClientPlayerEntity p, MinecraftClient mc, InventoryAccessor inv,
            ModConfig cfg) {
        boolean usePressed = mc.options.useKey.isPressed();
        boolean justPressed = usePressed && !wasUsePressed;
        wasUsePressed = usePressed;
        if (justPressed && windActionCooldown <= 0) {
            ItemStack offHandStack = p.getOffHandStack();
            if (!offHandStack.isEmpty() && offHandStack.getItem().getMaxUseTime(offHandStack, p) > 0) {
                return;
            }
            ItemStack held = p.getMainHandStack();
            if (Combat.isSword(held) || Combat.isAxe(held) || held.isOf(Items.MACE)) {
                int wcSlot = Combat.findItem(p, "wind_charge");
                int fwSlot = Combat.findItem(p, "firework_rocket");
                boolean hasOffhandWC = p.getOffHandStack().isOf(Items.WIND_CHARGE);

                if (hasOffhandWC) {
                    windReturnSlot = -1;
                    windSlot = -1;
                    windActionCooldown = 2;
                    fireWindCharge(mc);
                } else if (wcSlot != -1 && fwSlot != -1 && cfg.rocketBoostEnabled) {
                    int cur = inv.getSelectedSlot();
                    int chosen = (Math.abs(wcSlot - cur) <= Math.abs(fwSlot - cur)) ? wcSlot : fwSlot;
                    windReturnSlot = cur;
                    windSlot = chosen;
                    inv.setSelectedSlot(chosen);
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(chosen));
                    windActionCooldown = 3;
                    windFireDelay = 3 + (int) (Math.random() * 2);
                } else if (wcSlot != -1) {
                    ItemStack wc = p.getInventory().getStack(wcSlot);
                    if (!Combat.isValidWindItem(wc)) return;
                    windReturnSlot = inv.getSelectedSlot();
                    windSlot = wcSlot;
                    inv.setSelectedSlot(wcSlot);
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(wcSlot));
                    windActionCooldown = 3;
                    windFireDelay = 1 + (int) (Math.random() * 2);
                } else if (fwSlot != -1 && cfg.rocketBoostEnabled) {
                    ItemStack fw = p.getInventory().getStack(fwSlot);
                    if (!Combat.isValidWindItem(fw)) return;
                    windReturnSlot = inv.getSelectedSlot();
                    windSlot = fwSlot;
                    inv.setSelectedSlot(fwSlot);
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(fwSlot));
                    windActionCooldown = 3;
                    windFireDelay = 3 + (int) (Math.random() * 2);
                }
            }
        }
    }

    public static void fireWindCharge(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;
        Hand fireHand = (windSlot == -1) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        client.interactionManager.interactItem(p, fireHand);
        p.swingHand(fireHand);
        float savedYaw = p.getYaw();
        boolean pearlFire = Pearl.isPearlCatchFire();
        Pearl.setPearlCatchFire(false);
        windSlot = -1;
        Pearl.cameraLocked = false;
        windReturnDelay = 1;
        if (pearlFire && ModConfig.get().pearlCatchReturnAngle) {
            float returnPitch = -(float) ModConfig.get().pearlReturnAngle;
            p.setYaw(savedYaw);
            p.setPitch(returnPitch);
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        savedYaw, returnPitch, p.isOnGround(), false));
            }
        }
    }

    private static void fireAirPotWindCharge(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;
        if (p.getOffHandStack().isOf(Items.WIND_CHARGE)) {
            client.interactionManager.interactItem(p, Hand.OFF_HAND);
            p.swingHand(Hand.OFF_HAND);
            return;
        }
        int slot = Combat.findItem(p, "wind_charge");
        if (slot == -1) return;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        int returnSlot = inv.getSelectedSlot();
        inv.setSelectedSlot(slot);
        client.interactionManager.interactItem(p, Hand.MAIN_HAND);
        p.swingHand(Hand.MAIN_HAND);
        client.execute(() -> inv.setSelectedSlot(returnSlot));
    }

    public static void tickElytraBoost(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null || !elytraBoostArmed || !ModConfig.get().elytraBoostEnabled) return;
        if (!p.isGliding()) return;

        net.minecraft.util.math.Vec3d vel = p.getVelocity();
        double totalSpeed = vel.length();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (totalSpeed < 0.173 || hSpeed < 0.27) return;

        if (client.world == null) return;
        float yaw = p.getYaw();
        boolean[] existBlock = new boolean[3];
        for (int i = 0; i < 3; i++) {
            double rad = Math.toRadians(yaw + (-45 + i * 45));
            double offsetX = -Math.sin(rad);
            double offsetZ = Math.cos(rad);
            net.minecraft.util.math.BlockPos blockPos = new net.minecraft.util.math.BlockPos(
                    (int) Math.floor(p.getX() + offsetX),
                    (int) Math.floor(p.getY() - 1),
                    (int) Math.floor(p.getZ() + offsetZ));
            net.minecraft.block.BlockState state = client.world.getBlockState(blockPos);
            existBlock[i] = !state.isAir()
                    && (state.isOpaque()
                            || state.isSolidBlock(client.world, blockPos)
                            || !state.getCollisionShape(client.world, blockPos).isEmpty());
        }
        if (!existBlock[1]) return;

        int wcSlot = Combat.findItem(p, "wind_charge");
        boolean offhand = wcSlot == -1 && p.getOffHandStack().isOf(Items.WIND_CHARGE);
        if (wcSlot == -1 && !offhand) return;

        net.minecraft.util.math.Vec3d velv = p.getVelocity();
        double vy = -velv.y;
        double y = p.getY();
        int tick = (vy != 0.0) ? (int) Math.ceil((y - Math.floor(y)) / vy) : 0;
        int boostThrow = (tick <= 3) ? 5 : 4;
        int boostJump = (tick <= 3) ? 4 : 3;

        ModConfig cfg = ModConfig.get();
        if (!offhand) {
            InventoryAccessor inv = (InventoryAccessor) p.getInventory();
            windReturnSlot = inv.getSelectedSlot();
            windSlot = wcSlot;
            inv.setSelectedSlot(wcSlot);
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(wcSlot));
        } else {
            windSlot = -1;
            windReturnSlot = -1;
        }
        float fireAngle = 90.0f;
        p.setPitch(fireAngle);
        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    p.getYaw(), fireAngle, p.isOnGround(), false));
        Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
        client.interactionManager.interactItem(p, hand);
        p.swingHand(hand);
        alienWcRedirectAnglePending = (float) cfg.elytraBoostRedirectAngle;
        alienWcRedirectDelay = cfg.elytraBoostRedirectTicks;
        windFireDelay = boostThrow;
        jumpDelay = boostJump;
        elytraBoostArmed = false;
        elytraBoostFiredMessageClear = 20;
    }

    public static void onSplashPotThrown(ClientPlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.airPotsEnabled) return;
        if (player.isOnGround()) return;
        airPotThrown = true;
        airPotWindDelay = 2;
    }

    public static void armElytraBoost() {
        elytraBoostArmed = true;
    }

    // --- Lunge Swap public API ---
    public static boolean isLungeSwapPending() { return lungeSwapPending; }
    public static int getLungeReturnSlot() { return lungeReturnSlot; }

    public static void startLungeSwap(int returnSlot) {
        lungeReturnSlot = returnSlot;
        lungeSwapPending = true;
        lungeSwapDelay = ModConfig.get().lungeSwapDelay;
    }

    /** Pickaxe / shovel / hoe: never lunge-swap while holding a mining tool. */
    private static boolean isMiningTool(ItemStack s) {
        String path = net.minecraft.registry.Registries.ITEM.getId(s.getItem()).getPath();
        return path.endsWith("_pickaxe") || path.endsWith("_shovel") || path.endsWith("_hoe");
    }

    /** True if an entity is under the crosshair (within reach). */
    private static boolean crosshairOnEntity(MinecraftClient client) {
        return client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult
                || client.targetedEntity != null;
    }

    private static boolean tryLungeSwap(ClientPlayerEntity p) {
        ModConfig cfg = ModConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        if (!cfg.lungeSwapEnabled || lungeSwapPending) return false;
        // Crosshair on an entity in reach -> normal hit, no lunge swap
        if (crosshairOnEntity(client)) return false;
        ItemStack held = p.getMainHandStack();
        if (isMiningTool(held)) return false;
        if (Combat.isSword(held) || Combat.isAxe(held) || held.isOf(Items.MACE) || Combat.isSpear(held)) return false;
        int spearSlot = Combat.findSpear(p);
        if (spearSlot == -1 || client.getNetworkHandler() == null) return false;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        int returnSlot = inv.getSelectedSlot();
        inv.setSelectedSlot(spearSlot);
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(spearSlot));
        startLungeSwap(returnSlot);
        return true;
    }

    public static void onBlockHit(ClientPlayerEntity p) {
        tryLungeSwap(p);
    }

    public static void onAttack(ClientPlayerEntity player) {
        tryLungeSwap(player);
    }

    // --- Wind state setters (used by Pearl.java) ---
    public static void setWindSlot(int v) { windSlot = v; }
    public static void setWindReturnSlot(int v) { windReturnSlot = v; }
    public static void setWindFireDelay(int v) { windFireDelay = v; }

    public static void reset() {
        elytraBoostArmed = false;
        alienWcRedirectDelay = 0;
        alienWcRedirectAnglePending = 0f;
        windFireDelay = 0;
        windSlot = -1;
        windReturnSlot = -1;
        windActionCooldown = 0;
        windReturnDelay = 0;
        lungeSwapPending = false;
        lungeReturnSlot = -1;
        lungeSwapDelay = 0;
        airPotThrown = false;
        airPotWindDelay = 0;
        jumpDelay = 0;
        wasUsePressed = false;
    }
}
