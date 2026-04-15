package com.leclowndu93150.simpletts.tts;

import java.util.concurrent.atomic.AtomicReference;

public class DownloadProgressTracker {

    private static final AtomicReference<String> currentProgress = new AtomicReference<>("");

    public static void startTracking(String voiceName) {
        currentProgress.set("Preparing " + voiceName + "...");
    }

    public static void stopTracking() {
    }

    public static void update(String status) {
        currentProgress.set(status);
    }

    public static String getProgress() {
        return currentProgress.get();
    }

    public static void reset() {
        currentProgress.set("");
    }
}
