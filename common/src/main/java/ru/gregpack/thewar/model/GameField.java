package ru.gregpack.thewar.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.basic.FieldCell;
import ru.gregpack.thewar.model.entities.composite.units.Barrack;
import ru.gregpack.thewar.model.entities.composite.units.Entity;
import ru.gregpack.thewar.model.entities.composite.units.Unit;
import ru.gregpack.thewar.model.utils.Algorithms;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Singleton
public class GameField {
    private final Coordinate UPLEFT = new Coordinate(-1, 1);
    private final Coordinate UPRIGHT = new Coordinate(1, 1);
    private final Coordinate DOWNLEFT = new Coordinate(-1, -1);
    private final Coordinate DOWNRIGHT = new Coordinate(1, -1);

    /*
     *    /\   if you move through this list
     *   /  \  you basically go counterclockwise through rhombus
     *   \  /  starting at the right vertex
     *    \/
     */
    private final List<Coordinate> roundCoord = List.of(UPLEFT, DOWNLEFT, DOWNRIGHT, UPRIGHT);

    /*
     *   ****  if you move through this list
     *   *  *  you basically go counterclockwise through square
     *   *  *  starting at the left down vertex
     *   ****
     */
    private final List<Direction> squareCoord = List.of(Direction.RIGHT, Direction.UP, Direction.LEFT, Direction.DOWN);

    private final Map<Coordinate, Boolean> basesOccupation = new HashMap<>();

    @Getter
    private final FieldCell[][] gameField;
    @Getter
    private final int[][] baseCache;
    private final int length = PropertyUtil.getIntProperty("logic.field.length", 40);
    private final int height = PropertyUtil.getIntProperty("logic.field.height", 15);
    private final int baseLength = PropertyUtil.getIntProperty("logic.base.length", 10);
    private final int baseHeight = PropertyUtil.getIntProperty("logic.base.height", 15);

    @Inject
    public GameField() {
        List<Coordinate> possibleBases = PropertyUtil.getCoordinateList("logic.field.bases");
        for (Coordinate base : possibleBases) {
            basesOccupation.put(base, false);
        }
        this.gameField = new FieldCell[height][length];
        for (int i = 0; i < gameField.length; i++) {
            for (int j = 0; j < gameField[i].length; j++) {
                gameField[i][j] = new FieldCell();
            }
        }
        this.baseCache = new int[height][length];
        for (int[] ints : baseCache) {
            Arrays.fill(ints, -1);
        }
    }

    public void addBarrack(Barrack<?> barrack) {
        for (int yOffset = 0; yOffset < Barrack.SIZE; yOffset++) {
            for (int xOffset = 0; xOffset < Barrack.SIZE; xOffset++) {
                gameField[barrack.getPosition().getY() + yOffset][barrack.getPosition().getX() + xOffset].addOccupant(barrack);
            }
        }
    }

    public void addUnit(Unit unit) {
        gameField[unit.getPosition().getY()][unit.getPosition().getX()].addOccupant(unit);
    }

    public void addUnit(Coordinate position, Unit unit) {
        gameField[position.getY()][position.getX()].addOccupant(unit);
    }

    public void removeEntity(Entity entity) {
        gameField[entity.getPosition().getY()][entity.getPosition().getX()].removeOccupantById(entity.getId());
    }

