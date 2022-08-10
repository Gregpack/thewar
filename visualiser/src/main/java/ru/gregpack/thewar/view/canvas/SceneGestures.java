package ru.gregpack.thewar.view.canvas;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class SceneGestures {

    private static final double MAX_SCALE = 10.0d;
    private static final double MIN_SCALE = .9d;

    private final DragContext sceneDragContext = new DragContext();

    private final PannableCanvas canvas;

    public SceneGestures(PannableCanvas canvas) {
        this.canvas = canvas;
    }

    public void onMousePressedEventHandler(MouseEvent event) {
        // right mouse button => panning
        //if (!event.isSecondaryButtonDown())
        //    return;

        sceneDragContext.mouseAnchorX = event.getSceneX();
        sceneDragContext.mouseAnchorY = event.getSceneY();

        sceneDragContext.translateAnchorX = canvas.getTranslateX();
        sceneDragContext.translateAnchorY = canvas.getTranslateY();
    }

    public void onMouseDraggedEventHandler(MouseEvent event) {
        // right mouse button => panning
        //if (!event.isSecondaryButtonDown())
        //    return;

        canvas.setTranslateX(sceneDragContext.translateAnchorX + event.getSceneX() - sceneDragContext.mouseAnchorX);
        canvas.setTranslateY(sceneDragContext.translateAnchorY + event.getSceneY() - sceneDragContext.mouseAnchorY);

        event.consume();
    }
    public void onScrollEventHandle(ScrollEvent event) {
        double delta = 1.2;

        double scale = canvas.getScale(); // currently we only use Y, same value is used for X
        double oldScale = scale;

        if (event.getDeltaY() < 0)
            scale /= delta;
        else
            scale *= delta;

        scale = clamp(scale, MIN_SCALE, MAX_SCALE);

        double f = (scale / oldScale) - 1;

        double dx = (event.getSceneX() - (canvas.getBoundsInParent().getWidth() / 2 + canvas.getBoundsInParent().getMinX()));
        double dy = (event.getSceneY() - (canvas.getBoundsInParent().getHeight() / 2 + canvas.getBoundsInParent().getMinY()));

        canvas.setScale(scale);

        // note: pivot value must be untransformed, i. e. without scaling
        canvas.setPivot(f * dx, f * dy);

        event.consume();
    }

    private double clamp(double value, double min, double max) {

        if (Double.compare(value, min) < 0)
            return min;

        if (Double.compare(value, max) > 0)
            return max;

        return value;
    }
}
