package com.leclowndu93150.simpletts;

import com.leclowndu93150.simpletts.client.SpeakScreen;
import com.leclowndu93150.simpletts.tts.SpeechHistory;
import com.leclowndu93150.simpletts.voicechat.TTSVoicechatPlugin;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SimplettsForgeClient {

    private static final KeyMapping OPEN_TTS_KEY = new KeyMapping(
            "key.simpletts.open",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_GRAVE,
            "key.categories.simpletts"
    );

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SimplettsForgeClient::onRegisterKeys);
        MinecraftForge.EVENT_BUS.addListener(SimplettsForgeClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(SimplettsForgeClient::onRegisterCommands);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TTS_KEY);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (OPEN_TTS_KEY.consumeClick() && mc.screen == null) {
            mc.setScreen(new SpeakScreen());
        }
    }

    private static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("tts")
                .then(Commands.literal("speak")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String text = StringArgumentType.getString(ctx, "text");
                                    TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
                                    if (plugin == null || !plugin.isConnected()) {
                                        ctx.getSource().sendFailure(Component.literal("Voice chat not connected"));
                                        return 0;
                                    }
                                    plugin.speak(text);
                                    SpeechHistory.add(text);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Speaking: " + text), false);
                                    return 1;
                                })))
                .then(Commands.literal("stop")
                        .executes(ctx -> {
                            TTSVoicechatPlugin plugin = TTSVoicechatPlugin.getInstance();
                            if (plugin != null) {
                                plugin.stopSpeaking();
                                ctx.getSource().sendSuccess(() -> Component.literal("Stopped speaking"), false);
                            }
                            return 1;
                        })));
    }
}
