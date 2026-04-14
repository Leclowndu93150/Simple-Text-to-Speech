package com.leclowndu93150.simpletts.config;

import com.leclowndu93150.simpletts.tts.TTSEngine;
import com.leclowndu93150.simpletts.tts.TTSLanguage;
import com.leclowndu93150.simpletts.tts.TTSVoice;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.cycling.ICyclingController;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public class DynamicVoiceController implements ICyclingController<String> {

    private final Option<String> option;
    private final Supplier<TTSLanguage> languageSupplier;

    public DynamicVoiceController(Option<String> option, Supplier<TTSLanguage> languageSupplier) {
        this.option = option;
        this.languageSupplier = languageSupplier;
    }

    private List<String> getVoiceIds() {
        return TTSVoice.getVoicesForLanguage(languageSupplier.get()).stream()
                .map(TTSVoice::getId).toList();
    }

    @Override
    public Option<String> option() {
        return option;
    }

    @Override
    public Component formatValue() {
        String id = option.pendingValue();
        TTSVoice voice = TTSVoice.fromId(id);
        TTSEngine engine = TTSEngine.getInstance();
        String status;
        if (voice.isBundled()) {
            status = "Bundled";
        } else if (engine.isVoiceDownloaded(voice)) {
            status = "Ready";
        } else {
            status = "Download";
        }
        return Component.literal(voice.getDisplayName() + " [" + status + "]");
    }

    @Override
    public void setPendingValue(int ordinal) {
        List<String> ids = getVoiceIds();
        if (!ids.isEmpty() && ordinal >= 0 && ordinal < ids.size()) {
            option.requestSet(ids.get(ordinal));
        }
    }

    @Override
    public int getPendingValue() {
        List<String> ids = getVoiceIds();
        int idx = ids.indexOf(option.pendingValue());
        return Math.max(idx, 0);
    }

    @Override
    public int getCycleLength() {
        return Math.max(getVoiceIds().size(), 1);
    }
}
