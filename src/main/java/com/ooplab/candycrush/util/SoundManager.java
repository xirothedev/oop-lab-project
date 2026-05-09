package com.ooplab.candycrush.util;

import javafx.scene.media.AudioClip;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoundManager {

    private static final Logger LOGGER = Logger.getLogger(SoundManager.class.getName());

    private static double volume = 0.3;
    private static boolean muted = false;

    private static final Map<String, AudioClip> cache = new HashMap<>();

    public static void playSwap() {
        play("/sfx/swap.wav");
    }
    public static void playMatch() {
        play("/sfx/match.wav");
    }
    public static void playClick() {
        play("/sfx/click.wav");
    }
    private static void play(String path) {
        if (muted) return;

        try {
            AudioClip clip = cache.computeIfAbsent(path, p -> {
                var url = SoundManager.class.getResource(p);
                if (url == null) {
                    LOGGER.warning("Sound resource missing: " + p);
                    return null;
                }
                return new AudioClip(url.toExternalForm());
            });

            if (clip != null) {
                clip.setVolume(volume);
                clip.play();
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Sound playback error", e);
        }
    }


    public static void setVolume(double v) {
        volume = Math.max(0, Math.min(1, v));
    }

    public static void setMuted(boolean m) {
        muted = m;
    }
}