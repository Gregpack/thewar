package ru.gregpack.thewar.engine.logic;

import ru.gregpack.thewar.model.GameTickSubscriber;
import ru.gregpack.thewar.network.messages.orders.Order;
import ru.gregpack.thewar.model.entities.GamePlayer;

public interface GameLogic {

    void addSubscriber(GameTickSubscriber gameStateSubscriber);

    void removeSubscriber(GameTickSubscriber gameStateSubscriber);

    GamePlayer createPlayer(String name);

    boolean nextTick();

    void handleOrder(Order order);

}
