# NeedsOfNature Voice Optimization

该模组为NON不同动画阶段配置了音频，可在设置中选择音频包和播放频率。

Adds configurable voice audio to NeedsOfNature animation stages and replaces the female hurt sound from Female Gender Mod.

This repository contains the Java source code and build configuration. Bundled audio assets are not included in the source tree. Download the complete version from [Releases](../../releases).

## Features

- Plays progressive `low`, `med`, `high`, and `climax` voice groups based on the current NeedsOfNature animation stage.
- Provides Mod Menu settings for volume, playback delay, and the bundled voice pack used by each stage.
- Adds a configurable control binding that opens the voice settings in game.
- Adds play and stop controls below each voice pack selector for previewing one random clip.
- Adds the `NON音频优化` key category with `打开音效设置` and `打开调试功能` bindings; debug mode reports stage and audio band changes in chat without printing every tick.
- Replaces the `wildfire_gender:female_hurt` sound.
- Runs on the client only.

## Supported Environment

- Minecraft: `1.21.11`
- Mod loader: Fabric Loader `0.18.5` or later
- Java: `21` or later
- Runtime side: client only
- Forge and NeoForge are not supported.
- No other Minecraft versions are currently confirmed to be supported.

## Required Dependencies

- Fabric API for Minecraft `1.21.11`
- NeedsOfNature `1.4.4` or later
- Mod Menu `17.0.0` or later
- Cloth Config `21.11.153` or later

This mod was developed using NeedsOfNature `1.5.1+1.21.11` as its reference environment. Although the mod metadata allows version `1.4.4` or later, not every version has been compatibility-tested.

## Optional Integration

Female Gender Mod is not a required dependency. When installed, this mod replaces its `wildfire_gender:female_hurt` sound. Without Female Gender Mod, the NeedsOfNature animation stage voices and configuration features remain available.

## Conflicts and Compatibility Risks

There are currently no confirmed mod conflicts, but compatibility issues may occur in the following cases:

- Installing multiple versions of this mod at the same time may cause duplicate mod ID or sound resource conflicts.
- Mods that modify NeedsOfNature's internal animation runtime may prevent stage voices from triggering correctly.
- Mods that also intercept player `playSound` calls or replace `wildfire_gender:female_hurt` may produce results that depend on Mixin loading order.

## Trigger Rules

- Two-stage multi-player animations use `high` then `climax`.
- Single-player animations use `low`, then `med` for the first half of the pre-climax stages, then `high`, followed by `climax` (for example, four stages use `low`, `med`, `high`, `climax`; seven stages use `low`, `med`, `med`, `med`, `high`, `high`, `climax`).
- Multi-player animations use `high` then `climax` for two stages, `low`, `high`, then `climax` for three stages, `low`, `med`, `high`, then `climax` for four stages, and `low`, `med`, then `high` for all remaining pre-climax stages for five or more stages.
- Stage audio except `climax` starts after the configured delay.
- `gasping` is used after the configured delay when animation metadata or identifiers contain defeat, block, wall, fence, slime-wall, or prone-state markers.
- A single-stage animation is treated as its final stage and therefore uses `climax`.

## Installation

1. Download `needsofnature-voice-optimization-1.3.5.jar` from [Releases](../../releases).
2. Place the JAR in the `mods` directory of your Minecraft instance.
3. Start the game and open the configuration screen through Mod Menu, or use `打开音效设置` under the `NON音频优化` key category in the controls screen. Use `打开调试功能` to toggle chat diagnostics.

Do not install multiple versions of this mod at the same time.

## File Verification

SHA-256 for the `v1.2.2` JAR:

```text
846C6CD7A6103532065AF7456C7BB6504EB3A9A28F38F6C792719C01951C2571
```

## License

Copyright (c) 2026 Ibukicha. All rights reserved.
