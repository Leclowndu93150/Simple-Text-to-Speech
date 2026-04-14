package com.leclowndu93150.simpletts.voicechat;

import com.leclowndu93150.simpletts.Simpletts;
import com.leclowndu93150.simpletts.config.TTSConfig;
import com.leclowndu93150.simpletts.tts.TTSEngine;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MergeClientSoundEvent;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TTSVoicechatPlugin implements VoicechatPlugin {

    public static final String PLUGIN_ID = "simpletts";
    private static final int FRAME_SIZE = 960;

    private static TTSVoicechatPlugin instance;
    private VoicechatClientApi clientApi;
    private final ConcurrentLinkedQueue<short[]> audioQueue = new ConcurrentLinkedQueue<>();
    private boolean speaking;
    private volatile SourceDataLine localPlaybackLine;

    public TTSVoicechatPlugin() {
        instance = this;
    }

    public static TTSVoicechatPlugin getInstance() {
        return instance;
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        Simpletts.LOGGER.info("Simple TTS voice chat plugin initialized");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientVoicechatConnectionEvent.class, this::onConnection);
        registration.registerEvent(MergeClientSoundEvent.class, this::onMergeSound);
    }

    private void onConnection(ClientVoicechatConnectionEvent event) {
        if (event.isConnected()) {
            clientApi = event.getVoicechat();
            Simpletts.LOGGER.info("Simple TTS connected to voice chat");
        } else {
            clientApi = null;
            stopLocalPlayback();
            audioQueue.clear();
            speaking = false;
        }
    }

    private void onMergeSound(MergeClientSoundEvent event) {
        short[] frame = audioQueue.poll();
        if (frame != null) {
            speaking = true;
            event.mergeAudio(frame);
        } else if (speaking) {
            speaking = false;
        }
    }

    public void speak(String text) {
        TTSEngine engine = TTSEngine.getInstance();
        if (!engine.isInitialized()) {
            engine.initialize(TTSConfig.getInstance().getSelectedVoice());
        }

        engine.synthesize(text).thenAccept(audio -> {
            if (audio.length == 0) {
                return;
            }

            float volumeMultiplier = TTSConfig.getInstance().volume / 100.0f;
            short[] adjusted = applyVolume(audio, volumeMultiplier);
            if (TTSConfig.getInstance().hearSelf) {
                playLocally(adjusted);
            }
            queueAudio(adjusted);
        });
    }

    public void stopSpeaking() {
        audioQueue.clear();
        stopLocalPlayback();
        speaking = false;
    }

    private void queueAudio(short[] audio) {
        int offset = 0;
        while (offset < audio.length) {
            short[] frame = new short[FRAME_SIZE];
            int remaining = Math.min(FRAME_SIZE, audio.length - offset);
            System.arraycopy(audio, offset, frame, 0, remaining);
            audioQueue.add(frame);
            offset += remaining;
        }
    }

    private short[] applyVolume(short[] audio, float volumeMultiplier) {
        if (volumeMultiplier == 1.0f) return audio;

        short[] result = new short[audio.length];
        for (int i = 0; i < audio.length; i++) {
            int sample = (int) (audio[i] * volumeMultiplier);
            result[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample));
        }
        return result;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public boolean isConnected() {
        return clientApi != null;
    }

    private void playLocally(short[] audio) {
        Thread thread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(48000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                synchronized (this) {
                    stopLocalPlayback();
                    localPlaybackLine = line;
                }
                line.start();

                byte[] bytes = new byte[audio.length * 2];
                for (int i = 0; i < audio.length; i++) {
                    bytes[i * 2] = (byte) (audio[i] & 0xFF);
                    bytes[i * 2 + 1] = (byte) ((audio[i] >> 8) & 0xFF);
                }

                line.write(bytes, 0, bytes.length);
                line.drain();
                synchronized (this) {
                    if (localPlaybackLine == line) {
                        localPlaybackLine = null;
                    }
                }
                line.close();
            } catch (Exception e) {
                Simpletts.LOGGER.error("Failed to play local TTS audio", e);
            }
        }, "SimpleTTS-LocalPlayback");
        thread.setDaemon(true);
        thread.start();
    }

    private synchronized void stopLocalPlayback() {
        if (localPlaybackLine == null) {
            return;
        }
        localPlaybackLine.stop();
        localPlaybackLine.flush();
        localPlaybackLine.close();
        localPlaybackLine = null;
    }
}
