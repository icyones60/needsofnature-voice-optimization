package com.codex.stagevoice;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class StageVoiceModMenu implements ModMenuApi {
    private static final long DEFAULT_DELAY_MILLIS = 600L;
    private static final int DEFAULT_VOLUME_PERCENT = 80;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return StageVoiceModMenu::createConfigScreen;
    }

    static Screen createConfigScreen(Screen parent) {
        StageVoiceConfig config = StageVoiceClient.config();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("NeedsOfNature Voice Optimization 设置"));
        ConfigCategory category = builder.getOrCreateCategory(Text.literal("语音设置"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        category.addEntry(entries.startLongField(Text.literal("循环延迟（毫秒）"), config.delayMillis())
                .setDefaultValue(DEFAULT_DELAY_MILLIS)
                .setMin(0L)
                .setTooltip(Text.literal("适用于 high、gasping 和 climax。每段播完后等待该时间再播放下一段。"))
                .setSaveConsumer(config::setDelayMillis)
                .build());

        int volumePercent = Math.round(config.volume() * 100.0f);
        IntegerSliderEntry volumeEntry = entries.startIntSlider(
                        Text.literal("语音音量"), volumePercent, 0, 500)
                .setDefaultValue(DEFAULT_VOLUME_PERCENT)
                .setTextGetter(value -> Text.literal(value + "%"))
                .setTooltip(Text.literal("100% 以上使用同一音频的并行增益层，音量过高可能失真。"))
                .setSaveConsumer(value -> config.setVolume(value / 100.0f))
                .build();
        category.addEntry(volumeEntry);

        SelectionListEntry<VoicePack> highEntry = packSelector(
                entries,
                "process 音频包",
                config.highPack(),
                VoicePack.DEFAULT,
                config::setHighPack);
        category.addEntry(highEntry);
        category.addEntry(previewEntry("试听 low", highEntry, "low", volumeEntry));
        category.addEntry(previewEntry("试听 med", highEntry, "med", volumeEntry));
        category.addEntry(previewEntry("试听 high", highEntry, "high", volumeEntry));

        SelectionListEntry<VoicePack> gaspingEntry = packSelector(
                entries,
                "gasping 音频包",
                config.gaspingPack(),
                VoicePack.DEFAULT,
                config::setGaspingPack);
        category.addEntry(gaspingEntry);
        category.addEntry(previewEntry("试听 gasping", gaspingEntry, "gasping", volumeEntry));

        SelectionListEntry<VoicePack> climaxEntry = packSelector(
                entries,
                "climax 音频包",
                config.climaxPack(),
                VoicePack.DEFAULT,
                config::setClimaxPack);
        category.addEntry(climaxEntry);
        category.addEntry(previewEntry("试听 climax", climaxEntry, "climax", volumeEntry));

        SelectionListEntry<VoicePack> hurtEntry = packSelector(
                entries,
                "女性受伤音频包（high）",
                config.femaleHurtPack(),
                VoicePack.KATAGIRI_AKI,
                config::setFemaleHurtPack);
        category.addEntry(hurtEntry);
        category.addEntry(previewEntry("试听女性受伤音效", hurtEntry, "high", volumeEntry));

        builder.setSavingRunnable(() -> {
            config.save();
            StageVoiceClient.onConfigSaved();
        });
        return builder.build();
    }

    private static SelectionListEntry<VoicePack> packSelector(
            ConfigEntryBuilder entries,
            String label,
            VoicePack current,
            VoicePack defaultPack,
            java.util.function.Consumer<VoicePack> saveConsumer) {
        return entries.startSelector(Text.literal(label), VoicePack.VALUES, current)
                .setDefaultValue(defaultPack)
                .setNameProvider(pack -> Text.literal(pack.displayName()))
                .setSaveConsumer(saveConsumer)
                .build();
    }

    private static PreviewButtonEntry previewEntry(
            String label,
            SelectionListEntry<VoicePack> packEntry,
            String band,
            IntegerSliderEntry volumeEntry) {
        return new PreviewButtonEntry(
                Text.literal(label),
                () -> StageVoiceClient.preview(
                        packEntry.getValue(),
                        band,
                        volumeEntry.getValue() / 100.0f,
                        packEntry.getConfigScreen()),
                StageVoiceClient::stopPreview,
                StageVoiceClient::canPreview);
    }
}
