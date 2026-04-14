package com.leclowndu93150.simpletts;

import com.leclowndu93150.simpletts.config.TTSConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class SimplettsModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TTSConfigScreen::create;
    }
}
