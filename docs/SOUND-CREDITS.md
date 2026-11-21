# Timer chime

`timer_chime.ogg`: excerpt of **Windglockenspiel.Koshi**, recorded by **Membeth**.

- Original and license: https://commons.wikimedia.org/wiki/File:Windglockenspiel.Koshi.ogg
- License: **CC0 1.0 Universal**, https://creativecommons.org/publicdomain/zero/1.0/
- Downloaded excerpt: https://github.com/gretzke/open-alarm/blob/main/OpenAlarm/Resources/Ringtones/ringtone_classic_koshichime.caf
- Source ledger: https://github.com/gretzke/open-alarm/blob/main/OpenAlarm/Resources/Ringtones/SOURCES.md
- Converted from the OpenAlarm excerpt to mono 44.1 kHz, 16-bit PCM WAV with FFmpeg, normalized for audibility (-18 LUFS target, -1 dBTP). Removed the source CAF packet-table metadata for decoding; audio packets are unchanged before conversion. The OpenAlarm excerpt is trimmed and has a faded tail. No Apple recording is used.

For Android 1.1.1 the PCM WAV excerpt was transcoded to mono Ogg Vorbis (libvorbis quality 5, 44.1 kHz) using FFmpeg. The recording, duration and sample count are preserved; Vorbis is lossy compression. The asset shrank from 2,557,006 to 321,520 bytes. Decoding produced the same 1,278,464 samples; RMS changed by +0.0089 dB. This is a technical conversion check, not a subjective listening or on-device playback test.

The sound is embedded in the Android APK as an uncompressed raw resource and played offline through MediaPlayer, looping until the alert is acknowledged. Vorbis/Ogg is supported by the [Android media framework](https://developer.android.com/media/platform/supported-formats).
