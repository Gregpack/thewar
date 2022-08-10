package ru.gregpack.thewar.engine.bots;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;
import ru.gregpack.thewar.network.AI;
import ru.gregpack.thewar.network.messages.OrderMessage;
import ru.gregpack.thewar.network.messages.orders.BuildOrder;
import ru.gregpack.thewar.network.messages.orders.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SmartAI implements AI {
    private static final Logger logger = LogManager.getLogger(SmartAI.class.getName());

    private GamePlayer gamePlayer;
    private boolean isNoSpace = false;
    private final Random random = new Random();
    private UnitType[] frontLineUnits = new UnitType[] {UnitType.FOOTMAN, UnitType.CAVALRY};
    private UnitType[] damageUnits = new UnitType[] {UnitType.ALCHEMIST, UnitType.ARCHER, UnitType.ASSASSIN};
    private UnitType nextUnit = frontLineUnits[random.nextInt(frontLineUnits.length)];

    private boolean isFirstRowBuilt = false;
    private Direction baseCursorMovement;
    private Coordinate baseCursor;
    private Coordinate lastBaseBuilt;
    @Override
    public List<Order> nextOrders(GameState gameState) {
        List<Order> orderList = new ArrayList<>();
        Integer cost = gameState.getBarrackService().getBarrackCost(nextUnit);
        int playerMoney = gameState.getPlayerService().getPlayerById(gamePlayer.getPlayerId()).getMoney();
        if (!isNoSpace && playerMoney >= cost) {
            Coordinate basePlacement = moveCursorToNextBase(gameState);
            if (basePlacement == null) {
                return Collections.emptyList();
            }
            if (isFirstRowBuilt && (nextUnit == UnitType.FOOTMAN || nextUnit == UnitType.CAVALRY)) {
                nextUnit = damageUnits[random.nextInt(damageUnits.length)];
                if (playerMoney < gameState.getBarrackService().getBarrackCost(nextUnit)) {
                    return Collections.emptyList();
                }
            }
            BuildOrder order = new BuildOrder();
            order.setX(basePlacement.getX());
            order.setY(basePlacement.getY());
            order.setUnitType(nextUnit);
            orderList.add(order);
            lastBaseBuilt = basePlacement;
            nextUnit = isFirstRowBuilt ? damageUnits[random.nextInt(damageUnits.length)] : frontLineUnits[random.nextInt(frontLineUnits.length)];
        }
        if (!orderList.isEmpty()) {
            logger.info("Player {}, money {}, sending orders {}", gamePlayer.getPlayerId(), gameState.getPlayerService().getPlayerById(gamePlayer.getPlayerId()).getMoney(), orderList);
        }
        return orderList;
    }

    private Coordinate moveCursorToNextBase(GameState gameState) {
        if (lastBaseBuilt == null) {
            return baseCursor;
        }
        if (!gameState.isSpaceOccupied(lastBaseBuilt, 3)) {
            return lastBaseBuilt;
        }
        Coordinate basePlacement;
        basePlacement = lastBaseBuilt.nCellsToTheDirection(Direction.UP, 4);
        if (!gameState.isSpaceOccupied(basePlacement, 3)) {
            return basePlacement;
        }
        basePlacement = baseCursor;
        isFirstRowBuilt = true;
        while (gameState.isSpaceOccupied(basePlacement, 3)) {
            basePlacement = basePlacement.nCellsToTheDirection(baseCursorMovement, 4);
            if (gameState.getCellOwner(basePlacement) != gamePlayer.getPlayerId()) {
                isNoSpace = true;
                return null;
            }
        }
        return basePlacement;
    }

    @Override
    public void initAI(GamePlayer player) {
        gamePlayer = player;
        baseCursorMovement = gamePlayer.getBase().getX() == 0 ? Direction.LEFT : Direction.RIGHT;
        baseCursor = gamePlayer.getBase();
        if (baseCursorMovement.equals(Direction.LEFT)) {
            baseCursor = baseCursor.nCellsToTheDirection(Direction.RIGHT, 17);
        }
    }

    @Override
    public String getName() {
        return "SmartAI";
    }
}