    public void removeBarrack(Barrack<?> barrack) {
        Coordinate startCoord = barrack.getPosition();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                gameField[startCoord.getY() + y][startCoord.getX() + x].removeOccupantById(barrack.getId());
            }
        }
    }

    public void removeUnit(Coordinate position, int unitId) {
        gameField[position.getY()][position.getX()].removeOccupantById(unitId);
    }

    // loop through every 45 deg rotated square with center in %position% and diagonal = 2 * k where k = 1..radius
    public Unit findClosestUnitConditional(Coordinate position, int radius, Predicate<Unit> condition) {
        int positionY = position.getY();
        int positionX = position.getX();
        for (int i = 1; i <= radius; i++) {
            Coordinate startCoordinate = new Coordinate(positionX + i, positionY);
            for (Coordinate coordinate : roundCoord) {
                for (int j = 0; j < i; j++) {
                    Unit unit = checkUnitInCellConditional(startCoordinate, condition);
                    if (unit != null) {
                        return unit;
                    }
                    startCoordinate.addMutable(coordinate);
                }
            }
        }
        return null;
    }

    public Unit checkUnitInCellConditional(Coordinate position, Predicate<Unit> condition) {
        if (isCellOutsideBounds(position)) {
            return null;
        }
        FieldCell cell = gameField[position.getY()][position.getX()];
        if (!cell.isOccupied()) {
            return null;
        }
        Entity entity = cell.getCellOccupant();
        if (entity instanceof Unit && condition.test((Unit) entity)) {
            return (Unit) entity;
        }
        return null;
    }

    public List<Direction> nextMoveToTarget(Unit unit, Coordinate target) {
        return aStarAlgorithm(unit.getPosition(), target);
    }

    public boolean isCellOutsideBounds(Coordinate cell) {
        return cell.getX() < 0 || cell.getY() < 0 || cell.getX() >= length || cell.getY() >= height;
    }

    public boolean isCellOccupied(Coordinate cell) {
        return isCellOutsideBounds(cell) || gameField[cell.getY()][cell.getX()].isOccupied();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (FieldCell[] fieldCells : gameField) {
            for (FieldCell cell : fieldCells) {
                sb.append(cell.toString());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public List<Direction> aStarAlgorithm(Coordinate from, Coordinate to) {
        return Algorithms.aStarAlgorithm(gameField, from, to);
    }

    public Coordinate findFreeSpaceAroundBarrack(Coordinate center) {
        Coordinate startCoordinate = center
                .nCellsToTheDirection(Direction.DOWN, 1)
                .nCellsToTheDirection(Direction.LEFT, 1);
        int barrackLength = Barrack.SIZE + 1;
        for (Direction direction : squareCoord) {
            for (int j = 0; j < barrackLength; j++) {
                if (!isCellOutsideBounds(startCoordinate) && !isCellOccupied(startCoordinate)) {
                    return startCoordinate;
                }
                startCoordinate.addMutable(direction);
            }
        }
        return null;
    }

    public Coordinate getFreeBase() {
        return basesOccupation.entrySet().stream().filter(base -> {
            boolean isBaseOccupued = base.getValue();
            return !isBaseOccupued;
        }).findFirst().orElseThrow(() -> new IllegalArgumentException("No place for a new player!")).getKey();
    }

    public void occupyFreeBase(Coordinate freeBase, Integer playerId) {
        if (!basesOccupation.containsKey(freeBase)) {
            throw new IllegalArgumentException("Base " + freeBase + " is not a free base!");
        }
        basesOccupation.put(freeBase, true);
        int leftBaseX = freeBase.getX();
        int bottomBaseY = freeBase.getY();
        for (int y = bottomBaseY; y < bottomBaseY + baseHeight; y++) {
            for (int x = leftBaseX; x < leftBaseX + baseLength; x++) {
                baseCache[y][x] = playerId;
            }
        }
    }

    public int getCellOwner(Coordinate coordinate) {
        if (coordinate.getY() < 0 || coordinate.getX() < 0 || coordinate.getX() > length || coordinate.getY() > height) {
            return -1;
        }
        return baseCache[coordinate.getY()][coordinate.getX()];
    }


    public boolean isSpaceOccupied(Coordinate cell, int size) {
        int startX = cell.getX();
        int startY = cell.getY();
        for (int y = startY; y < startY + size; y++) {
            for (int x = startX; x < startX + size; x++) {
                if (isCellOccupied(cell)) {
                    return true;
                }
            }
        }
        return false;
    }

}
