package com.leclowndu93150.simpletts.client;

import com.leclowndu93150.simpletts.tts.SpeechHistory;
import com.leclowndu93150.simpletts.voicechat.TTSVoicechatPlugin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SpeakScreen extends Screen {

    private EditBox textInput;
    private Button speakButton;
    private Button stopButton;
    private Button clearHistoryButton;
    private int scrollOffset;

    public SpeakScreen() {
        super(Component.literal("Simple TTS"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int inputWidth = Math.min(300, width - 40);

        textInput = new EditBox(font, centerX - inputWidth / 2, 30, inputWidth, 20, Component.literal("Type to speak..."));
        textInput.setMaxLength(256);
        textInput.setHint(Component.literal("Type to speak..."));
        addWidget(textInput);
        setInitialFocus(textInput);

        speakButton = Button.builder(Component.literal("Speak"), btn -> {
            String text = textInput.getValue().trim();
            if (!text.isEmpty()) {
                TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
                if (plugin != null && plugin.isConnected()) {
                    plugin.speak(text);
                    SpeechHistory.add(text);
                    textInput.setValue("");
                }
            }
        }).bounds(centerX - inputWidth / 2, 56, (inputWidth - 8) / 3, 20).build();

        stopButton = Button.builder(Component.literal("Stop"), btn -> {
            TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
            if (plugin != null) {
                plugin.stopSpeaking();
            }
        }).bounds(centerX - inputWidth / 2 + (inputWidth - 8) / 3 + 4, 56, (inputWidth - 8) / 3, 20).build();

        clearHistoryButton = Button.builder(Component.literal("Clear History"), btn -> {
            SpeechHistory.clear();
        }).bounds(centerX - inputWidth / 2 + 2 * ((inputWidth - 8) / 3 + 4), 56, (inputWidth - 8) / 3, 20).build();

        addRenderableWidget(speakButton);
        addRenderableWidget(stopButton);
        addRenderableWidget(clearHistoryButton);

        scrollOffset = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        textInput.render(graphics, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int inputWidth = Math.min(300, width - 40);
        int listX = centerX - inputWidth / 2;
        int listY = 84;
        int entryHeight = 14;

        TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
        if (plugin != null && plugin.isSpeaking()) {
            graphics.drawCenteredString(font, "Speaking...", centerX, listY, 0x55FF55);
            listY += 14;
        } else if (plugin == null || !plugin.isConnected()) {
            graphics.drawCenteredString(font, "Voice chat not connected", centerX, listY, 0xFF5555);
            listY += 14;
        }

        graphics.drawString(font, "History:", listX, listY, 0xAAAAAA);
        listY += 12;

        List<String> history = SpeechHistory.getHistory();
        int maxVisible = (height - listY - 10) / entryHeight;

        for (int i = scrollOffset; i < Math.min(history.size(), scrollOffset + maxVisible); i++) {
            String entry = history.get(i);
            String display = entry.length() > 45 ? entry.substring(0, 42) + "..." : entry;

            int entryY = listY + (i - scrollOffset) * entryHeight;
            boolean hovered = mouseX >= listX && mouseX <= listX + inputWidth
                    && mouseY >= entryY && mouseY < entryY + entryHeight;

            if (hovered) {
                graphics.fill(listX - 2, entryY - 1, listX + inputWidth + 2, entryY + entryHeight - 1, 0x40FFFFFF);
            }

            graphics.drawString(font, display, listX, entryY, hovered ? 0xFFFFFF : 0xCCCCCC);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int centerX = width / 2;
        int inputWidth = Math.min(300, width - 40);
        int listX = centerX - inputWidth / 2;
        int listY = 84;
        int entryHeight = 14;

        TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
        if (plugin != null && (plugin.isSpeaking() || !plugin.isConnected())) {
            listY += 14;
        }
        listY += 12;

        List<String> history = SpeechHistory.getHistory();
        int maxVisible = (height - listY - 10) / entryHeight;

        for (int i = scrollOffset; i < Math.min(history.size(), scrollOffset + maxVisible); i++) {
            int entryY = listY + (i - scrollOffset) * entryHeight;
            if (mouseX >= listX && mouseX <= listX + inputWidth
                    && mouseY >= entryY && mouseY < entryY + entryHeight) {
                String entry = history.get(i);
                if (button == 0) {
                    textInput.setValue(entry);
                } else if (button == 1 && plugin != null && plugin.isConnected()) {
                    plugin.speak(entry);
                    SpeechHistory.add(entry);
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, SpeechHistory.getHistory().size() - 10);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && textInput.isFocused()) {
            speakButton.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
