package com.ooplab.candycrush.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicManager {

    private static final Logger LOGGER = Logger.getLogger(MusicManager.class.getName());

    private static MediaPlayer player;

    public static void playBackgroundMusic() {
        try {
            var url = MusicManager.class.getResource("/music/bgm.mp3");

            if (url == null) {
                LOGGER.warning("Background music resource missing: /music/bgm.mp3");
                return;
            }

            Media media = new Media(url.toExternalForm());
            player = new MediaPlayer(media);

            player.setCycleCount(MediaPlayer.INDEFINITE); // loop forever
            player.setVolume(0.2); // adjust volume
            player.play();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Background music error", e);
        }
    }

    public static void stop() {
        if (player != null) {
            player.stop();
        }
    }
}
