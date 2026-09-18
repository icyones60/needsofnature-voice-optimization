# NeedsOfNature Voice Optimization

Adds configurable voice audio to NeedsOfNature animation stages and replaces the female hurt sound from Female Gender Mod.

This repository contains the Java source code and build configuration for `v1.1.4`. Bundled audio assets are not included in the source tree. Download the complete version from [Releases](../../releases).

## Features

- Plays `high`, `climax`, and `gasping` voice groups based on the current NeedsOfNature animation stage.
- Provides Mod Menu settings for volume, playback delay, and the bundled voice pack used by each stage.
- Replaces the `wildfire_gender:female_hurt` sound.
- Runs on the client only.

## Trigger Rules

- The final animation stage uses `climax` and starts playing immediately.
- Other regular stages use `high` after the configured delay.
- `gasping` is used after the configured delay when an animation ID or actor key contains `defeat`, `defeated`, `on_back`, `on_belly`, `on_block`, or `on_wall`.
- A single-stage animation is treated as its final stage and therefore uses `climax`.

## Installation

1. Download `needsofnature-voice-optimization-1.1.4.jar` from [Releases](../../releases).
2. Place the JAR in the `mods` directory of your Minecraft instance.
3. Start the game and open the configuration screen through Mod Menu.

Do not install multiple versions of this mod at the same time.

## File Verification

SHA-256 for the `v1.1.4` JAR:

```text
1D55C011843AE2AFE05C98F79207D1FFF5BC256537F7F3CA9184897F73E27247
```

## License

Copyright (c) 2026 Ibukicha. All rights reserved.
