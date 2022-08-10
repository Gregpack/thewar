package ru.gregpack.thewar.network;

import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.network.messages.orders.Order;

import java.util.List;

public interface AI {
    List<Order> nextOrders(GameState gameState);
    void initAI(GamePlayer player);
    String getName();
}
