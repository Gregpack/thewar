package ru.gregpack.thewar.view.canvas;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

public class PannableCanvas extends Pane {

    private final DoubleProperty myScale = new SimpleDoubleProperty(1.0);
    private final Canvas canvas;

    public PannableCanvas(int width, int height) {
        int screenWidth = (int) Screen.getPrimary().getBounds().getWidth();
        setPrefSize(Math.min(width, screenWidth), height);
        scaleXProperty().bind(myScale);
        scaleYProperty().bind(myScale);
        canvas = new Canvas(width, height);
        getChildren().add(canvas);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public double getScale() {
        return myScale.get();
    }

    public void setScale(double scale) {
        myScale.set(scale);
    }

    public void setPivot(double x, double y) {
        setTranslateX(getTranslateX() - x);
        setTranslateY(getTranslateY() - y);
    }
}