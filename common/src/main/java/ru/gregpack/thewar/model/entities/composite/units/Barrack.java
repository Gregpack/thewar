package ru.gregpack.thewar.model.entities.composite.units;

import lombok.Getter;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.utils.PropertyUtil;


public class Barrack<T extends Unit> extends Entity {
    public static final int SIZE = 3;

    @Getter
    private final UnitType unitType;

    private final UnitProvider<T> unitGenerator;
    @Getter
    private final Coordinate defaultSpawnCoordinate;

    @Getter
    private final int buildRate = PropertyUtil.getIntProperty("logic.barrack.buildrate", 10);

    public Barrack(UnitProvider<T> unitGenerator, Coordinate position, Coordinate defaultSpawnCoordinate, UnitType unitType) {
        this.unitGenerator = unitGenerator;
        this.setPosition(position);
        this.defaultSpawnCoordinate = defaultSpawnCoordinate;
        this.unitType = unitType;
    }

    public T generateUnit(GameState gameState, int ticks) {
        if (ticks % buildRate != 0) {
            return null;
        }
        Coordinate spawnCoordinate = getSpawnCoordinate(gameState);
        if (spawnCoordinate == null) {
            return null;
        }
        T unit = unitGenerator.createUnit();
        unit.setPosition(spawnCoordinate);
        unit.setPlayerId(getPlayerId());
        return unit;
    }

    private Coordinate getSpawnCoordinate(GameState gameState) {
        if (!gameState.isCellOccupied(defaultSpawnCoordinate)) {
            return defaultSpawnCoordinate;
        }
        return gameState.getBarrackService().findFreeSpaceAroundBarrack(this.getPosition());
    }

}
