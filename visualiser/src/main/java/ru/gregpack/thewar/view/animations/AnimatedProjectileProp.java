package ru.gregpack.thewar.view.animations;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class AnimatedProjectileProp extends AnimatedProp {
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;
    private final int millis;

    public AnimatedProjectileProp(Image image, double startX, double startY, double endX, double endY, int millis) {
        super(image, startX, startY, endX, endY);
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.millis = millis;
        initTimeline();
    }

    @Override
    protected void initTimeline() {
        double xProjection = 0;
        double yProjection = 0;
        double propLength = getImage().getWidth();
        double radRotationAngle = Math.toRadians(getRotationAngle());
        if (endX - startX != 0) {
            xProjection = propLength * Math.cos(radRotationAngle);
        }
        if (endY - startY != 0) {
            yProjection = propLength * Math.sin(radRotationAngle);
        }
        this.timeline = new Timeline(new KeyFrame(Duration.millis(millis * 0.8),
                new KeyValue(x, endX - xProjection),
                new KeyValue(y, endY - yProjection)
        ));
        this.timeline.setDelay(Duration.millis(millis * 0.2));
        this.timeline.setOnFinished((e) -> isFinished = true);
    }
}
