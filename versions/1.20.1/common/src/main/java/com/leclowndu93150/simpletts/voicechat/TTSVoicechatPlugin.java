package com.leclowndu93150.simpletts.voicechat;

import com.leclowndu93150.simpletts.Simpletts;
import com.leclowndu93150.simpletts.config.TTSConfig;
import com.leclowndu93150.simpletts.tts.TTSEngine;
import com.leclowndu93150.simpletts.tts.TTSVoice;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MergeClientSoundEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TTSVoicechatPlugin implements VoicechatPlugin {

    public static final String PLUGIN_ID = "simpletts";
    private static final int FRAME_SIZE = 960;

    private static TTSVoicechatPlugin instance;
    private VoicechatClientApi clientApi;
    private final ConcurrentLinkedQueue<short[]> audioQueue = new ConcurrentLinkedQueue<>();
    private boolean speaking;

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
            if (audio.length == 0) return;

            float volumeMultiplier = TTSConfig.getInstance().volume / 100.0f;
            short[] adjusted = applyVolume(audio, volumeMultiplier);
            queueAudio(adjusted);
        });
    }

    public void stopSpeaking() {
        audioQueue.clear();
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
}
