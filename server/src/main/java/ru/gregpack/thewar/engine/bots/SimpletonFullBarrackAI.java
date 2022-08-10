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
import java.util.List;
import java.util.Random;

public class SimpletonFullBarrackAI implements AI {
    private static final Logger logger = LogManager.getLogger(SimpletonFullBarrackAI.class.getName());

    private GamePlayer gamePlayer;
    private Coordinate lastBaseBuilt;
    private boolean isNoSpace = false;
    private final Random random = new Random();
    private UnitType nextUnit = UnitType.FOOTMAN;

    @Override
    public List<Order> nextOrders(GameState gameState) {
        List<Order> orderList = new ArrayList<>();
        Coordinate basePlacement = lastBaseBuilt;
        if (nextUnit == null) {
            nextUnit = UnitType.values()[random.nextInt(UnitType.values().length)];
        }
        Integer cost = gameState.getBarrackService().getBarrackCost(nextUnit);
        if (!isNoSpace && gameState.getPlayerService().getPlayerById(gamePlayer.getPlayerId()).getMoney() >= cost) {
            if (lastBaseBuilt == null) {
                basePlacement = gamePlayer.getBase().nCellsToTheDirection(Direction.RIGHT, 1).nCellsToTheDirection(Direction.UP, 1);
            } else {
                basePlacement = basePlacement.nCellsToTheDirection(Direction.UP, 4);
                if (gameState.isSpaceOccupied(basePlacement, 3)) {
                    basePlacement = gamePlayer.getBase().nCellsToTheDirection(Direction.RIGHT, 1).nCellsToTheDirection(Direction.UP, 1);
                    while (gameState.isSpaceOccupied(basePlacement, 3)) {
                        basePlacement = basePlacement.nCellsToTheDirection(Direction.RIGHT, 4);
                        if (gameState.getCellOwner(basePlacement) != gamePlayer.getPlayerId()) {
                            isNoSpace = true;
                            return orderList;
                        }
                    }
                }
            }
            BuildOrder order = new BuildOrder();
            order.setX(basePlacement.getX());
            order.setY(basePlacement.getY());
            order.setUnitType(nextUnit);
            orderList.add(order);
            lastBaseBuilt = basePlacement;
            nextUnit = UnitType.values()[random.nextInt(UnitType.values().length)];
        }
        if (!orderList.isEmpty()) {
            logger.info("Player {}, money {}, sending orders {}", gamePlayer.getPlayerId(), gameState.getPlayerService().getPlayerById(gamePlayer.getPlayerId()).getMoney(), orderList);
        }
        return orderList;
    }

    @Override
    public void initAI(GamePlayer player) {
        gamePlayer = player;
    }

    @Override
    public String getName() {
        return "Barrack Enjoyer";
    }
}
