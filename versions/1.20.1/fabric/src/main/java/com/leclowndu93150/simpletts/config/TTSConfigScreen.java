package com.leclowndu93150.simpletts.config;

import com.leclowndu93150.simpletts.Simpletts;
import com.leclowndu93150.simpletts.tts.DownloadProgressTracker;
import com.leclowndu93150.simpletts.tts.TTSEngine;
import com.leclowndu93150.simpletts.tts.TTSLanguage;
import com.leclowndu93150.simpletts.tts.TTSVoice;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TTSConfigScreen {

    private static final AtomicReference<String> previewStatus = new AtomicReference<>("Download & Preview");

    public static Screen create(Screen parent) {
        TTSConfig config = TTSConfig.getInstance();
        TTSConfig defaults = new TTSConfig();
        TTSEngine engine = TTSEngine.getInstance();

        Option<TTSLanguage> languageOption = Option.<TTSLanguage>createBuilder()
                .name(Component.literal("Language"))
                .description(OptionDescription.of(Component.literal(
                        "Choose the language. The voice list updates automatically.")))
                .binding(
                        TTSLanguage.fromLocale(defaults.selectedLanguage),
                        () -> TTSLanguage.fromLocale(config.selectedLanguage),
                        v -> config.selectedLanguage = v.getLocale()
                )
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(TTSLanguage.class)
                        .valueFormatter(v -> {
                            int count = TTSVoice.getVoicesForLanguage(v).size();
                            return Component.literal(v.getDisplayName() + " (" + count + " voices)");
                        }))
                .build();

        Option<String> voiceOption = Option.<String>createBuilder()
                .name(Component.literal("Voice"))
                .description(OptionDescription.of(Component.literal(
                        "Select a voice for the chosen language. "
                                + "Bundled voices work offline. Others download on first use (~60MB each).")))
                .binding(
                        defaults.selectedVoice,
                        () -> config.selectedVoice,
                        v -> config.selectedVoice = v
                )
                .customController(opt -> new DynamicVoiceController(opt,
                        () -> languageOption.pendingValue()))
                .build();

        languageOption.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                TTSLanguage lang = opt.pendingValue();
                List<TTSVoice> voices = TTSVoice.getVoicesForLanguage(lang);
                if (!voices.isEmpty()) {
                    voiceOption.requestSet(voices.get(0).getId());
                }
            }
        });

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Simple TTS Configuration"))
                .save(() -> {
                    TTSConfig.save();
                    TTSVoice selected = config.getSelectedVoice();
                    if (engine.getCurrentVoiceType() != selected) {
                        new Thread(() -> {
                            if (!engine.isInitialized()) {
                                engine.initialize(selected);
                            } else {
                                engine.setVoice(selected);
                            }
                        }, "SimpleTTS-VoiceLoad").start();
                    }
                })

                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Voice"))
                        .tooltip(Component.literal("Voice and playback settings"))

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Language & Voice"))
                                .description(OptionDescription.of(Component.literal(
                                        "Select a language, then cycle through available voices.")))

                                .option(languageOption)

                                .option(voiceOption)

                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal(""))
                                        .description(OptionDescription.of(Component.literal(
                                                "Downloads the voice if needed, then plays a sample.")))
                                        .binding(false, () -> false, v -> {})
                                        .customController(opt -> new DynamicButtonController(opt,
                                                () -> {
                                                    String progress = DownloadProgressTracker.getProgress();
                                                    if (!progress.isEmpty() && !previewStatus.get().equals("Download & Preview")) {
                                                        return Component.literal(progress);
                                                    }
                                                    return Component.literal(previewStatus.get());
                                                },
                                                o -> {
                                                    TTSVoice voice = TTSVoice.fromId(voiceOption.pendingValue());
                                                    o.setAvailable(false);
                                                    previewStatus.set("Preparing...");
                                                    new Thread(() -> {
                                                        try {
                                                            if (!engine.isInitialized()) {
                                                                DownloadProgressTracker.startTracking(voice.getDisplayName());
                                                                engine.initialize(voice);
                                                                DownloadProgressTracker.stopTracking();
                                                            } else if (engine.getCurrentVoiceType() != voice) {
                                                                DownloadProgressTracker.startTracking(voice.getDisplayName());
                                                                engine.setVoice(voice);
                                                                DownloadProgressTracker.stopTracking();
                                                            }
                                                            previewStatus.set("Synthesizing...");
                                                            short[] audio = engine.synthesizeSync(
                                                                    "Hello! This is a preview of the " + voice.getDisplayName() + " voice.");
                                                            if (audio.length > 0) {
                                                                previewStatus.set("Playing...");
                                                                playPreviewAudio(audio);
                                                            }
                                                        } catch (Exception e) {
                                                            previewStatus.set("Error: " + e.getMessage());
                                                        } finally {
                                                            o.setAvailable(true);
                                                            DownloadProgressTracker.reset();
                                                            previewStatus.set("Download & Preview");
                                                        }
                                                    }, "SimpleTTS-Preview").start();
                                                }))
                                        .build())

                                .build())

                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Playback"))
                                .description(OptionDescription.of(Component.literal("Volume and speed settings")))

                                .option(Option.<Integer>createBuilder()
                                        .name(Component.literal("Volume"))
                                        .description(OptionDescription.of(Component.literal("TTS playback volume")))
                                        .binding(defaults.volume, () -> config.volume, v -> config.volume = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 200)
                                                .step(5)
                                                .valueFormatter(v -> Component.literal(v + "%")))
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Component.literal("Speed"))
                                        .description(OptionDescription.of(Component.literal("TTS speech speed")))
                                        .binding(defaults.speed, () -> config.speed, v -> config.speed = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.5f, 2.0f)
                                                .step(0.1f)
                                                .valueFormatter(v -> Component.literal(String.format("%.1fx", v))))
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Hear Self"))
                                        .description(OptionDescription.of(Component.literal(
                                                "Hear your own TTS audio locally")))
                                        .binding(defaults.hearSelf, () -> config.hearSelf, v -> config.hearSelf = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                        .build())

                                .build())

                        .build())

                .build()
                .generateScreen(parent);
    }

    private static void playPreviewAudio(short[] audio) {
        Thread thread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(48000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                byte[] bytes = new byte[audio.length * 2];
                for (int i = 0; i < audio.length; i++) {
                    bytes[i * 2] = (byte) (audio[i] & 0xFF);
                    bytes[i * 2 + 1] = (byte) ((audio[i] >> 8) & 0xFF);
                }

                line.write(bytes, 0, bytes.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                Simpletts.LOGGER.error("Failed to play preview audio", e);
            }
        }, "SimpleTTS-Preview");
        thread.setDaemon(true);
        thread.start();
    }
}
