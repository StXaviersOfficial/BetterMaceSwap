package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quick Gear Up.
 *
 * /bms misc record_inv_1 / _2 / _3 (record_inv = kit 1) -> saves every slot (empty ones too) for that kit
 * Gear Up 1/2/3 keybinds or /bms misc gearup_1 / _2 / _3 (gearup = kit 1) -> puts your items back where the record says,
 * using the best gear you currently have.
 *
 * Rules:
 *  - A recorded EMPTY slot is ignored. Whatever is in it stays. Nothing is ever cleared.
 *  - A recorded item that can't be found anywhere is skipped. That slot is left alone.
 *  - Armor slots pick the best piece you own for that slot (protection score), and any piece under 20%
 *    durability loses to a healthy one, so the second best gets chosen. If the record has an elytra on the
 *    chest, it puts the elytra back instead.
 *  - Only swap clicks (PICKUP, never a drop click). Items can't be thrown out.
 *  - Clicks per tick comes from the gearUpClicksPerTick setting (3 clicks = 1 move).
 */
public class GearUp {

    private static final float MIN_DURABILITY = 0.20F;
    private static final String FILE_NAME = "bettermaceswap_inventory.txt";

    // Slot index scheme (same as vanilla Inventory):
    // 0-8 hotbar, 9-35 main inventory, 36 boots, 37 leggings, 38 chestplate, 39 helmet, 40 offhand
    private static final int SLOTS = 41;
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    private static final ArrayDeque<Integer> clickQueue = new ArrayDeque<>();
    private static boolean running = false;
    private static int delayLeft = 0;

    private static class Record {
        final String[] id = new String[SLOTS];
        final String[] ench = new String[SLOTS];
    }

    // ------------------------------------------------------------------
    // Record
    // ------------------------------------------------------------------

    public static void record(Minecraft client) {
        record(client, 1);
    }

