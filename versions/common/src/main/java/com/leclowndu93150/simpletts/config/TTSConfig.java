package com.leclowndu93150.simpletts.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leclowndu93150.simpletts.Simpletts;
import com.leclowndu93150.simpletts.tts.TTSLanguage;
import com.leclowndu93150.simpletts.tts.TTSVoice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TTSConfig {

    private static TTSConfig instance;
    private static Path configPath;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String selectedLanguage = TTSLanguage.EN_GB.getLocale();
    public String selectedVoice = "en_GB-alba-medium";
    public int volume = 100;
    public float speed = 1.0f;
    public boolean hearSelf = true;

    public static TTSConfig getInstance() {
        if (instance == null) {
            instance = new TTSConfig();
        }
        return instance;
    }

    public static void init(Path gameDir) {
        configPath = gameDir.resolve("config").resolve("simpletts.json");
        load();
    }

    public static void load() {
        if (configPath == null) return;
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                instance = GSON.fromJson(json, TTSConfig.class);
                if (instance == null) instance = new TTSConfig();
            } catch (IOException e) {
                Simpletts.LOGGER.error("Failed to load config", e);
                instance = new TTSConfig();
            }
        } else {
            instance = new TTSConfig();
            save();
        }
    }

    public static void save() {
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(getInstance()));
        } catch (IOException e) {
            Simpletts.LOGGER.error("Failed to save config", e);
        }
    }

    public TTSLanguage getSelectedLanguage() {
        return TTSLanguage.fromLocale(selectedLanguage);
    }

    public TTSVoice getSelectedVoice() {
        return TTSVoice.fromId(selectedVoice);
    }
}
