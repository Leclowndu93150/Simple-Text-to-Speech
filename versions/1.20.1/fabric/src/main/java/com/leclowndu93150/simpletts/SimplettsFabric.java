package com.leclowndu93150.simpletts;

import com.leclowndu93150.simpletts.config.TTSConfig;
import com.leclowndu93150.simpletts.tts.SpeechHistory;
import com.leclowndu93150.simpletts.tts.TTSEngine;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class SimplettsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Simpletts.init();
        TTSConfig.init(FabricLoader.getInstance().getGameDir());
        SpeechHistory.init(FabricLoader.getInstance().getGameDir().resolve("config"));
        TTSEngine.getInstance().initialize(TTSConfig.getInstance().getSelectedVoice());
    }
}
