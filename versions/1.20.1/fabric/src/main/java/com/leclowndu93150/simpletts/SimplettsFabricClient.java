package com.leclowndu93150.simpletts;

import com.leclowndu93150.simpletts.client.SpeakScreen;
import com.leclowndu93150.simpletts.tts.SpeechHistory;
import com.leclowndu93150.simpletts.voicechat.TTSVoicechatPlugin;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class SimplettsFabricClient implements ClientModInitializer {

    private static final KeyMapping OPEN_TTS_KEY = new KeyMapping(
            "key.simpletts.open",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_GRAVE,
            "key.categories.simpletts"
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_TTS_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_TTS_KEY.consumeClick() && client.screen == null) {
                client.setScreen(new SpeakScreen());
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(ClientCommandManager.literal("tts")
                    .then(ClientCommandManager.literal("speak")
                            .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        String text = StringArgumentType.getString(ctx, "text");
                                        TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
                                        if (plugin == null || !plugin.isConnected()) {
                                            ctx.getSource().sendError(Component.literal("Voice chat not connected"));
                                            return 0;
                                        }
                                        plugin.speak(text);
                                        SpeechHistory.add(text);
                                        ctx.getSource().sendFeedback(Component.literal("Speaking: " + text));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("stop")
                            .executes(ctx -> {
                                TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
                                if (plugin != null) {
                                    plugin.stopSpeaking();
                                    ctx.getSource().sendFeedback(Component.literal("Stopped speaking"));
                                }
                                return 1;
                            })));
        });
    }
}
