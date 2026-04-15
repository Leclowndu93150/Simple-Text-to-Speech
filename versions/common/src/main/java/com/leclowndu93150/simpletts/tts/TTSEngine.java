package com.leclowndu93150.simpletts.tts;

import com.leclowndu93150.simpletts.Simpletts;
import org.pitest.voices.Chorus;
import org.pitest.voices.ChorusConfig;
import org.pitest.voices.audio.Audio;
import org.pitest.voices.Voice;
import org.pitest.voices.us.EnUsDictionary;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class TTSEngine {

    private static TTSEngine instance;

    private Chorus chorus;
    private Voice currentVoice;
    private TTSVoice currentVoiceType;
    private Path cacheBase;
    private final AtomicReference<String> downloadStatus = new AtomicReference<>("");
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SimpleTTS-Engine");
        t.setDaemon(true);
        return t;
    });

    private TTSEngine() {
    }

    public static TTSEngine getInstance() {
        if (instance == null) {
            instance = new TTSEngine();
        }
        return instance;
    }

    public void initialize(TTSVoice voice) {
        try {
            ChorusConfig config = ChorusConfig.chorusConfig(EnUsDictionary.en_us());
            cacheBase = config.base();
            chorus = new Chorus(config);
            setVoice(voice);
            Simpletts.LOGGER.info("TTS Engine initialized with voice: {}", voice.getDisplayName());
        } catch (Exception e) {
            Simpletts.LOGGER.error("Failed to initialize TTS engine", e);
        }
    }

    public void setVoice(TTSVoice voice) {
        if (chorus == null) {
            return;
        }
        try {
            sanitizeCachedModelConfig(voice);
            currentVoice = chorus.voice(voice.getModel());
            currentVoiceType = voice;
            Simpletts.LOGGER.info("Voice set to: {}", voice.getDisplayName());
        } catch (Exception e) {
            try {
                if (sanitizeCachedModelConfig(voice)) {
                    currentVoice = chorus.voice(voice.getModel());
                    currentVoiceType = voice;
                    return;
                }
            } catch (Exception retryError) {
                e.addSuppressed(retryError);
            }
            Simpletts.LOGGER.error("Failed to set voice: {}", voice.getDisplayName(), e);
        }
    }

    public TTSVoice getCurrentVoiceType() {
        return currentVoiceType;
    }

    public CompletableFuture<short[]> synthesize(String text) {
        return CompletableFuture.supplyAsync(() -> {
            if (currentVoice == null) {
                Simpletts.LOGGER.warn("TTS engine not initialized");
                return new short[0];
            }
            try {
                Audio audio = currentVoice.say(text);
                return resampleTo48kHz(audio.getSamples(), audio.getSampleRate());
            } catch (Exception e) {
                Simpletts.LOGGER.error("Failed to synthesize: {}", text, e);
                return new short[0];
            }
        }, executor);
    }

    public short[] synthesizeSync(String text) {
        if (currentVoice == null) return new short[0];
        try {
            Audio audio = currentVoice.say(text);
            return resampleTo48kHz(audio.getSamples(), audio.getSampleRate());
        } catch (Exception e) {
            Simpletts.LOGGER.error("Failed to synthesize: {}", text, e);
            return new short[0];
        }
    }

    private short[] resampleTo48kHz(float[] samples, int sourceSampleRate) {
        if (sourceSampleRate == 48000) {
            short[] result = new short[samples.length];
            for (int i = 0; i < samples.length; i++) {
                result[i] = (short) (clamp(samples[i], -1.0f, 1.0f) * 32767);
            }
            return result;
        }

        double ratio = 48000.0 / sourceSampleRate;
        int newLength = (int) (samples.length * ratio);
        short[] result = new short[newLength];

        for (int i = 0; i < newLength; i++) {
            double srcIndex = i / ratio;
            int srcInt = (int) srcIndex;
            double frac = srcIndex - srcInt;

            float sample;
            if (srcInt + 1 < samples.length) {
                sample = (float) (samples[srcInt] * (1.0 - frac) + samples[srcInt + 1] * frac);
            } else if (srcInt < samples.length) {
                sample = samples[srcInt];
            } else {
                sample = 0;
            }

            result[i] = (short) (clamp(sample, -1.0f, 1.0f) * 32767);
        }

        return result;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean isVoiceDownloaded(TTSVoice voice) {
        if (!voice.requiresDownload()) return true;
        if (cacheBase == null) {
            cacheBase = Path.of(System.getProperty("user.home"), ".cache", "voices");
        }
        try {
            Path modelDir = cacheBase.resolve("vits-piper-" + voice.getId());
            boolean exists = Files.exists(modelDir);
            boolean hasFiles = exists && Files.list(modelDir).findAny().isPresent();
            return exists && hasFiles;
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<Boolean> downloadVoice(TTSVoice voice) {
        return CompletableFuture.supplyAsync(() -> {
            if (!voice.requiresDownload()) return true;
            if (isVoiceDownloaded(voice)) return true;

            downloadStatus.set("Downloading " + voice.getDisplayName() + "...");
            try {
                if (chorus == null) {
                    initialize(TTSVoice.fromId("en_GB-alba-medium"));
                }
                chorus.voice(voice.getModel());
                downloadStatus.set("Downloaded " + voice.getDisplayName());
                return true;
            } catch (Exception e) {
                Simpletts.LOGGER.error("Failed to download voice: {}", voice.getDisplayName(), e);
                downloadStatus.set("Failed to download " + voice.getDisplayName());
                return false;
            }
        }, executor);
    }

    public String getDownloadStatus() {
        return downloadStatus.get();
    }

    public boolean isInitialized() {
        return chorus != null && currentVoice != null;
    }

    public void shutdown() {
        executor.shutdownNow();
        if (chorus != null) {
            try {
                chorus.close();
            } catch (Exception e) {
                Simpletts.LOGGER.error("Failed to close TTS engine", e);
            }
            chorus = null;
            currentVoice = null;
            currentVoiceType = null;
        }
    }

    private boolean sanitizeCachedModelConfig(TTSVoice voice) throws Exception {
        if (voice == null || cacheBase == null || !voice.requiresDownload()) {
            return false;
        }

        Path configPath = cacheBase
                .resolve("vits-piper-" + voice.getId())
                .resolve(voice.getId() + ".onnx.json");
        if (!Files.exists(configPath)) {
            return false;
        }

        String text = Files.readString(configPath, StandardCharsets.UTF_8);
        String escaped = escapeNonAsciiJson(text);
        if (text.equals(escaped)) {
            return false;
        }

        Files.writeString(configPath, escaped, StandardCharsets.UTF_8);
        return true;
    }

    private String escapeNonAsciiJson(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        text.codePoints().forEach(codePoint -> {
            if (codePoint >= 0x20 && codePoint <= 0x7E) {
                escaped.append((char) codePoint);
                return;
            }
            if (codePoint == '\n' || codePoint == '\r' || codePoint == '\t') {
                escaped.append((char) codePoint);
                return;
            }
            if (codePoint <= 0xFFFF) {
                escaped.append(String.format("\\u%04X", codePoint));
                return;
            }
            for (char surrogate : Character.toChars(codePoint)) {
                escaped.append(String.format("\\u%04X", (int) surrogate));
            }
        });
        return escaped.toString();
    }
}
