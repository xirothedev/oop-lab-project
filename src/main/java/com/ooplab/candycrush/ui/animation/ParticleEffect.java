package com.ooplab.candycrush.ui.animation;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ParticleEffect {

    private static final int MIN_PARTICLES = 8;
    private static final int MAX_PARTICLES = 12;
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 5.0;
    private static final double TRAVEL_DISTANCE = 40.0;

    public static void createExplosion(Pane overlay, Color color, double centerX, double centerY) {
        List<Node> particles = new ArrayList<>();
        int count = ThreadLocalRandom.current().nextInt(MIN_PARTICLES, MAX_PARTICLES + 1);
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count + ThreadLocalRandom.current().nextDouble(0.3);
            double radius = ThreadLocalRandom.current().nextDouble(MIN_RADIUS, MAX_RADIUS);
            Circle particle = new Circle(radius, color);
            particle.setTranslateX(centerX);
            particle.setTranslateY(centerY);
            particle.setOpacity(1.0);
            particles.add(particle);

            double endX = Math.cos(angle) * TRAVEL_DISTANCE;
            double endY = Math.sin(angle) * TRAVEL_DISTANCE;

            TranslateTransition translate = new TranslateTransition(
                    Duration.millis(AnimationConfig.PARTICLE_DURATION_MS), particle);
            translate.setToX(centerX + endX);
            translate.setToY(centerY + endY);
            translate.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(
                    Duration.millis(AnimationConfig.PARTICLE_DURATION_MS), particle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(translate, fade);
            pt.setOnFinished(e -> overlay.getChildren().remove(particle));
            pt.play();
        }
    }
}
