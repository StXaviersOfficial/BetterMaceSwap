package com.ritesh.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Config(name = "bettermaceswap")
@Environment(EnvType.CLIENT)
public class ModConfig implements ConfigData {
    @Category("Combat")
    @Tooltip
    public boolean autoBreachSwapEnabled = true;
    @Category("Combat")
    @Tooltip
    public boolean smartSwitchEnabled = false;
    @Category("Combat")
    @Tooltip
    @BoundedDiscrete(min = 1, max = 5)
    public int breachSwapBackTicks = 3;
    @Category("Combat")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public BreachSwapTriggerMode breachSwapTriggerMode = BreachSwapTriggerMode.WEAPONS;
    @Category("Combat")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public MaceEnchantMode maceMode = MaceEnchantMode.DENSITY;
    @Category("Combat")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AttributeSwapMode attributeSwapMode = AttributeSwapMode.ALL;
    @Category("Combat")
    @Tooltip
    public boolean lungeSwapEnabled = false;
    @Category("Combat")
    @Tooltip
    @BoundedDiscrete(min = 0, max = 3)
    public int lungeSwapDelay = 2;

    @Category("Targets")
    @Tooltip
    public boolean combatTargetMobs = true;
    @Category("Targets")
    @Tooltip
    public boolean combatTargetPlayers = true;
    @Category("Targets")
    @Tooltip
    public boolean aimTargetMobs = true;
    @Category("Targets")
    @Tooltip
    public boolean aimTargetPlayers = true;

    @Category("Stun Slam")
    @Tooltip
    public boolean autoStunSlamEnabled = true;
    @Category("Stun Slam")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public StunSlamTriggerMode stunSlamTriggerMode = StunSlamTriggerMode.WEAPONS;
    @Category("Stun Slam")
    @Tooltip
    public boolean stunSlamReturnSlot = true;
    @Category("Stun Slam")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public StunSlamMode stunSlamMode = StunSlamMode.NORMAL;
    @Category("Stun Slam")
    @Tooltip
    @BoundedDiscrete(min = 0, max = 3)
    public int noMaceAxeHitDelay = 3;

    @Category("Pearl")
    @Tooltip
    public boolean pearlCatchEnabled = true;
    @Category("Pearl")
    @Tooltip
    @BoundedDiscrete(min = 1, max = 5)
    public int pearlCatchDelayTicks = 2;
    @Category("Pearl")
    @Tooltip
    @BoundedDiscrete(min = 60, max = 90)
    public int pearlCatchAngle = 60;
    @Category("Pearl")
    @Tooltip
    public boolean pearlCatchReturnAngle = true;
    @Category("Pearl")
    @Tooltip
    @BoundedDiscrete(min = -90, max = 90)
    public int pearlReturnAngle = 40;
    @Category("Pearl")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public ReturnSlotMode pearlReturnMode = ReturnSlotMode.PREVIOUS;
    @Category("Pearl")
    @Tooltip
    public boolean experimentalPearlCatch = false;

    @Category("Aim Assist")
    @Tooltip
    public boolean aimAssistEnabled = false;
    @Category("Aim Assist")
    @Tooltip
    @BoundedDiscrete(min = 20, max = 100)
    public int aimSpeed = 15;
    @Category("Aim Assist")
    @Tooltip
    @BoundedDiscrete(min = 1, max = 25)
    public int aimIntensity = 15;
    @Category("Aim Assist")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AimBone aimBone = AimBone.EYE;
    @Category("Aim Assist")
    @Tooltip
    public boolean triggerBotEnabled = false;
    @Category("Aim Assist")
    @Tooltip
    public boolean autoChestplateOnTrigger = false;
    @Category("Aim Assist")
    @Tooltip
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AimMode aimMode = AimMode.WHILE_FALLING;

    @Category("Wind Charge")
    @Tooltip
    public boolean windOnRightClickEnabled = true;
    @Category("Wind Charge")
    @Tooltip
    public boolean rocketBoostEnabled = true;
    @Category("Wind Charge")
    @Tooltip
    public boolean autoJumpWindCharge = false;
    @Category("Wind Charge")
    @Tooltip
    public boolean elytraBoostEnabled = false;
    @Category("Wind Charge")
    @Tooltip
    @BoundedDiscrete(min = -90, max = 50)
    public int elytraBoostRedirectAngle = -35;
    @Category("Wind Charge")
    @Tooltip
    @BoundedDiscrete(min = 1, max = 10)
    public int elytraBoostRedirectTicks = 2;

    @Category("Misc")
    @Tooltip
    public boolean showActionBarMessages = true;
    @Category("Misc")
    @Tooltip
    @BoundedDiscrete(min = 1, max = 9)
    public int gearUpClicksPerTick = 1;
    @Category("Misc")
    @Tooltip
    @BoundedDiscrete(min = 0, max = 20)
    public int gearUpDelayTicks = 0;
    @Category("Misc")
    @Tooltip
    public boolean airPotsEnabled = false;

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    @Environment(EnvType.CLIENT)
    public enum MaceEnchantMode {
        DENSITY, BREACH;

        @Override
        public String toString() {
            return this == DENSITY ? "Density" : "Breach";
        }

        public Text asText() {
            return this == DENSITY ? Text.literal("Density").formatted(Formatting.GREEN)
                    : Text.literal("Breach").formatted(Formatting.GOLD);
        }
    }

    @Environment(EnvType.CLIENT)
    public enum AimMode {
        WHILE_FALLING, ALWAYS;

        @Override
        public String toString() {
            return this == WHILE_FALLING ? "While Falling" : "Always";
        }

        public Text asText() {
            return this == WHILE_FALLING ? Text.literal("While Falling").formatted(Formatting.YELLOW)
                    : Text.literal("Always").formatted(Formatting.AQUA);
        }
    }

    @Environment(EnvType.CLIENT)
    public enum StunSlamMode {
        NORMAL, SAFE, UNSAFE;

        @Override
        public String toString() {
            return switch (this) {
                case NORMAL -> "Normal";
                case SAFE -> "Safe";
                case UNSAFE -> "Unsafe";
            };
        }
    }

    @Environment(EnvType.CLIENT)
    public enum AttributeSwapMode {
        ALL("All"), WEAPONS_ONLY("Weapons Only");

        private final String label;

        AttributeSwapMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Environment(EnvType.CLIENT)
    public enum BreachSwapTriggerMode {
        WEAPONS("Weapons"), ALL("All");

        private final String label;

        BreachSwapTriggerMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Environment(EnvType.CLIENT)
    public enum StunSlamTriggerMode {
        WEAPONS("Weapons"), ALL("All");

        private final String label;

        StunSlamTriggerMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Environment(EnvType.CLIENT)
    public enum ReturnSlotMode {
        PREVIOUS, SWORD, AXE, ELYTRA
    }

    @Environment(EnvType.CLIENT)
    public enum AimBone {
        EYE, CHEST, LEGS;

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
}