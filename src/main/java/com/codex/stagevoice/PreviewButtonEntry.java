package com.codex.stagevoice;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

final class PreviewButtonEntry extends TooltipListEntry<Object> {
    private static final int PLAY_WIDTH = 52;
    private static final int STOP_WIDTH = 52;
    private static final int BUTTON_HEIGHT = 20;

    private final ButtonWidget playButton;
    private final ButtonWidget stopButton;
    private final BooleanSupplier enabled;
    private final List<ButtonWidget> widgets;

    PreviewButtonEntry(Text label, Runnable play, Runnable stop, BooleanSupplier enabled) {
        super(label, null);
        this.enabled = enabled;
        playButton = ButtonWidget.builder(Text.literal("播放"), button -> play.run())
                .dimensions(0, 0, PLAY_WIDTH, BUTTON_HEIGHT)
                .build();
        stopButton = ButtonWidget.builder(Text.literal("停止"), button -> stop.run())
                .dimensions(0, 0, STOP_WIDTH, BUTTON_HEIGHT)
                .build();
        widgets = List.of(playButton, stopButton);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth,
                       int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        super.render(context, index, y, x, entryWidth, entryHeight,
                mouseX, mouseY, hovered, tickDelta);

        playButton.active = enabled.getAsBoolean();
        stopButton.active = enabled.getAsBoolean();
        stopButton.setPosition(x + entryWidth - STOP_WIDTH, y);
        playButton.setPosition(stopButton.getX() - PLAY_WIDTH - 4, y);

        context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                getDisplayedFieldName(),
                x,
                y + 6,
                getPreferredTextColor());
        playButton.render(context, mouseX, mouseY, tickDelta);
        stopButton.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public int getItemHeight() {
        return BUTTON_HEIGHT + 4;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public List<? extends Element> children() {
        return widgets;
    }

    @Override
    public List<? extends Selectable> narratables() {
        return widgets;
    }
}
