package ru.gregpack.thewar.view.animations;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import javafx.util.Duration;

public abstract class AnimatedProp {
    private final Image image;
    protected final DoubleProperty x = new SimpleDoubleProperty();
    protected final DoubleProperty y = new SimpleDoubleProperty();
    protected Timeline timeline;
    private final double rotationAngle;
    protected boolean isFinished = false;

    public AnimatedProp(Image image, double startX, double startY, double endX, double endY) {
        this.image = image;
        x.set(startX);
        y.set(startY);
        rotationAngle = Math.toDegrees(Math.atan2(endY - startY, endX - startX));
    }

    protected abstract void initTimeline();

    public Image getImage() {
        return image;
    }

    public double getX() {
        return x.get();
    }

    public double getY() {
        return y.get();
    }

    public Animation getTimeline() {
        return timeline;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public boolean isFinished() {
        return isFinished;
    }
}
