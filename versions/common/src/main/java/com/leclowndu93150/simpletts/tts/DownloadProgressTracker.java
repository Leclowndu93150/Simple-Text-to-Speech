package com.leclowndu93150.simpletts.tts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class DownloadProgressTracker {

    private static final AtomicReference<String> currentProgress = new AtomicReference<>("");
    private static volatile boolean tracking = false;

    public static void startTracking(String voiceName) {
        currentProgress.set("Downloading " + voiceName + "...");
        tracking = true;

        Thread tracker = new Thread(() -> {
            try {
                Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
                while (tracking) {
                    try {
                        long totalSize = 0;
                        for (Path p : Files.list(tempDir).toList()) {
                            if (p.getFileName().toString().startsWith("voices-model") && Files.isDirectory(p)) {
                                for (Path f : Files.list(p).toList()) {
                                    totalSize += Files.size(f);
                                }
                            }
                        }
                        if (totalSize > 0) {
                            long mb = totalSize / (1024 * 1024);
                            currentProgress.set("Downloading " + voiceName + "... " + mb + "MB");
                        }
                    } catch (IOException ignored) {
                    }
                    Thread.sleep(500);
                }
            } catch (InterruptedException ignored) {
            }
        }, "SimpleTTS-DownloadTracker");
        tracker.setDaemon(true);
        tracker.start();
    }

    public static void stopTracking() {
        tracking = false;
    }

    public static void update(String status) {
        currentProgress.set(status);
    }

    public static String getProgress() {
        return currentProgress.get();
    }

    public static void reset() {
        tracking = false;
        currentProgress.set("");
    }
}
