package com.leclowndu93150.simpletts.tts;

import com.leclowndu93150.simpletts.Simpletts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpeechHistory {

    private static final int MAX_ENTRIES = 50;
    private static Path historyFile;
    private static final List<String> history = new ArrayList<>();

    public static void init(Path configDir) {
        historyFile = configDir.resolve("simpletts_history.txt");
        load();
    }

    public static void add(String text) {
        history.remove(text);
        history.add(0, text);
        while (history.size() > MAX_ENTRIES) {
            history.remove(history.size() - 1);
        }
        save();
    }

    public static List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public static void clear() {
        history.clear();
        save();
    }

    private static void load() {
        if (historyFile == null || !Files.exists(historyFile)) return;
        try {
            List<String> lines = Files.readAllLines(historyFile);
            history.clear();
            for (String line : lines) {
                if (!line.isBlank()) {
                    history.add(line);
                }
            }
        } catch (IOException e) {
            Simpletts.LOGGER.error("Failed to load speech history", e);
        }
    }

    private static void save() {
        if (historyFile == null) return;
        try {
            Files.createDirectories(historyFile.getParent());
            Files.write(historyFile, history);
        } catch (IOException e) {
            Simpletts.LOGGER.error("Failed to save speech history", e);
        }
    }
}
