package com.ritesh.keybind;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyMapping BREACH_SWAP_TOGGLE;
    public static KeyMapping STUN_SLAM_TOGGLE;
    public static KeyMapping MACE_MODE_CYCLE;
    public static KeyMapping PEARL_CATCH_TOGGLE;
    public static KeyMapping AIM_ASSIST_TOGGLE;
    public static KeyMapping TRIGGER_BOT_TOGGLE;
    public static KeyMapping LUNGE_SWAP_TOGGLE;
    public static KeyMapping AIM_MODE_TOGGLE;
    public static KeyMapping ELYTRA_BOOST_ARM;
    public static KeyMapping GEAR_UP;
    public static KeyMapping GEAR_UP_2;
    public static KeyMapping GEAR_UP_3;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("bettermaceswap", "general"));

    public static void register() {
        BREACH_SWAP_TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.breach_swap_toggle",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

        LUNGE_SWAP_TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.lunge_swap_toggle",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

        STUN_SLAM_TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.stun_slam_toggle",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));

        MACE_MODE_CYCLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.mace_mode_cycle",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY));

        PEARL_CATCH_TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.pearl_catch_toggle",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

        AIM_ASSIST_TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.aim_assist_toggle",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY));

        TRIGGER_BOT_TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.trigger_bot_toggle",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));

        AIM_MODE_TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.aim_mode_toggle",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

        ELYTRA_BOOST_ARM = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.elytra_boost_arm",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

        GEAR_UP = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.gear_up",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

        GEAR_UP_2 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.gear_up_2",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

        GEAR_UP_3 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermaceswap.gear_up_3",
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

    }
}
