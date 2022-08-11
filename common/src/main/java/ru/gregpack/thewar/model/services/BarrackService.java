package ru.gregpack.thewar.model.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.gregpack.thewar.model.GameField;
import ru.gregpack.thewar.model.entities.BarrackInfo;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.composite.units.*;
import ru.gregpack.thewar.model.repositories.BarrackInMemoryRepository;
import ru.gregpack.thewar.model.repositories.PlayerInMemoryRepository;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.util.*;

@Singleton
public class BarrackService {

    private final GameField gameField;
    private final BarrackInMemoryRepository barrackRepository;
    private final PlayerInMemoryRepository playerRepository;

    private final Map<String, Integer> barrackCost;

    @Inject
    public BarrackService(BarrackInMemoryRepository barrackRepository,
                          PlayerInMemoryRepository playerRepository,
                          GameField gameField) {
        this.barrackRepository = barrackRepository;
        this.playerRepository = playerRepository;
        this.gameField = gameField;
        Map<String, String> barrackCostString = PropertyUtil.getPropertyMap("logic.barrack.cost");
        this.barrackCost = new HashMap<>();
        barrackCostString.forEach((key, value) -> barrackCost.put(key, Integer.parseInt(value)));
    }

    public void createBarrack(Coordinate position, UnitType unitType, int ownerId) {
        if (gameField.isSpaceOccupied(position, Barrack.SIZE)) {
            throw new IllegalArgumentException("Can't place barrack at " + position + ": space is occupied.");
        }
        int baseOwnerId = gameField.getCellOwner(position);
        // wtf cant compare int to null
        if (baseOwnerId != ownerId) {
            throw new IllegalArgumentException("Can't place barrack at " + position + ": it is outside your base.");
        }
        Integer cost = getBarrackCost(unitType);
        GamePlayer owner = playerRepository.getPlayerById(ownerId);
        if (owner.getMoney() < cost) {
            throw new IllegalArgumentException("Can't buy barrack - insufficient funds!");
        }

        owner.removeMoney(cost);
        Coordinate defaultSpawnPoint = position.nCellsToTheDirection(Direction.UP, 3).nCellsToTheDirection(Direction.RIGHT, 1);
        Barrack<?> barrack;
        UnitProvider<?> unitProvider = null;
        switch (unitType) {
            case FOOTMAN:
                unitProvider = Footman::new;
                break;
            case ARCHER:
                unitProvider = Archer::new;
                break;
            case ALCHEMIST:
                unitProvider = Alchemist::new;
                break;
            case CAVALRY:
                unitProvider = Cavalry::new;
                break;
            case ASSASSIN:
                unitProvider = Assassin::new;
                break;
        }
        barrack = new Barrack<>(unitProvider, position, defaultSpawnPoint, unitType);
        try {
            barrack.setPlayerId(ownerId);
            gameField.addBarrack(barrack);
            barrackRepository.addBarrack(barrack, ownerId);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Can't place barrack in cell " + position + " - barrack will be out of bounds!");
        }
    }

    public Integer getBarrackCost(UnitType unitType) {
        return barrackCost.get(unitType.toString().toLowerCase(Locale.ROOT));
    }

    public Collection<Barrack<?>> getBarracks() {
        return barrackRepository.getBarracks();
    }

    public int getBarrackOwnerId(int barrackId) {
        return barrackRepository.getBarrackInfoById(barrackId).getOwnerId();
    }

    public Coordinate findFreeSpaceAroundBarrack(Coordinate center) {
        return gameField.findFreeSpaceAroundBarrack(center);
    }

    public int removeBarrack(int barrackId, int senderId) {
        int ownerId = barrackRepository.getBarrackInfoById(barrackId).getOwner().getPlayerId();
        if (ownerId != senderId) {
            throw new IllegalArgumentException("You can't sell your barrack!");
        }
        Barrack<?> barrack = barrackRepository.getBarrackById(barrackId);
        barrackRepository.removeBarrack(barrackId);
        gameField.removeBarrack(barrack);
        return getBarrackCost(barrack.getUnitType());
    }
}
