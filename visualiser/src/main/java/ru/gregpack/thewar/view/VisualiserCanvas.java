package ru.gregpack.thewar.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import ru.gregpack.thewar.model.actions.Action;
import ru.gregpack.thewar.model.actions.AttackAction;
import ru.gregpack.thewar.model.actions.MoveAction;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;
import ru.gregpack.thewar.network.messages.dto.UnitDto;
import ru.gregpack.thewar.view.animations.AnimatedProjectileProp;
import ru.gregpack.thewar.view.animations.AnimatedProp;
import ru.gregpack.thewar.view.animations.AnimatedUnitProp;
import ru.gregpack.thewar.view.canvas.PannableCanvas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class VisualiserCanvas {

    private final static int CELL_SIDE_LENGTH = 20;
    private final static int HALF_CELL_SIDE_LENGTH = CELL_SIDE_LENGTH / 2;
    private final static int HP_BAR_LENGTH = 16;
    private final static int HP_BAR_HEIGHT = 4;
    private final static int HP_BAR_OFFSET = 2;

    private final int baseLength;
    private final int baseHeight;
    private final int tickrate;
    private final int attackAnimationLength;
    private final int moveAnimationLength;
    private final List<Coordinate> bases;
    private PannableCanvas canvas;
    private final int width;
    private final int height;
    private final Color baseColor = new Color(0.729, 0.847, 0.698, 1);
    private final List<AnimatedProp> currentlyAliveProps = new ArrayList<>();
    private GameStateDto currentGameStateDto = null;
    private GameStateDto previousGameStateDto = null;
    private final HashSet<Integer> stationaryUnitIds = new HashSet<>();

    public VisualiserCanvas(int width, int height, int baseLength, int baseHeight, List<Coordinate> bases, int tickrate) {
        this.width = width;
        this.height = height;
        this.baseLength = baseLength;
        this.baseHeight = baseHeight;
        this.bases = bases;
        this.tickrate = tickrate;
        attackAnimationLength = (int) (tickrate * 0.9);
        moveAnimationLength = (int) (tickrate * 0.9);
    }

    public void initCanvas() {
        canvas = new PannableCanvas(CELL_SIDE_LENGTH * width, CELL_SIDE_LENGTH * height);
        drawTiles();
        createProjectileAnimation(GraphicsLibrary.getUnitWeapon(UnitType.ARCHER), 10, 0, 0, 1, 3000);
        //currentlyAliveProps.get(0).getTimeline().setCycleCount(Animation.INDEFINITE);
    }

    public void drawGameFrame() {
        GraphicsContext gc = canvas.getCanvas().getGraphicsContext2D();
        drawTiles();
        drawGameState(currentGameStateDto);
        gc.save();
        for (AnimatedProp prop : currentlyAliveProps) {
            if (prop instanceof AnimatedUnitProp) {
                AnimatedUnitProp unit = (AnimatedUnitProp) prop;
                drawImageUnscaled(gc, unit.getImage(), unit.getX(), unit.getY());
                drawUnitHpBarUnscaled(gc, unit.getX(), unit.getY(), unit.getHpPercent());
                if (unit.isFinished()) {
                    stationaryUnitIds.add(unit.getUnitId());
                }
                continue;
            }
            drawRotatedImage(gc, prop.getImage(), prop.getX(), prop.getY(), prop.getRotationAngle());
        }
        gc.restore();
        currentlyAliveProps.removeIf(AnimatedProp::isFinished);
    }

    public void updateGameState(GameStateDto gameStateDto) {
        previousGameStateDto = currentGameStateDto;
        currentGameStateDto = gameStateDto;
        stationaryUnitIds.clear();
        stationaryUnitIds.addAll(currentGameStateDto.getUnits().keySet());
        currentlyAliveProps.clear();
    }

    private void drawGameState(GameStateDto gameStateDto) {
        if (gameStateDto == null) {
            return;
        }
        GraphicsContext gc = canvas.getCanvas().getGraphicsContext2D();
        gameStateDto.getBarracks().forEach((id, barrackDto) -> {
            drawImage(gc, GraphicsLibrary.getBarrackImage(), barrackDto.getX(), barrackDto.getY());
            drawImage(gc, GraphicsLibrary.getUnitImage(barrackDto.getUnitType(), barrackDto.getPlayerId()), barrackDto.getX() + 1, barrackDto.getY() + 1);
        });
        gameStateDto.getMoveActions().stream().peek(a -> {
            a.setStartX(a.getStartX() * CELL_SIDE_LENGTH);
            a.setStartY(a.getStartY() * CELL_SIDE_LENGTH);
            a.setToX(a.getToX() * CELL_SIDE_LENGTH);
            a.setToY(a.getToY() * CELL_SIDE_LENGTH);
        }).collect(Collectors.groupingBy(Action::getPerformerId)).forEach((id, moveAction) -> {
            if (moveAction != null && moveAction.isEmpty()) {
                return;
            }
            stationaryUnitIds.remove(id);
            this.createMoveAnimation(id, moveAction);
        });
        gameStateDto.getAttackActions().forEach(this::createAttackAnimation);
        gameStateDto.getAttackActions().clear();
        gameStateDto.getMoveActions().clear();
        gameStateDto.getUnits().forEach((id, unitDto) -> {
            if (!stationaryUnitIds.contains(id)) {
                return;
            }
            drawImage(gc, GraphicsLibrary.getUnitImage(unitDto.getUnitType(), unitDto.getPlayerId()), unitDto.getX(), unitDto.getY());
            //gc.fillText(String.valueOf(unitDto.getId()), unitDto.getX() * CELL_SIDE_LENGTH, unitDto.getY() * CELL_SIDE_LENGTH);
            double hpLeft = unitDto.getHealthPoints() / unitDto.getTotalHealthPoints();
            drawUnitHpBar(gc, unitDto.getX(), unitDto.getY(), hpLeft);
        });
    }

    private void drawTiles() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                drawTile(x, y);
            }
        }
        for (Coordinate basis : bases) {
            for (int yOffset = 0; yOffset < baseHeight; yOffset++) {
                for (int xOffset = 0; xOffset < baseLength; xOffset++) {
                    drawTile(basis.getX() + xOffset, basis.getY() + yOffset, baseColor);
                }
            }
        }
    }

    private void drawTile(int x, int y, Color color) {
        GraphicsContext gc = canvas.getCanvas().getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(x * CELL_SIDE_LENGTH, y * CELL_SIDE_LENGTH, CELL_SIDE_LENGTH, CELL_SIDE_LENGTH);
        gc.setFill(color);
        gc.fillRect(x * CELL_SIDE_LENGTH + 1, y * CELL_SIDE_LENGTH + 1, CELL_SIDE_LENGTH - 1, CELL_SIDE_LENGTH - 1);
    }

    private void drawTile(int x, int y) {
        GraphicsContext gc = canvas.getCanvas().getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(x * CELL_SIDE_LENGTH, y * CELL_SIDE_LENGTH, CELL_SIDE_LENGTH, CELL_SIDE_LENGTH);
        gc.setFill(Color.WHITE);
        gc.fillRect(x * CELL_SIDE_LENGTH + 1, y * CELL_SIDE_LENGTH + 1, CELL_SIDE_LENGTH - 1, CELL_SIDE_LENGTH - 1);
    }

    private void drawImage(GraphicsContext gc, Image image, double x, double y) {
        gc.drawImage(image, x * CELL_SIDE_LENGTH, y * CELL_SIDE_LENGTH);
    }

    private void drawImageUnscaled(GraphicsContext gc, Image image, double x, double y) {
        gc.drawImage(image, x, y);
    }

    private void drawUnitHpBar(GraphicsContext gc, double unitX, double unitY, double hpLeft) {
        gc.setFill(Color.RED);
        drawRectangle(gc, unitX, unitY, HP_BAR_OFFSET, -HP_BAR_OFFSET, HP_BAR_LENGTH, HP_BAR_HEIGHT);
        gc.setFill(Color.GREEN);
        drawRectangle(gc, unitX, unitY, HP_BAR_OFFSET, -HP_BAR_OFFSET, HP_BAR_LENGTH * hpLeft, HP_BAR_HEIGHT);
    }

    private void drawUnitHpBarUnscaled(GraphicsContext gc, double unitX, double unitY, double hpLeft) {
        gc.setFill(Color.RED);
        drawRectangleUnscaled(gc, unitX, unitY, HP_BAR_OFFSET, -HP_BAR_OFFSET, HP_BAR_LENGTH, HP_BAR_HEIGHT);
        gc.setFill(Color.GREEN);
        drawRectangleUnscaled(gc, unitX, unitY, HP_BAR_OFFSET, -HP_BAR_OFFSET, HP_BAR_LENGTH * hpLeft, HP_BAR_HEIGHT);
    }

    private void drawRotatedImage(GraphicsContext gc, Image image, double x, double y, double angle) {
        Rotate r = new Rotate(angle, x, y);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        gc.drawImage(image, x, y);
    }

    private void drawRectangle(GraphicsContext gc, double x, double y, double xOff, double yOff, double length, double height) {
        gc.fillRect(x * CELL_SIDE_LENGTH + xOff, y * CELL_SIDE_LENGTH + yOff, length, height);
    }

    private void drawRectangleUnscaled(GraphicsContext gc, double x, double y, double xOff, double yOff, double length, double height) {
        gc.fillRect(x + xOff, y + yOff, length, height);
    }

    private void createMoveAnimation(Integer performerId, List<MoveAction> moveAction) {
        UnitDto unitDto = currentGameStateDto.getUnits().containsKey(performerId)
                ? currentGameStateDto.getUnits().get(performerId)
                : previousGameStateDto.getUnits().get(performerId);
        if (unitDto == null) {
            return;
        }
        Image image = GraphicsLibrary.getUnitImage(unitDto.getUnitType(), unitDto.getPlayerId());
        double hpLeft = unitDto.getHealthPoints() / unitDto.getTotalHealthPoints();
        AnimatedUnitProp animatedUnitProp = new AnimatedUnitProp(image, moveAction, moveAnimationLength, hpLeft, performerId);
        currentlyAliveProps.add(animatedUnitProp);
        animatedUnitProp.getTimeline().setCycleCount(1);
        animatedUnitProp.getTimeline().play();
    }

    private void createAttackAnimation(AttackAction action) {
        UnitDto attacker = currentGameStateDto.getUnits().get(action.getPerformerId());
        UnitDto attacked = currentGameStateDto.getUnits().get(action.getAttackedId());
        if (attacker == null) {
            attacker = previousGameStateDto != null ? previousGameStateDto.getUnits().get(action.getPerformerId()) : null;
        }
        if (attacked == null) {
            attacked = previousGameStateDto != null ? previousGameStateDto.getUnits().get(action.getAttackedId()) : null;
        }
        if (attacker != null && attacked != null) {
            createProjectileAnimation(GraphicsLibrary.getUnitWeapon(attacker.getUnitType()),
                    attacker.getX(), attacker.getY(),
                    attacked.getX(), attacked.getY(),
                    attackAnimationLength);
        }
    }

    private void createProjectileAnimation(Image image,
                                           double startX, double startY,
                                           double endX, double endY,
                                           int millis) {
        AnimatedProp prop = new AnimatedProjectileProp(image,
                startX * CELL_SIDE_LENGTH + HALF_CELL_SIDE_LENGTH,
                startY * CELL_SIDE_LENGTH + HALF_CELL_SIDE_LENGTH,
                endX * CELL_SIDE_LENGTH + HALF_CELL_SIDE_LENGTH,
                endY * CELL_SIDE_LENGTH + HALF_CELL_SIDE_LENGTH, millis);
        currentlyAliveProps.add(prop);
        prop.getTimeline().setCycleCount(1);
        prop.getTimeline().play();
    }

    public PannableCanvas getPannableCanvas() {
        return canvas;
    }

    public int getWidth() {
        return Math.min(width * CELL_SIDE_LENGTH, (int)Screen.getPrimary().getBounds().getWidth());
    }

    public int getHeight() {
        return height * CELL_SIDE_LENGTH;
    }
}
