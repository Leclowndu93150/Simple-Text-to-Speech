# Simple Text to Speech

Simple Text to Speech lets you type a message and play it through [Simple Voice Chat](https://modrepo.de/minecraft/voicechat) as spoken audio.

It is a client-side mod for Minecraft 1.20.1 with Fabric and Forge support. Speech is generated locally with the Voices library, and can be played into voice chat as if you were talking.

## Features

- Type text and send it through Simple Voice Chat as speech
- Works on Fabric and Forge for Minecraft 1.20.1
- Built in config screen with voice, volume, speed, and local playback options
- Quick input screen for sending speech without opening the config
- Speech history for replaying common messages
- Downloadable voices with multiple language options
- Local preview button for testing a voice before using it in game

## Screenshots

Add your screenshots here.

```md
![Config Screen](path/to/config-screen.png)
![Speak Screen](path/to/speak-screen.png)
```

## Requirements

- Minecraft 1.20.1
- [Simple Voice Chat](https://modrepo.de/minecraft/voicechat)
- Fabric or Forge, depending on the build you use

## How To Use

1. Install Simple Voice Chat and Simple Text to Speech.
2. Start the game and join a world or server where Simple Voice Chat is available.
3. Open the config screen and choose a language and voice.
4. If needed, use the preview button to download and test the selected voice.
5. Open the speak screen with the configured keybind.
6. Type a message and press `Speak`.

You can also use commands:

- `/tts speak <text>`
- `/tts stop`

## Config

The config screen lets you change:

- Language
- Voice
- Volume
- Speed
- Hear Self

`Hear Self` plays your generated speech locally in addition to sending it through voice chat.

## Notes

- Voice generation happens on your client.
- Some voices are downloaded on first use.
- Voice quality and startup time depend on the selected model.
