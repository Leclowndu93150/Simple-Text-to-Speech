package com.leclowndu93150.simpletts;

import com.leclowndu93150.simpletts.config.TTSConfig;
import com.leclowndu93150.simpletts.config.TTSConfigScreen;
import com.leclowndu93150.simpletts.tts.SpeechHistory;
import com.leclowndu93150.simpletts.tts.TTSEngine;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("simpletts")
public class SimplettsForge {

    public SimplettsForge() {
        Simpletts.init();
        TTSConfig.init(FMLPaths.GAMEDIR.get());
        SpeechHistory.init(FMLPaths.GAMEDIR.get().resolve("config"));
        TTSEngine.getInstance().initialize(TTSConfig.getInstance().getSelectedVoice());

        SimplettsForgeClient.init();

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(TTSConfigScreen::create)
        );
    }
}
