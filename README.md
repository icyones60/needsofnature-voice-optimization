# NeedsOfNature Voice Optimization

Adds configurable voice audio to NeedsOfNature animation stages and replaces the female hurt sound from Female Gender Mod.

This repository contains the Java source code and build configuration. Bundled audio assets are not included in the source tree. Download the complete version from [Releases](../../releases).

## Features

- Plays `high`, `climax`, and `gasping` voice groups based on the current NeedsOfNature animation stage.
- Provides Mod Menu settings for volume, playback delay, and the bundled voice pack used by each stage.
- Adds a configurable control binding that opens the voice settings in game.
- Loops random previews for each configured voice group using the current playback delay until stopped or the settings screen closes.
- Replaces the `wildfire_gender:female_hurt` sound.
- Runs on the client only.

## Trigger Rules

- The final animation stage uses `climax` and starts playing immediately.
- Other regular stages use `high` after the configured delay.
- `gasping` is used after the configured delay when an animation ID or actor key contains `defeat`, `defeated`, `on_back`, `on_belly`, `on_block`, or `on_wall`.
- A single-stage animation is treated as its final stage and therefore uses `climax`.

## Installation

1. Download `needsofnature-voice-optimization-1.2.1.jar` from [Releases](../../releases).
2. Place the JAR in the `mods` directory of your Minecraft instance.
3. Start the game and open the configuration screen through Mod Menu, or bind `Open Voice Settings` under `NeedsOfNature Voice Optimization Options` in the controls screen.

Do not install multiple versions of this mod at the same time.

## File Verification

SHA-256 for the `v1.2.1` JAR:

```text
EAC9E12866538C79E4AA55B165831128C6261ABA58CCA7ED730F1AFBA77063DD
```

## License

Copyright (c) 2026 Ibukicha. All rights reserved.
