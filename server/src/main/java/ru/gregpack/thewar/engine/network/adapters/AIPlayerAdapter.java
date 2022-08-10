package ru.gregpack.thewar.engine.network.adapters;

import lombok.SneakyThrows;
import ru.gregpack.thewar.engine.logic.GameLogic;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.network.AI;
import ru.gregpack.thewar.network.messages.OrderMessage;
import ru.gregpack.thewar.network.messages.orders.Order;

import java.util.List;

public class AIPlayerAdapter extends PlayerAdapter {

    private GameState lastGameState = null;
    private final AI ai;

    public AIPlayerAdapter(GamePlayer gamePlayer, GameLogic gameLogic, AI ai) {
        super(gamePlayer, gameLogic, null, null, null);
        this.ai = ai;
    }

    @Override
    public void onNewGameState(GameState gameState) {
        synchronized (this) {
            lastGameState = gameState;
            notify();
        }
    }

    @Override
    public void onGameEnd(GamePlayer winner) {
        synchronized (this) {
            isDone = true;
            notify();
        }
    }

    @SneakyThrows
    @Override
    public List<Order> getNextOrders() {
        synchronized (this) {
            wait();
            List<Order> orders = ai.nextOrders(lastGameState);
            orders.forEach(order -> order.setSenderId(gamePlayer.getPlayerId()));
            return orders;
        }
    }

    @Override
    public void initAdapter() {
        ai.initAI(gamePlayer);
    }
}
