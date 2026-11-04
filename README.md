# BetterMaceSwap

A client-side Fabric mod that automates mace PvP combat sequences — no manual hotkey juggling required. Focus on the fight, not the inventory.

> **73,000+ downloads on CurseForge** · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/bettermaceswap) · [Discord](https://discord.gg/K4YG6DnAVu)

**Minecraft 26.3** · Fabric · Client-side

---

## Features

**Combat**
- **Auto Attribute Swap** — swaps to mace after a sword/axe hit, swaps back after a configurable delay
- **Smart Switch** — auto picks Density when falling, Breach when grounded
- **Auto Stun Slam** — axe-stuns shields, mace slam follow-up, sword chain finish. Normal / Safe / Unsafe modes + Return Slot
- **Lunge Swap** — swaps to your lunge spear, swaps back automatically
- **Target filters** — choose Players and/or Mobs separately for combat and aim

**Pearl & Wind Charge**
- **Pearl Catch** — fires a Wind Charge after an Ender Pearl throw, with configurable catch delay, return angle and return slot
- **Experimental Pearl Catch** — simulates the pearl's flight to time the catch
- **Right-Click Wind Charge** — right-click with sword, axe or mace to fire a Wind Charge from your hotbar
- **Rocket Boost** — firework rockets work with the right-click boost
- **Auto Jump Wind Charge** — jumps and throws a Wind Charge when you look down at the ground
- **Auto Air Pots** — throws a Wind Charge after a splash potion thrown mid-air

**Aim & Elytra**
- **Aim Assist** — smooth target tracking with adjustable speed, distance and target bone (Eye / Chest / Legs). Works while falling or always
- **Trigger Bot** — attacks when your crosshair is on a valid target while falling
- **Auto Chestplate** — swaps elytra for a chestplate when a target is 4–5 blocks away, then back
- **Elytra Boost** — arm it with a keybind, redirect angle and delay configurable

**Quality of life**
- **Quick Gear Up** — record up to 3 inventory kits, restore the best gear with one keybind (never drops items)
- **`/bms` commands** — toggle everything from chat with a clickable status screen
- **Mod Menu config** — tabbed screen with tooltips on every option
- Action bar messages for toggles and Gear Up

## Installation

1. Install [Fabric Loader 0.19.5+](https://fabricmc.net/use/installer/)
2. Install **Fabric API**, **Cloth Config** and **Mod Menu**
3. Download the latest jar from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/bettermaceswap)
4. Drop it into `.minecraft/mods` and launch

## Commands

`/bms` (alias `/bettermaceswap`)

| Command | What it does |
| --- | --- |
| `/bms status` | Clickable category list: combat, targets, stunslam, pearl, aim, windcharge, misc, all |
| `/bms menu` | Opens the config screen |
| `/bms combat <feature> [on/off/toggle]` | attributeswap, smartswitch, mace_mode, target_players, target_mobs… |
| `/bms stunslam <option>` | stun_slam, trigger_mode, return_slot, mode (normal/safe/unsafe) |
| `/bms pearl <option>` | pearl_catch, return_angle, return_mode |
| `/bms aim <option>` | aim_assist, bone, mode, triggerbot, auto_chestplate, targets |
| `/bms misc <option>` | lungeswap, wind, rocketboost, autojump_wind, elytra_boost, airpots, action_bar, experimental_pearl |
| `/bms misc record_inv_1..3` | Records your inventory as Gear Up kit 1–3 |
| `/bms misc gearup_1..3` | Restores Gear Up kit 1–3 |

## Keybinds

Find them in Minecraft Controls under **BetterMaceSwap**.

| Action | Default |
| --- | --- |
| Toggle Auto Attribute Swap | B |
| Toggle Auto Stun Slam | V |
| Cycle Mace Mode (Density / Breach) | N |
| Toggle Aim Assist | Z |
| Toggle Aim Assist Mode | G |
| Toggle Trigger Bot | K |
| Toggle Lunge Swap | *unbound* |
| Toggle Pearl Catch | *unbound* |
| Arm Elytra Boost | *unbound* |
| Quick Gear Up 1 / 2 / 3 | *unbound* |

## Requirements

Minecraft 26.3 · Fabric Loader 0.19.5+ · Fabric API · Cloth Config · Mod Menu · Java 25+

## Building

```
gradlew build
```

## Support

Questions, bugs or suggestions? Join the [Discord](https://discord.gg/K4YG6DnAVu). Like the mod? [Ko-fi](https://ko-fi.com/thatzritesh)

## License

MIT — *Also check out [BetterPvPSprint](https://www.curseforge.com/minecraft/mc-mods/betterpvpsprint) — full sprint control for PvP.*
