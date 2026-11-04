package com.ritesh.command;

import com.mojang.brigadier.CommandDispatcher;
import com.ritesh.config.ModConfig;
import com.ritesh.feature.GearUp;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class BmsCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommands.literal("bms")

                // ═══════════════════════════════════════════════
                // COMBAT
                // ═══════════════════════════════════════════════
                .then(ClientCommands.literal("combat")
                        .then(ClientCommands.literal("attributeswap")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().autoBreachSwapEnabled = !ModConfig.get().autoBreachSwapEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Attribute Swap", ModConfig.get().autoBreachSwapEnabled, "/bms combat attributeswap");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().autoBreachSwapEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Attribute Swap", true, "/bms combat attributeswap");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().autoBreachSwapEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Attribute Swap", false, "/bms combat attributeswap");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("smartswitch")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().smartSwitchEnabled = !ModConfig.get().smartSwitchEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Smart Switch", ModConfig.get().smartSwitchEnabled, "/bms combat smartswitch");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().smartSwitchEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Smart Switch", true, "/bms combat smartswitch");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().smartSwitchEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Smart Switch", false, "/bms combat smartswitch");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("mace_mode")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Mace Mode", ModConfig.get().maceMode, v -> ModConfig.get().maceMode = v, "/bms combat mace_mode")))
                                .then(ClientCommands.literal("density").executes(ctx -> {
                                    ModConfig.get().maceMode = ModConfig.MaceEnchantMode.DENSITY;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Mace Mode", "Density", "/bms combat mace_mode breach");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("breach").executes(ctx -> {
                                    ModConfig.get().maceMode = ModConfig.MaceEnchantMode.BREACH;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Mace Mode", "Breach", "/bms combat mace_mode density");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("attribute_swap_mode")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Attribute Swap Mode", ModConfig.get().attributeSwapMode, v -> ModConfig.get().attributeSwapMode = v, "/bms combat attribute_swap_mode")))
                                .then(ClientCommands.literal("all").executes(ctx -> {
                                    ModConfig.get().attributeSwapMode = ModConfig.AttributeSwapMode.ALL;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Attribute Swap Mode", "All", "/bms combat attribute_swap_mode weapons_only");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("weapons_only").executes(ctx -> {
                                    ModConfig.get().attributeSwapMode = ModConfig.AttributeSwapMode.WEAPONS_ONLY;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Attribute Swap Mode", "Weapons Only", "/bms combat attribute_swap_mode all");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("attribute_trigger_mode")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Attribute Trigger Mode", ModConfig.get().breachSwapTriggerMode, v -> ModConfig.get().breachSwapTriggerMode = v, "/bms combat attribute_trigger_mode")))
                                .then(ClientCommands.literal("weapons").executes(ctx -> {
                                    ModConfig.get().breachSwapTriggerMode = ModConfig.BreachSwapTriggerMode.WEAPONS;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Attribute Trigger Mode", "Weapons", "/bms combat attribute_trigger_mode all");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("all").executes(ctx -> {
                                    ModConfig.get().breachSwapTriggerMode = ModConfig.BreachSwapTriggerMode.ALL;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Attribute Trigger Mode", "All", "/bms combat attribute_trigger_mode weapons");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("target_players")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().combatTargetPlayers = !ModConfig.get().combatTargetPlayers;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Combat Target Players", ModConfig.get().combatTargetPlayers, "/bms combat target_players");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().combatTargetPlayers = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Combat Target Players", true, "/bms combat target_players");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().combatTargetPlayers = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Combat Target Players", false, "/bms combat target_players");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("target_mobs")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().combatTargetMobs = !ModConfig.get().combatTargetMobs;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Combat Target Mobs", ModConfig.get().combatTargetMobs, "/bms combat target_mobs");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().combatTargetMobs = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Combat Target Mobs", true, "/bms combat target_mobs");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().combatTargetMobs = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Combat Target Mobs", false, "/bms combat target_mobs");
                                    return 1;
                                }))))

                // ═══════════════════════════════════════════════
                // STUN SLAM
                // ═══════════════════════════════════════════════
                .then(ClientCommands.literal("stunslam")
                        .then(ClientCommands.literal("stun_slam")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().autoStunSlamEnabled = !ModConfig.get().autoStunSlamEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Stun Slam", ModConfig.get().autoStunSlamEnabled, "/bms stunslam stun_slam");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().autoStunSlamEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Stun Slam", true, "/bms stunslam stun_slam");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().autoStunSlamEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Stun Slam", false, "/bms stunslam stun_slam");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("trigger_mode")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Stun Trigger Mode", ModConfig.get().stunSlamTriggerMode, v -> ModConfig.get().stunSlamTriggerMode = v, "/bms stunslam trigger_mode")))
                                .then(ClientCommands.literal("weapons").executes(ctx -> {
                                    ModConfig.get().stunSlamTriggerMode = ModConfig.StunSlamTriggerMode.WEAPONS;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Stun Trigger Mode", "Weapons", "/bms stunslam trigger_mode all");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("all").executes(ctx -> {
                                    ModConfig.get().stunSlamTriggerMode = ModConfig.StunSlamTriggerMode.ALL;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Stun Trigger Mode", "All", "/bms stunslam trigger_mode weapons");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("return_slot")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().stunSlamReturnSlot = !ModConfig.get().stunSlamReturnSlot;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Stun Return Slot", ModConfig.get().stunSlamReturnSlot, "/bms stunslam return_slot");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().stunSlamReturnSlot = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Stun Return Slot", true, "/bms stunslam return_slot");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().stunSlamReturnSlot = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Stun Return Slot", false, "/bms stunslam return_slot");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("mode")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Stun Slam Mode", ModConfig.get().stunSlamMode, v -> ModConfig.get().stunSlamMode = v, "/bms stunslam mode")))
                                .then(ClientCommands.literal("normal").executes(ctx -> {
                                    ModConfig.get().stunSlamMode = ModConfig.StunSlamMode.NORMAL;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Stun Slam Mode", "Normal", "/bms stunslam mode safe");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("safe").executes(ctx -> {
                                    ModConfig.get().stunSlamMode = ModConfig.StunSlamMode.SAFE;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Stun Slam Mode", "Safe", "/bms stunslam mode unsafe");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("unsafe").executes(ctx -> {
                                    ModConfig.get().stunSlamMode = ModConfig.StunSlamMode.UNSAFE;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Stun Slam Mode", "Unsafe", "/bms stunslam mode normal");
                                    return 1;
                                }))))

                // ═══════════════════════════════════════════════
                // PEARL
                // ═══════════════════════════════════════════════
                .then(ClientCommands.literal("pearl")
                        .then(ClientCommands.literal("pearl_catch")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().pearlCatchEnabled = !ModConfig.get().pearlCatchEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Pearl Catch", ModConfig.get().pearlCatchEnabled, "/bms pearl pearl_catch");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().pearlCatchEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Pearl Catch", true, "/bms pearl pearl_catch");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().pearlCatchEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Pearl Catch", false, "/bms pearl pearl_catch");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("return_angle")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().pearlCatchReturnAngle = !ModConfig.get().pearlCatchReturnAngle;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Pearl Return Angle", ModConfig.get().pearlCatchReturnAngle, "/bms pearl return_angle");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().pearlCatchReturnAngle = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Pearl Return Angle", true, "/bms pearl return_angle");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().pearlCatchReturnAngle = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Pearl Return Angle", false, "/bms pearl return_angle");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("return_mode")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Pearl Return Mode", ModConfig.get().pearlReturnMode, v -> ModConfig.get().pearlReturnMode = v, "/bms pearl return_mode")))
                                .then(ClientCommands.literal("previous").executes(ctx -> {
                                    ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.PREVIOUS;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Pearl Return Mode", "Previous", "/bms pearl return_mode sword");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("sword").executes(ctx -> {
                                    ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.SWORD;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Pearl Return Mode", "Sword", "/bms pearl return_mode axe");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("axe").executes(ctx -> {
                                    ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.AXE;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Pearl Return Mode", "Axe", "/bms pearl return_mode elytra");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("elytra").executes(ctx -> {
                                    ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.ELYTRA;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Pearl Return Mode", "Elytra", "/bms pearl return_mode previous");
                                    return 1;
                                }))))

                // ═══════════════════════════════════════════════
                // AIM ASSIST
                // ═══════════════════════════════════════════════
                .then(ClientCommands.literal("aim")
                        .then(ClientCommands.literal("aim_assist")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().aimAssistEnabled = !ModConfig.get().aimAssistEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Assist", ModConfig.get().aimAssistEnabled, "/bms aim aim_assist");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().aimAssistEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Assist", true, "/bms aim aim_assist");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().aimAssistEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Assist", false, "/bms aim aim_assist");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("target_players")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().aimTargetPlayers = !ModConfig.get().aimTargetPlayers;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Target Players", ModConfig.get().aimTargetPlayers, "/bms aim target_players");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().aimTargetPlayers = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Target Players", true, "/bms aim target_players");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().aimTargetPlayers = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Target Players", false, "/bms aim target_players");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("target_mobs")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().aimTargetMobs = !ModConfig.get().aimTargetMobs;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Target Mobs", ModConfig.get().aimTargetMobs, "/bms aim target_mobs");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().aimTargetMobs = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Target Mobs", true, "/bms aim target_mobs");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().aimTargetMobs = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Aim Target Mobs", false, "/bms aim target_mobs");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("bone")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Aim Bone", ModConfig.get().aimBone, v -> ModConfig.get().aimBone = v, "/bms aim bone")))
                                .then(ClientCommands.literal("eye").executes(ctx -> {
                                    ModConfig.get().aimBone = ModConfig.AimBone.EYE;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Aim Bone", "Eye", "/bms aim bone chest");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("chest").executes(ctx -> {
                                    ModConfig.get().aimBone = ModConfig.AimBone.CHEST;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Aim Bone", "Chest", "/bms aim bone legs");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("legs").executes(ctx -> {
                                    ModConfig.get().aimBone = ModConfig.AimBone.LEGS;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Aim Bone", "Legs", "/bms aim bone eye");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("triggerbot")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().triggerBotEnabled = !ModConfig.get().triggerBotEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Trigger Bot", ModConfig.get().triggerBotEnabled, "/bms aim triggerbot");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().triggerBotEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Trigger Bot", true, "/bms aim triggerbot");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().triggerBotEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Trigger Bot", false, "/bms aim triggerbot");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("auto_chestplate")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().autoChestplateOnTrigger = !ModConfig.get().autoChestplateOnTrigger;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Auto Chestplate", ModConfig.get().autoChestplateOnTrigger, "/bms aim auto_chestplate");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().autoChestplateOnTrigger = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Auto Chestplate", true, "/bms aim auto_chestplate");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().autoChestplateOnTrigger = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Auto Chestplate", false, "/bms aim auto_chestplate");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("mode")
                                .then(ClientCommands.literal("next").executes(ctx -> cycle(ctx.getSource(), "Aim Mode", ModConfig.get().aimMode, v -> ModConfig.get().aimMode = v, "/bms aim mode")))
                                .then(ClientCommands.literal("while_falling").executes(ctx -> {
                                    ModConfig.get().aimMode = ModConfig.AimMode.WHILE_FALLING;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Aim Mode", "While Falling", "/bms aim mode always");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("always").executes(ctx -> {
                                    ModConfig.get().aimMode = ModConfig.AimMode.ALWAYS;
                                    ModConfig.save();
                                    sendEnum(ctx.getSource(), "Aim Mode", "Always", "/bms aim mode while_falling");
                                    return 1;
                                }))))

                // ═══════════════════════════════════════════════
                // MISC
                // ═══════════════════════════════════════════════
                .then(ClientCommands.literal("misc")
                        .then(ClientCommands.literal("record_inv_1")
                                .executes(ctx -> {
                                    GearUp.record(Minecraft.getInstance(), 1);
                                    return 1;
                                }))
                        .then(ClientCommands.literal("gearup_1")
                                .executes(ctx -> {
                                    GearUp.start(Minecraft.getInstance(), 1);
                                    return 1;
                                }))
                        .then(ClientCommands.literal("record_inv_2")
                                .executes(ctx -> {
                                    GearUp.record(Minecraft.getInstance(), 2);
                                    return 1;
                                }))
                        .then(ClientCommands.literal("gearup_2")
                                .executes(ctx -> {
                                    GearUp.start(Minecraft.getInstance(), 2);
                                    return 1;
                                }))
                        .then(ClientCommands.literal("record_inv_3")
                                .executes(ctx -> {
                                    GearUp.record(Minecraft.getInstance(), 3);
                                    return 1;
                                }))
                        .then(ClientCommands.literal("gearup_3")
                                .executes(ctx -> {
                                    GearUp.start(Minecraft.getInstance(), 3);
                                    return 1;
                                }))
                        .then(ClientCommands.literal("lungeswap")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().lungeSwapEnabled = !ModConfig.get().lungeSwapEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Lunge Swap", ModConfig.get().lungeSwapEnabled, "/bms misc lungeswap");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().lungeSwapEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Lunge Swap", true, "/bms misc lungeswap");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().lungeSwapEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Lunge Swap", false, "/bms misc lungeswap");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("wind")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().windOnRightClickEnabled = !ModConfig.get().windOnRightClickEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Wind Right Click", ModConfig.get().windOnRightClickEnabled, "/bms misc wind");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().windOnRightClickEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Wind Right Click", true, "/bms misc wind");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().windOnRightClickEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Wind Right Click", false, "/bms misc wind");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("rocketboost")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().rocketBoostEnabled = !ModConfig.get().rocketBoostEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Rocket Boost", ModConfig.get().rocketBoostEnabled, "/bms misc rocketboost");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().rocketBoostEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Rocket Boost", true, "/bms misc rocketboost");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().rocketBoostEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Rocket Boost", false, "/bms misc rocketboost");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("autojump_wind")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().autoJumpWindCharge = !ModConfig.get().autoJumpWindCharge;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Auto Jump Wind", ModConfig.get().autoJumpWindCharge, "/bms misc autojump_wind");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().autoJumpWindCharge = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Auto Jump Wind", true, "/bms misc autojump_wind");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().autoJumpWindCharge = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Auto Jump Wind", false, "/bms misc autojump_wind");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("elytra_boost")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().elytraBoostEnabled = !ModConfig.get().elytraBoostEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Elytra Boost", ModConfig.get().elytraBoostEnabled, "/bms misc elytra_boost");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().elytraBoostEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Elytra Boost", true, "/bms misc elytra_boost");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().elytraBoostEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Elytra Boost", false, "/bms misc elytra_boost");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("airpots")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().airPotsEnabled = !ModConfig.get().airPotsEnabled;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Air Pots", ModConfig.get().airPotsEnabled, "/bms misc airpots");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().airPotsEnabled = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Air Pots", true, "/bms misc airpots");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().airPotsEnabled = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Air Pots", false, "/bms misc airpots");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("action_bar")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().showActionBarMessages = !ModConfig.get().showActionBarMessages;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Action Bar Messages", ModConfig.get().showActionBarMessages, "/bms misc action_bar");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().showActionBarMessages = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Action Bar Messages", true, "/bms misc action_bar");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().showActionBarMessages = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Action Bar Messages", false, "/bms misc action_bar");
                                    return 1;
                                })))
                        .then(ClientCommands.literal("experimental_pearl")
                                .then(ClientCommands.literal("toggle").executes(ctx -> {
                                    ModConfig.get().experimentalPearlCatch = !ModConfig.get().experimentalPearlCatch;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Experimental Pearl Catch", ModConfig.get().experimentalPearlCatch, "/bms misc experimental_pearl");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("on").executes(ctx -> {
                                    ModConfig.get().experimentalPearlCatch = true;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Experimental Pearl Catch", true, "/bms misc experimental_pearl");
                                    return 1;
                                }))
                                .then(ClientCommands.literal("off").executes(ctx -> {
                                    ModConfig.get().experimentalPearlCatch = false;
                                    ModConfig.save();
                                    send(ctx.getSource(), "Experimental Pearl Catch", false, "/bms misc experimental_pearl");
                                    return 1;
                                }))))

                // ═══════════════════════════════════════════════
                // MENU
                // ═══════════════════════════════════════════════
                .then(ClientCommands.literal("menu").executes(ctx -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> mc.gui.setScreen(
                            me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen(ModConfig.class, mc.gui.screen()).get()));
                    return 1;
                }))

                // ═══════════════════════════════════════════════
                // STATUS
                // ═══════════════════════════════════════════════
                .then(ClientCommands.literal("status")
                        .executes(ctx -> {
                            statusMenu(ctx.getSource());
                            return 1;
                        })
                        .then(ClientCommands.literal("all").executes(ctx -> {
                            statusAll(ctx.getSource());
                            return 1;
                        }))
                        .then(ClientCommands.literal("combat").executes(ctx -> {
                            statusOne(ctx.getSource(), "combat");
                            return 1;
                        }))
                        .then(ClientCommands.literal("targets").executes(ctx -> {
                            statusOne(ctx.getSource(), "targets");
                            return 1;
                        }))
                        .then(ClientCommands.literal("stunslam").executes(ctx -> {
                            statusOne(ctx.getSource(), "stunslam");
                            return 1;
                        }))
                        .then(ClientCommands.literal("pearl").executes(ctx -> {
                            statusOne(ctx.getSource(), "pearl");
                            return 1;
                        }))
                        .then(ClientCommands.literal("aim").executes(ctx -> {
                            statusOne(ctx.getSource(), "aim");
                            return 1;
                        }))
                        .then(ClientCommands.literal("windcharge").executes(ctx -> {
                            statusOne(ctx.getSource(), "windcharge");
                            return 1;
                        }))
                        .then(ClientCommands.literal("misc").executes(ctx -> {
                            statusOne(ctx.getSource(), "misc");
                            return 1;
                        })));

        dispatcher.register(root);
        dispatcher.register(
                ClientCommands.literal("bettermaceswap").redirect(dispatcher.getRoot().getChild("bms")));
    }


    // ── /bms status ──
    private static final String[][] STATUS_CATS = {
            {"combat", "Combat"}, {"targets", "Targets"}, {"stunslam", "Stun Slam"}, {"pearl", "Pearl"},
            {"aim", "Aim Assist"}, {"windcharge", "Wind Charge"}, {"misc", "Misc"}
    };

    /** /bms status : a clickable list of categories. */
    private static void statusMenu(FabricClientCommandSource src) {
        src.sendFeedback(Component.literal("§6§l━━━ BetterMaceSwap Status ━━━"));
        for (String[] cat : STATUS_CATS) {
            src.sendFeedback(clickable("§e  " + cat[1], "/bms status " + cat[0], "Show " + cat[1] + " settings"));
        }
        src.sendFeedback(clickable("§a  All", "/bms status all", "Show every setting"));
        src.sendFeedback(clickable("§7  Open Menu", "/bms menu", "Open mod config screen"));
        src.sendFeedback(Component.literal("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private static void statusAll(FabricClientCommandSource src) {
        src.sendFeedback(Component.literal("§6§l━━━ BetterMaceSwap Status ━━━"));
        src.sendFeedback(Component.literal("§7Click any toggle to flip it  •  ")
                .append(clickable("§e[Open Menu]", "/bms menu", "Open mod config screen")));
        for (String[] cat : STATUS_CATS) statusLines(src, cat[0]);
        src.sendFeedback(Component.literal("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private static void statusOne(FabricClientCommandSource src, String key) {
        String title = key;
        for (String[] cat : STATUS_CATS) if (cat[0].equals(key)) title = cat[1];
        src.sendFeedback(Component.literal("§6§l━━━ BMS: " + title + " ━━━"));
        statusLines(src, key);
        src.sendFeedback(Component.literal("§7")
                .append(clickable("§e[Back]", "/bms status", "Back to categories"))
                .append(Component.literal("  "))
                .append(clickable("§e[Open Menu]", "/bms menu", "Open mod config screen")));
    }

    private static void statusLines(FabricClientCommandSource src, String key) {
        ModConfig c = ModConfig.get();
        switch (key) {
            case "combat" -> {
                header(src, "Combat");
                    src.sendFeedback(toggleLine("  Auto Attribute Swap", c.autoBreachSwapEnabled, "/bms combat attributeswap"));
                    src.sendFeedback(toggleLine("  Smart Switch", c.smartSwitchEnabled, "/bms combat smartswitch"));
                    src.sendFeedback(valueLine("  Swap Delay (Ticks)", c.breachSwapBackTicks));
                    src.sendFeedback(enumLine("  Attribute Swap Trigger", c.breachSwapTriggerMode.toString(),
                            c.breachSwapTriggerMode == ModConfig.BreachSwapTriggerMode.WEAPONS
                                    ? "/bms combat attribute_trigger_mode all"
                                    : "/bms combat attribute_trigger_mode weapons",
                            "Click to switch"));
                    src.sendFeedback(enumLine("  Default Mace Mode", c.maceMode.toString(),
                            c.maceMode == ModConfig.MaceEnchantMode.DENSITY
                                    ? "/bms combat mace_mode breach"
                                    : "/bms combat mace_mode density",
                            "Click to switch"));
                    src.sendFeedback(enumLine("  Swap Trigger", c.attributeSwapMode.toString(),
                            c.attributeSwapMode == ModConfig.AttributeSwapMode.ALL
                                    ? "/bms combat attribute_swap_mode weapons_only"
                                    : "/bms combat attribute_swap_mode all",
                            "Click to switch"));
                    src.sendFeedback(toggleLine("  Lunge Swap", c.lungeSwapEnabled, "/bms misc lungeswap"));
                    src.sendFeedback(valueLine("  Lunge Swap Delay", c.lungeSwapDelay));
            }
            case "targets" -> {
                header(src, "Targets");
                    src.sendFeedback(toggleLine("  Target Players (Combat)", c.combatTargetPlayers, "/bms combat target_players"));
                    src.sendFeedback(toggleLine("  Target Mobs (Combat)", c.combatTargetMobs, "/bms combat target_mobs"));
                    src.sendFeedback(toggleLine("  Target Players (Aim)", c.aimTargetPlayers, "/bms aim target_players"));
                    src.sendFeedback(toggleLine("  Target Mobs (Aim)", c.aimTargetMobs, "/bms aim target_mobs"));
            }
            case "stunslam" -> {
                header(src, "Stun Slam");
                    src.sendFeedback(toggleLine("  Auto Stun Slam", c.autoStunSlamEnabled, "/bms stunslam stun_slam"));
                    src.sendFeedback(enumLine("  Stun Trigger Mode", c.stunSlamTriggerMode.toString(),
                            c.stunSlamTriggerMode == ModConfig.StunSlamTriggerMode.WEAPONS
                                    ? "/bms stunslam trigger_mode all"
                                    : "/bms stunslam trigger_mode weapons",
                            "Click to switch"));
                    src.sendFeedback(toggleLine("  Return Slot", c.stunSlamReturnSlot, "/bms stunslam return_slot"));
                    src.sendFeedback(enumLine("  Stun Slam Mode", c.stunSlamMode.toString(),
                            "/bms stunslam mode " + nextSlamMode(c.stunSlamMode), "Click to cycle"));
                    src.sendFeedback(valueLine("  No-Mace Axe Hit Delay", c.noMaceAxeHitDelay));
            }
            case "pearl" -> {
                header(src, "Pearl");
                    src.sendFeedback(toggleLine("  Pearl Catch", c.pearlCatchEnabled, "/bms pearl pearl_catch"));
                    src.sendFeedback(valueLine("  Catch Delay (Ticks)", c.pearlCatchDelayTicks));
                    src.sendFeedback(valueLine("  Catch Angle", c.pearlCatchAngle));
                    src.sendFeedback(toggleLine("  Return Angle", c.pearlCatchReturnAngle, "/bms pearl return_angle"));
                    src.sendFeedback(valueLine("  Return Angle Value", c.pearlReturnAngle));
                    src.sendFeedback(enumLine("  Return Slot", enumName(c.pearlReturnMode),
                            "/bms pearl return_mode " + nextReturnMode(c.pearlReturnMode), "Click to cycle"));
                    src.sendFeedback(toggleLine("  Experimental Pearl Catch", c.experimentalPearlCatch, "/bms misc experimental_pearl"));
            }
            case "aim" -> {
                header(src, "Aim Assist");
                    src.sendFeedback(toggleLine("  Aim Assist", c.aimAssistEnabled, "/bms aim aim_assist"));
                    src.sendFeedback(valueLine("  Aim Speed", c.aimSpeed));
                    src.sendFeedback(valueLine("  Aim Distance", c.aimIntensity));
                    src.sendFeedback(enumLine("  Aim Target Bone", c.aimBone.toString(),
                            "/bms aim bone " + nextBone(c.aimBone), "Click to cycle"));
                    src.sendFeedback(enumLine("  Aim Assist Mode", c.aimMode.toString(),
                            c.aimMode == ModConfig.AimMode.WHILE_FALLING
                                    ? "/bms aim mode always"
                                    : "/bms aim mode while_falling",
                            "Click to switch"));
                    src.sendFeedback(toggleLine("  Trigger Bot", c.triggerBotEnabled, "/bms aim triggerbot"));
                    src.sendFeedback(toggleLine("  Auto Chestplate", c.autoChestplateOnTrigger, "/bms aim auto_chestplate"));
            }
            case "windcharge" -> {
                header(src, "Wind Charge");
                    src.sendFeedback(toggleLine("  Right-Click Wind Charge", c.windOnRightClickEnabled, "/bms misc wind"));
                    src.sendFeedback(toggleLine("  Rocket Boost", c.rocketBoostEnabled, "/bms misc rocketboost"));
                    src.sendFeedback(toggleLine("  Auto Jump Wind Charge", c.autoJumpWindCharge, "/bms misc autojump_wind"));
                    src.sendFeedback(toggleLine("  Elytra Boost", c.elytraBoostEnabled, "/bms misc elytra_boost"));
                    src.sendFeedback(valueLine("  Boost Redirect Angle", c.elytraBoostRedirectAngle));
                    src.sendFeedback(valueLine("  Boost Redirect Delay (Ticks)", c.elytraBoostRedirectTicks));
            }
            case "misc" -> {
                header(src, "Misc");
                    src.sendFeedback(toggleLine("  Show Action Bar Messages", c.showActionBarMessages, "/bms misc action_bar"));
                    src.sendFeedback(valueLine("  Gear Up Clicks Per Tick", c.gearUpClicksPerTick));
                    src.sendFeedback(valueLine("  Gear Up Delay Between Clicks (ticks)", c.gearUpDelayTicks));
                    src.sendFeedback(toggleLine("  Auto Air Pots", c.airPotsEnabled, "/bms misc airpots"));
            }
            default -> { }
        }
    }

    // ── clickable toggle line ──
    private static MutableComponent toggleLine(String label, boolean on, String baseCmd) {
        String cmd = baseCmd + " toggle";
        MutableComponent badge = Component.literal(on ? " [ON]" : " [OFF]")
                .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED)
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.RunCommand(cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to turn " + (on ? "OFF" : "ON")))));
        return Component.literal(label + ":").withStyle(ChatFormatting.WHITE).append(badge);
    }

    // ── clickable enum line ──
    private static MutableComponent enumLine(String label, String currentVal, String cmd, String hoverMsg) {
        cmd = cmd.substring(0, cmd.lastIndexOf(' ')) + " next";
        final String fcmd = cmd;
        MutableComponent badge = Component.literal(" [" + currentVal + "]")
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.RunCommand(fcmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverMsg))));
        return Component.literal(label + ":").withStyle(ChatFormatting.WHITE).append(badge);
    }

    // ── category header ──
    private static void header(FabricClientCommandSource src, String name) {
        src.sendFeedback(Component.literal(name).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
    }

    // ── plain value line (numbers are set in the config screen) ──
    private static MutableComponent valueLine(String label, int value) {
        return Component.literal(label + ": ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.AQUA));
    }

    private static String enumName(Enum<?> e) {
        String n = e.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    // ── plain clickable text ──
    private static MutableComponent clickable(String display, String cmd, String hover) {
        return Component.literal(display)
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.RunCommand(cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover))));
    }

    // ── cycle helpers ──
    private static String nextReturnMode(ModConfig.ReturnSlotMode current) {
        return switch (current) {
            case PREVIOUS -> "sword";
            case SWORD -> "axe";
            case AXE -> "elytra";
            case ELYTRA -> "previous";
        };
    }

    private static String nextSlamMode(ModConfig.StunSlamMode mode) {
        return switch (mode) {
            case NORMAL -> "safe";
            case SAFE -> "unsafe";
            case UNSAFE -> "normal";
        };
    }

    private static String nextBone(ModConfig.AimBone current) {
        return switch (current) {
            case EYE -> "chest";
            case CHEST -> "legs";
            case LEGS -> "eye";
        };
    }

    // ── feedback helpers (every line is clickable: click again to flip back / cycle) ──
    private static void send(FabricClientCommandSource src, String feature, boolean on, String baseCmd) {
        String cmd = baseCmd + " toggle";
        MutableComponent line = Component.literal("[BMS] " + feature + ": ")
                .append(Component.literal(on ? "ON" : "OFF").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
        src.sendFeedback(line.withStyle(s -> s
                .withClickEvent(new ClickEvent.RunCommand(cmd))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to turn " + (on ? "OFF" : "ON"))))));
    }

    private static void sendEnum(FabricClientCommandSource src, String feature, String value, String nextCmdIn) {
        final String nextCmd = nextCmdIn.substring(0, nextCmdIn.lastIndexOf(' ')) + " next";
        MutableComponent line = Component.literal("[BMS] " + feature + ": ")
                .append(Component.literal(value).withStyle(ChatFormatting.YELLOW));
        src.sendFeedback(line.withStyle(s -> s
                .withClickEvent(new ClickEvent.RunCommand(nextCmd))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to switch")))));
    }

    private static <E extends Enum<E>> int cycle(FabricClientCommandSource src, String feature, E cur,
                                                 java.util.function.Consumer<E> setter, String base) {
        E[] vals = cur.getDeclaringClass().getEnumConstants();
        E next = vals[(cur.ordinal() + 1) % vals.length];
        setter.accept(next);
        ModConfig.save();
        sendEnum(src, feature, enumName(next), base + " next");
        return 1;
    }
}