    public static void record(Minecraft client, int kit) {
        LocalPlayer p = client.player;
        if (p == null) return;

        List<String> lines = new ArrayList<>();
        lines.add("# BetterMaceSwap inventory record v1");
        lines.add("# slot|item|enchants  (0-8 hotbar, 9-35 inventory, 36 boots, 37 leggings, 38 chestplate, 39 helmet, 40 offhand)");
        int count = 0;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack s = getStack(p, i);
            if (s.isEmpty()) {
                lines.add(i + "|empty");
            } else {
                lines.add(i + "|" + itemId(s) + "|" + enchantSig(s));
                count++;
            }
        }
        try {
            Files.write(file(kit), lines);
        } catch (IOException e) {
            msg(p, Component.literal("Failed to save inventory record").withStyle(ChatFormatting.RED), true);
            return;
        }
        msg(p, Component.literal("Recorded " + count + " items for Gear Up " + kit).withStyle(ChatFormatting.GREEN), false);
    }

    /** Kit 1 keeps the original file name so existing records still work. */
    private static Path file(int kit) {
        String name = kit == 1 ? FILE_NAME : "bettermaceswap_inventory_" + kit + ".txt";
        return FabricLoader.getInstance().getConfigDir().resolve(name);
    }

    private static Record load(int kit) {
        Path f = file(kit);
        if (!Files.exists(f)) return null;
        Record r = new Record();
        try {
            for (String line : Files.readAllLines(f)) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;
                int idx;
                try {
                    idx = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (idx < 0 || idx >= SLOTS) continue;
                if (parts[1].trim().equals("empty") || parts[1].isBlank()) {
                    r.id[idx] = null;
                } else {
                    r.id[idx] = parts[1].trim();
                    r.ench[idx] = parts.length > 2 ? parts[2].trim() : "";
                }
            }
        } catch (IOException e) {
            return null;
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Restore (plan + execute)
    // ------------------------------------------------------------------

    public static void start(Minecraft client) {
        start(client, 1);
    }

    public static void start(Minecraft client, int kit) {
        LocalPlayer p = client.player;
        if (p == null || client.gameMode == null) return;
        if (running) return;
        if (p.containerMenu != p.inventoryMenu) {
            msg(p, "Gear Up " + kit + ": close the open container first", true);
            return;
        }

        Record rec = load(kit);
        if (rec == null) {
            msg(p, "Gear Up " + kit + ": no record found. Use /bms misc record_inv_" + kit + " first", true);
            return;
        }

        ItemStack[] cur = new ItemStack[SLOTS];
        for (int i = 0; i < SLOTS; i++) cur[i] = getStack(p, i).copy();

        List<int[]> moves = plan(rec, cur);
        if (moves.isEmpty()) {
            msg(p, "Gear Up " + kit + ": already set", false);
            return;
        }

        clickQueue.clear();
        for (int[] m : moves) {
            int a = screenSlot(m[0]);
            int b = screenSlot(m[1]);
            // pick up source, drop on destination (swaps if occupied), put leftover back on source
            clickQueue.add(a);
            clickQueue.add(b);
            clickQueue.add(a);
        }
        running = true;
        delayLeft = 0;
        msg(p, "Gear Up " + kit + ": " + moves.size() + (moves.size() == 1 ? " move" : " moves"), false);
    }

    /** Returns a list of {fromIndex, toIndex} swaps. Works on a simulated copy of the inventory. */
    private static List<int[]> plan(Record rec, ItemStack[] cur) {
        List<int[]> moves = new ArrayList<>();

        // --- Armor: best piece per slot ---
        for (int k = 0; k < 4; k++) {
            int dst = 36 + k;
            if (rec.id[dst] == null) continue; // recorded empty -> leave alone
            EquipmentSlot es = ARMOR[k];
            boolean wantElytra = rec.id[dst].contains("elytra");

            int best = -1;
            double bestScore = -1e9;
            for (int i = 0; i < SLOTS; i++) {
                if (i >= 36 && i < 40 && i != dst) continue; // other armor slots can't hold this piece
                ItemStack s = cur[i];
                if (s.isEmpty()) continue;
                double score;
                if (wantElytra) {
                    if (!itemId(s).contains("elytra")) continue;
                    float d = durability(s);
                    score = d - (d < MIN_DURABILITY ? 1000.0 : 0.0);
                } else {
                    if (!isArmorFor(s, es)) continue;
                    score = armorScore(s, es);
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = i;
                }
            }
            if (best == -1 || best == dst) continue;
            moves.add(new int[]{best, dst});
            swap(cur, best, dst);
        }

        // --- Everything else: put recorded items back in their slots ---
        for (int pass = 0; pass < 3; pass++) {
            boolean moved = false;
            for (int s = 0; s < SLOTS; s++) {
                if (s >= 36 && s < 40) continue;      // armor done above
                if (rec.id[s] == null) continue;       // recorded empty -> ignore, never clear
                if (!cur[s].isEmpty() && itemId(cur[s]).equals(rec.id[s])) continue; // already there

                int src = -1;
                double bestScore = -1e9;
                for (int c = 0; c < SLOTS; c++) {
                    if (c == s || (c >= 36 && c < 40)) continue;
                    if (cur[c].isEmpty() || !itemId(cur[c]).equals(rec.id[s])) continue;
                    // don't steal from a slot that already holds its own recorded item
                    if (rec.id[c] != null && itemId(cur[c]).equals(rec.id[c])) continue;
                    double score = (enchantSig(cur[c]).equals(rec.ench[s]) ? 10.0 : 0.0) + durability(cur[c]);
                    if (score > bestScore) {
                        bestScore = score;
                        src = c;
                    }
                }
                if (src == -1) continue; // item not found anywhere -> leave slot alone
                moves.add(new int[]{src, s});
                swap(cur, src, s);
                moved = true;
            }
            if (!moved) break;
        }
        return moves;
    }

    public static void tick(Minecraft client) {
        if (!running) return;
        LocalPlayer p = client.player;
        if (p == null || client.gameMode == null || !p.isAlive()
                || p.containerMenu != p.inventoryMenu) {
            clickQueue.clear();
            running = false;
            return;
        }

        // Delay between click batches (gearUpDelayTicks, 0 = none)
        if (delayLeft > 0) {
            delayLeft--;
            return;
        }

        int clicksPerTick = Math.max(1, ModConfig.get().gearUpClicksPerTick); // 3 clicks = 1 move
        for (int n = 0; n < clicksPerTick && !clickQueue.isEmpty(); n++) {
            int slot = clickQueue.poll();
            client.gameMode.handleContainerInput(p.inventoryMenu.containerId, slot, 0, ContainerInput.PICKUP, p);
        }

        if (!clickQueue.isEmpty()) {
            delayLeft = ModConfig.get().gearUpDelayTicks;
        }

        if (clickQueue.isEmpty()) {
            cleanupCursor(client, p);
            running = false;
            msg(p, "Gear Up: done", false);
        }
    }

    /** If a click failed and left an item on the cursor, put it in an empty slot. Never drops it. */
    private static void cleanupCursor(Minecraft client, LocalPlayer p) {
        if (p.inventoryMenu.getCarried().isEmpty()) return;
        for (int id = 9; id <= 44; id++) {
            if (p.inventoryMenu.getSlot(id).getItem().isEmpty()) {
                client.gameMode.handleContainerInput(p.inventoryMenu.containerId, id, 0, ContainerInput.PICKUP, p);
                return;
            }
        }
    }

    public static void reset() {
        clickQueue.clear();
        running = false;
        delayLeft = 0;
    }

    public static boolean isRunning() {
        return running;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void msg(LocalPlayer p, String text, boolean always) {
        msg(p, Component.literal(text), always);
    }

    private static void msg(LocalPlayer p, Component text, boolean always) {
        if (always || ModConfig.get().showActionBarMessages) {
            Minecraft.getInstance().gui.hud.setOverlayMessage(text, false);
        }
    }

    private static void swap(ItemStack[] a, int i, int j) {
        ItemStack t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    private static ItemStack getStack(LocalPlayer p, int idx) {
        if (idx < 36) return p.getInventory().getItem(idx);
        return switch (idx) {
            case 36 -> p.getItemBySlot(EquipmentSlot.FEET);
            case 37 -> p.getItemBySlot(EquipmentSlot.LEGS);
            case 38 -> p.getItemBySlot(EquipmentSlot.CHEST);
            case 39 -> p.getItemBySlot(EquipmentSlot.HEAD);
            default -> p.getItemBySlot(EquipmentSlot.OFFHAND);
        };
    }

    /** Inventory index -> PlayerScreenHandler slot id. */
    private static int screenSlot(int idx) {
        if (idx < 9) return 36 + idx;   // hotbar
        if (idx < 36) return idx;       // main inventory
        return switch (idx) {
            case 36 -> 8;               // boots
            case 37 -> 7;               // leggings
            case 38 -> 6;               // chestplate
            case 39 -> 5;               // helmet
            default -> 45;              // offhand
        };
    }

    private static String itemId(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
    }

    private static float durability(ItemStack s) {
        if (!s.isDamageableItem() || s.getMaxDamage() <= 0) return 1.0F;
        return (s.getMaxDamage() - s.getDamageValue()) / (float) s.getMaxDamage();
    }

    private static String enchantSig(ItemStack s) {
        ItemEnchantments comp = s.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        List<String> parts = new ArrayList<>();
        for (Holder<Enchantment> e : comp.keySet()) {
            String path = enchantPath(e);
            parts.add(path + ":" + comp.getLevel(e));
        }
        Collections.sort(parts);
        return String.join(",", parts);
    }

    /** "minecraft:protection" -> "protection" (registry path, same as the old Yarn getValue().getPath()). */
    private static String enchantPath(Holder<Enchantment> e) {
        String id = e.getRegisteredName();
        int i = id.indexOf(':');
        return i >= 0 ? id.substring(i + 1) : id;
    }

    private static int enchantLevel(ItemStack s, String name) {
        ItemEnchantments comp = s.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> e : comp.keySet()) {
            if (enchantPath(e).equals(name)) {
                return comp.getLevel(e);
            }
        }
        return 0;
    }

    private static boolean isArmorFor(ItemStack s, EquipmentSlot slot) {
        String path = BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
        return switch (slot) {
            case HEAD -> path.endsWith("_helmet");
            case CHEST -> path.endsWith("_chestplate");
            case LEGS -> path.endsWith("_leggings");
            case FEET -> path.endsWith("_boots");
            default -> false;
        };
    }

    /**
     * Higher = better. Defense points + protection enchants, so iron with Prot IV beats weak diamond.
     * Under 20% durability gets a huge penalty so a healthy piece (second best) wins instead.
     */
    private static double armorScore(ItemStack s, EquipmentSlot slot) {
        String path = BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
        int k = switch (slot) { // HEAD, CHEST, LEGS, FEET
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            default -> 3;
        };

        int[] defense;
        double toughness = 0.0;
        double knockbackBonus = 0.0;
        if (path.startsWith("netherite")) {
            defense = new int[]{3, 8, 6, 3};
            toughness = 3.0;
            knockbackBonus = 0.5;
        } else if (path.startsWith("diamond")) {
            defense = new int[]{3, 8, 6, 3};
            toughness = 2.0;
        } else if (path.startsWith("iron")) {
            defense = new int[]{2, 6, 5, 2};
        } else if (path.startsWith("chainmail")) {
            defense = new int[]{2, 5, 4, 1};
        } else if (path.startsWith("golden")) {
            defense = new int[]{2, 5, 3, 1};
        } else if (path.startsWith("leather")) {
            defense = new int[]{1, 3, 2, 1};
        } else if (path.startsWith("turtle")) {
            defense = new int[]{2, 0, 0, 0};
        } else {
            defense = new int[]{1, 1, 1, 1}; // unknown / modded armor
        }

        double score = defense[k] + toughness * 0.25 + knockbackBonus;
        score += enchantLevel(s, "protection") * 1.0;
        score += enchantLevel(s, "blast_protection") * 0.8;
        score += enchantLevel(s, "projectile_protection") * 0.6;
        score += enchantLevel(s, "fire_protection") * 0.4;

        float d = durability(s);
        score += d * 0.01; // tiny tiebreak for healthier piece
        if (d < MIN_DURABILITY) score -= 1000.0;
        return score;
    }
}
