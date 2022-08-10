package ru.gregpack.thewar.view.animations;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;
import ru.gregpack.thewar.model.actions.MoveAction;

import java.util.List;

public class AnimatedUnitProp extends AnimatedProp {
    private final List<MoveAction> moveActions;
    private final int millis;
    private final double hpPercent;
    private final int unitId;

    public AnimatedUnitProp(Image image,
                            List<MoveAction> moveAction,
                            int millis, double hpPercent,
                            int unitId) {
        super(image, moveAction.get(0).getStartX(), moveAction.get(0).getStartY(),
                moveAction.get(0).getToX(), moveAction.get(0).getToY());
        this.hpPercent = hpPercent;
        this.unitId = unitId;
        this.moveActions = moveAction;
        this.millis = millis;
        initTimeline();
    }

    @Override
    protected void initTimeline() {
        int frames = moveActions.size();
        int oneFrameLength = millis / frames;
        int totalLength = oneFrameLength;
        this.timeline = new Timeline();
        for (MoveAction moveAction : moveActions) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(totalLength),
                    new KeyValue(x, moveAction.getToX()),
                    new KeyValue(y, moveAction.getToY())));
            totalLength += oneFrameLength;
        }
        this.timeline.setOnFinished((e) -> isFinished = true);
    }

    public double getHpPercent() {
        return hpPercent;
    }

    public int getUnitId() {
        return unitId;
    }
}
