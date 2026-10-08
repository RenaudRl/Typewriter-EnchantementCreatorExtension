# EnchantmentCreator

![Java Version](https://img.shields.io/badge/Java-21-orange)
![Target](https://img.shields.io/badge/Target-Paper-blue)
![Typewriter](https://img.shields.io/badge/Typewriter-0.9.0--beta--177-purple)

Create custom enchantments in the **Typewriter** web editor, with ready-made effect actions, enchanted books, and the option to blacklist vanilla enchantments. No extra plugin required.

## Features

- Enchantment definitions (`enchantment_definition`, `custom_enchantment_definition`) wired to effect actions.
- 69 effect actions: damage and status strikes, arrow effects, armor and aura effects, mining and harvesting, movement and utility.
- Enchanted books through `give_enchantment_book` and the admin command.
- A blacklist of vanilla enchantments (`vanilla_enchantment_blacklist_definition`).
- Each mechanic can be limited to one exact enchantment level with `runOnLevel` (`0` = every level).

## Entries

| Category | Entries |
|---|---|
| Manifest | `enchantment_definition`, `custom_enchantment_definition`, `vanilla_enchantment_blacklist_definition` |
| Command | `enchantment_command_manifest` |
| Actions | `give_enchantment_book`, `client_particle_effect` and the effect actions below |

Effect actions:
`agricultural_harvest_action`, `angel_wings_action`, `anti_knockback_action`, `aquatic_speed_action`, `area_miner_action`, `armor_pierce_action`, `auto_smelt_action`, `bedrock_breaker_action`, `beheading_action`, `bleed_action`, `blindness_strike_action`, `cleave_action`, `curse_of_sluggishness_action`, `curse_of_vulnerability_action`, `deflect_action`, `disarm_action`, `elytra_boost_action`, `elytra_shield_action`, `ender_arrow_action`, `enlightened_armor_action`, `evasion_action`, `executioner_action`, `experience_boost_action`, `explosive_arrow_action`, `feeder_action`, `freeze_strike_action`, `frozen_armor_action`, `gearing_action`, `green_thumb_action`, `hardened_armor_action`, `haste_action`, `healing_arrow_action`, `health_boost_action`, `homing_arrow_action`, `implosion_armor_action`, `indestructible_action`, `launch_action`, `lava_walker_action`, `lifesteal_action`, `lightning_arrow_action`, `lightning_strike_action`, `magnet_action`, `molten_armor_action`, `night_vision_action`, `oxygenate_action`, `piercing_shot_action`, `poison_strike_action`, `pull_arrow_action`, `push_arrow_action`, `regeneration_aura_action`, `resistance_aura_action`, `silence_action`, `sniper_arrow_action`, `soulbound_action`, `spawners_silktouch_action`, `telepathy_action`, `teleport_arrow_action`, `thorns_advanced_action`, `timber_action`, `toxic_aura_action`, `treasure_hunter_action`, `vampiric_aura_action`, `vein_miner_action`, `venom_arrow_action`, `volley_arrow_action`, `voodoo_armor_action`, `web_arrow_action`, `wither_arrow_action`, `wither_strike_action`.

`agricultural_harvest_action` reacts to the block-break event and harvests mature crops around the broken block (`radius` capped at 8), with `particle`, `particleCount`, `sound`, `soundVolume` and `soundPitch` fields.

Full field reference on the [wiki](https://docs.borntocraftstudio.net/extensions/free/enchantmentcreator/).

## Command and permission

Declared by the `enchantment_command_manifest` entry (names are fields of the entry):

| Command | Permission |
|---|---|
| `/enchantcreator give <player> <enchant> [level]` | `enchantcreator.admin` |

The command name (`commandName`), the permission (`adminPermission`) and the messages are editable in the entry.

## Requirements

- Typewriter engine `0.9.0-beta-177`, on **Paper**.
- Typewriter **Basic** extension.

## Building

Requires **Java 21**.

```bash
./gradlew clean build
```

Artifact: `build/libs/`.

## Documentation

[BTC Studio Docs](https://docs.borntocraftstudio.net/extensions/free/enchantmentcreator/)

---

## 📜 License

**GNU General Public License v3.0 or later** — [LICENSE](LICENSE) — with a
**linking exception** for the Typewriter engine — [LICENSE-EXCEPTION.md](LICENSE-EXCEPTION.md).

| | |
|---|---|
| You may | Run it anywhere, **including on a monetised server**. Study it, modify it, use it as a base, and redistribute it — **even for a fee**. GPLv3 §4 explicitly allows charging for a copy. |
| You must | Publish the complete corresponding source of your version under GPLv3, preserve the copyright notices, and **state that you modified it and when** (§5(a)). |
| You may not | Ship a closed-source or proprietary version, relicense under stricter terms, or strip the attribution and present this work as your own — §8 terminates your rights automatically. |
| Marks | **"Born To Craft"** and **"BTC Studio"** are **not** covered by the GPL. Fork it freely, sell your fork if you like — but **rebrand it**. |

> Reselling this code is legally allowed and practically pointless: whoever buys a
> copy from you receives, under the GPL, the right to redistribute it for free.
> That is the protection — not a clause forbidding sale, which the GPL does not
> permit us to add.

### About Typewriter

This is a **third-party extension**. It uses the public extension API of the
[Typewriter](https://github.com/gabber235/Typewriter) engine by gabber235 and
contains none of its source. Born To Craft Studio is not affiliated with or
endorsed by the Typewriter project.

The engine itself is **not** free software — its licence forbids redistributing
it. **Get it from the Typewriter project, and never redistribute it**, including
inside a fork of this repository.

Full attribution, the statement of modifications required by §5(a), and the
trademark reservation are in **[NOTICE.md](NOTICE.md)**. Read it before
redistributing.

© 2026 Born To Craft Studio.
