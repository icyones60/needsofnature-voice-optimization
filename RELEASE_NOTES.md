# v1.3.5

## 功能

- 新增 `low`、`med` 音频阶段，支持按 NeedsOfNature 动画阶段渐进播放语音。
- 阶段播放规则调整为：
  - 多 player 双阶段：`high → climax`
  - 单 player 双阶段：`low → climax`
  - 单 player 三阶段：`low → med → climax`
  - 单 player 四阶段：`low → med → high → climax`
  - 单 player 五阶段：`low → med → med → high → climax`
  - 单 player 七阶段：`low → med → med → med → high → high → climax`
  - 多 player 三阶段：`low → high → climax`
  - 多 player 四阶段：`low → med → high → climax`
  - 多 player 五阶段及以上：`low → med → high → high → ... → climax`
- 扩展 `gasping` 状态识别，读取动画定义的 `contentTags`，适配 `on_a_block`、`against_block`、`on_a_wall`、`against_wall`、`stuckfence`、`slimewall`、`on_knees`、`pronebone` 等标记。
- 内置 11 组音色的 `low`、`med`、`high`、`gasping` 和 `climax` 音频资源，共 1,320 个 OGG 音频。
- 设置页将原来的 `high 音频包` 显示为 `process 音频包`，并增加 `low`、`med` 音频试听入口。

## 按键与调试

- 控制设置中的按键分类改为 `NON音频优化`。
- 设置入口为 `打开音效设置`。
- 新增 `打开调试功能` 按键，用于切换阶段调试信息。
- 开启调试后，聊天栏会显示本地玩家的动画阶段名、阶段序号、当前音频 band（`low`、`med`、`high`、`climax` 或 `gasping`）和音色包名。
- 调试信息仅在阶段或音频 band 变化时输出，不会每 tick 刷屏。

## 兼容性

运行环境和依赖要求与之前版本相同：Minecraft 1.21.11、Fabric Loader 0.18.5+、Java 21+、NeedsOfNature 1.4.4+、Mod Menu 17.0.0+ 和 Cloth Config 21.11.153+。

## 模组信息

- 网站：https://github.com/icyones60/needsofnature-voice-optimization
- 问题反馈：https://github.com/icyones60/needsofnature-voice-optimization/issues

## English

### Features

- Added five stage-based audio groups: `low`, `med`, `high`, `climax`, and `gasping`.
- Audio now changes according to the current NeedsOfNature animation stage.
- Fixed an issue where entering a single-player animation played the `high` audio immediately.
- Added Play and Stop buttons below each voice pack selector. Play previews a random clip, while Stop stops the currently playing voice clip.
- Updated and optimized the audio settings screen for the new stage audio options.

### Key Bindings and Debugging

- Added the `NON Audio Optimization` key category in the Controls menu.
- You can bind `Open Debug Features` and `Open Voice Settings` in this category for quick access.
- When debug mode is enabled, the currently playing audio group and animation stage are displayed in the chat in real time.

# v1.3.0

## 功能

- 新增 `low` 和 `med` 音频阶段。
- 双阶段动画保持 `high → climax`。
- 三阶段动画使用 `low → high → climax`。
- 四阶段及以上动画使用 `low → med → ... → high → climax`。
- 扩展 `gasping` 标签识别，适配 `on_a_block`、`against_block`、`on_a_wall`、`against_wall`、`stuckfence`、`slimewall`、`on_knees` 和 `pronebone`。
- 内置 11 组音色的 `low`、`med`、`high`、`gasping` 和 `climax` 音频。

# v1.2.2

## 变更

- 移除 `memeno-hina-sounds` 音效包及其声音注册。
- `03memeno-hina` 音效包不受影响，继续保留。

# v1.2.1

## 功能

- 音效包选择项下方新增“播放”和“停止”按钮，“播放”随机试听一条，“停止”停止播放当前语音。
- 现在可以在 `NeedsOfNature Voice Optimization Options` 分类中，为“打开音效设置”绑定按键，方便直接调整。

# v1.2.0

## 功能

- 在 Minecraft 控制设置中加入“打开音效设置”键位，默认不绑定按键。
- 设置页为 `high`、`gasping`、`climax` 和女性受伤音效加入随机试听与停止按钮。
- 试听使用当前界面选择的音效包和音量，无需先保存设置。
- 离开设置页、退出世界或开始另一段试听时自动停止上一段试听。

## 兼容性

运行环境和依赖要求与 v1.1.4 相同。

# v1.1.4

首个 GitHub 发布版本。

## 功能

- 根据 NeedsOfNature 动画阶段播放 `high`、`climax`、`gasping` 语音。
- 支持在 Mod Menu 中配置音量、延迟及各阶段语音包。
- 支持替换 Female Gender Mod 的 `wildfire_gender:female_hurt` 音效。
- 内置多组语音资源。

## 环境

Minecraft 1.21.11、Fabric Loader 0.18.5+、Java 21+。需要 Fabric API、NeedsOfNature 1.4.4+、Mod Menu 17.0.0+ 和 Cloth Config 21.11.153+。

已针对 NeedsOfNature 1.5.1+1.21.11 使用；未来版本兼容性未经保证。

## 校验

SHA-256：`1D55C011843AE2AFE05C98F79207D1FFF5BC256537F7F3CA9184897F73E27247`
