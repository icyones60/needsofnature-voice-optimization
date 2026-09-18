# NeedsOfNature Voice Optimization

为 NeedsOfNature 动画阶段增加可配置语音，并替换 Female Gender Mod 的女性受伤音效。

本仓库公开 `v1.1.4` 的 Java 源码和构建配置。内置音频素材不包含在源码树中，请从 [Releases](../../releases) 下载完整版本。

## 功能

- 按 NeedsOfNature 动画阶段播放 `high`、`climax` 和 `gasping` 三组语音。
- 可在 Mod Menu 中调整音量、延迟和各阶段使用的内置语音包。
- 替换 `wildfire_gender:female_hurt` 受伤音效。
- 仅在客户端运行。

## 触发规则

- 动画最后一个阶段使用 `climax`，立即播放。
- 其他普通阶段使用 `high`，经过配置延迟后播放。
- 动画 ID 或角色 key 含有 `defeat`、`defeated`、`on_back`、`on_belly`、`on_block` 或 `on_wall` 时使用 `gasping`，经过配置延迟后播放。
- 单阶段动画会被视为最后一个阶段，因此使用 `climax`。

## 安装

1. 从 [Releases](../../releases) 下载 `needsofnature-voice-optimization-1.1.4.jar`。
2. 将 JAR 放入 Minecraft 实例的 `mods` 目录。
3. 启动游戏后通过 Mod Menu 打开配置界面。

不要同时安装同一模组的多个版本。

## 文件校验

`v1.1.4` JAR SHA-256：

```text
1D55C011843AE2AFE05C98F79207D1FFF5BC256537F7F3CA9184897F73E27247
```

## 许可

Copyright (c) 2026 Ibukicha. All rights reserved.

除权利人明确书面授权外，不得复制、修改、再发布或用于其他项目。第三方模组及其商标归各自权利人所有。本项目与 NeedsOfNature、Female Gender Mod 的原作者不存在官方隶属关系。

公开源码仅用于查看和审计，不改变上述许可。
