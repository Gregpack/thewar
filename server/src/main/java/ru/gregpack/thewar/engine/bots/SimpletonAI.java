package ru.gregpack.thewar.engine.bots;

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

public class SimpletonAI implements AI {

    private final UnitType unitType;
    private GamePlayer gamePlayer;

    public SimpletonAI(UnitType unitType) {
        this.unitType = unitType;
    }

    @Override
    public List<Order> nextOrders(GameState gameState) {
        List<Order> orderList = new ArrayList<>();
        Coordinate base = gamePlayer.getBase();
        base = base.nCellsToTheDirection(Direction.RIGHT, 1);
        base = base.nCellsToTheDirection(Direction.UP, 2);
        if (!gameState.isSpaceOccupied(base, 3)) {
            BuildOrder order = new BuildOrder();
            order.setX(base.getX());
            order.setY(base.getY());
            order.setUnitType(unitType);
            orderList.add(order);
        }
        return orderList;
    }

    @Override
    public void initAI(GamePlayer player) {
        gamePlayer = player;
    }

    @Override
    public String getName() {
        return "Simpleton";
    }
}
