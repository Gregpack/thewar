package ru.gregpack.thewar.engine.bots;

import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.network.AI;
import ru.gregpack.thewar.network.messages.orders.Order;

import java.util.ArrayList;
import java.util.List;

public class MuteAI implements AI {

    @Override
    public List<Order> nextOrders(GameState gameState) {
        return new ArrayList<>();
    }

    @Override
    public void initAI(GamePlayer player) {
    }

    @Override
    public String getName() {
        return "MuteAI";
    }
}
