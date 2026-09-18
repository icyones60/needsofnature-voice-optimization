# v1.2.1

## 调整

- 将试听按钮文字从“播放随机一条”改为“播放”。
- 试听开始后持续随机播放所选阶段音效，每条结束后按设置页当前循环延迟等待再播放下一条。
- 点击“停止”或退出设置界面后立即终止试听循环。
- 控制设置分类名称改为 `NeedsOfNature Voice Optimization Options`。

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
