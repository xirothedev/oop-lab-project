package com.ooplab.candycrush.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicManager {

    private static MediaPlayer player;

    public static void playBackgroundMusic() {
        try {
            var url = MusicManager.class.getResource("/music/bgm.mp3");

            if (url == null) {
                System.out.println("BGM not found!");
                return;
            }

            Media media = new Media(url.toExternalForm());
            player = new MediaPlayer(media);

            player.setCycleCount(MediaPlayer.INDEFINITE); // loop forever
            player.setVolume(0.2); // adjust volume
            player.play();

        } catch (Exception e) {
            System.out.println("BGM error: " + e.getMessage());
        }
    }

    public static void stop() {
        if (player != null) {
            player.stop();
        }
    }
}
