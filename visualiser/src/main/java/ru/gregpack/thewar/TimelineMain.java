package ru.gregpack.thewar;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;
import ru.gregpack.thewar.view.GraphicsLibrary;
import ru.gregpack.thewar.view.animations.AnimatedProjectileProp;
import ru.gregpack.thewar.view.animations.AnimatedProp;

import java.util.ArrayList;
import java.util.List;

public class TimelineMain extends Application {
    public static final double W = 200; // canvas dimensions.
    public static final double H = 200;

    public static final double D = 20;  // diameter.
    private final List<AnimatedProp> currentlyAliveProps = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        final Canvas canvas = new Canvas(W, H);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, W, H);
                gc.save();
                for (AnimatedProp prop : currentlyAliveProps) {
                    drawRotatedImage(gc, prop.getImage(), prop.getX(), prop.getY(), prop.getRotationAngle());
                }
                gc.restore();
                currentlyAliveProps.removeIf(AnimatedProp::isFinished);
            }
        };
        timer.start();

        stage.setScene(new Scene(new Group(canvas)));
        stage.show();
        timer.start();
        createAnimation(GraphicsLibrary.getUnitWeapon(UnitType.FOOTMAN), 0, 0, 150, 150, 2000);
        createAnimation(GraphicsLibrary.getUnitWeapon(UnitType.FOOTMAN), 50, 0, 150, 150, 2000);
        createAnimation(GraphicsLibrary.getUnitWeapon(UnitType.ARCHER), 100, 0, 150, 150, 2000);
        createAnimation(GraphicsLibrary.getUnitWeapon(UnitType.ARCHER), 150, 0, 150, 150, 2000);
    }

    private void createAnimation(Image image,
                                 double startX, double startY,
                                 double endX, double endY,
                                 int millis) {
        AnimatedProp prop = new AnimatedProjectileProp(image, startX, startY, endX, endY, millis);
        currentlyAliveProps.add(prop);
        prop.getTimeline().setCycleCount(Animation.INDEFINITE);
        prop.getTimeline().play();
    }

    private void drawRotatedImage(GraphicsContext gc, Image image, double x, double y, double angle) {
        Rotate r = new Rotate(angle, x, y);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        gc.drawImage(image, x, y);
    }

    public static void main(String[] args) {
        launch(args);
    }
}