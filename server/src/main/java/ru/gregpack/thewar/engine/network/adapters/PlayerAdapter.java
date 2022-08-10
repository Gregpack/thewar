package ru.gregpack.thewar.engine.network.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.engine.logic.GameLogic;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.network.messages.dto.mapping.GameStateMapper;
import ru.gregpack.thewar.network.messages.orders.Order;

import java.io.OutputStream;
import java.util.List;

public abstract class PlayerAdapter extends BasicAdapter implements Runnable {

    private static final Logger logger = LogManager.getLogger(PlayerAdapter.class.getName());

    protected final GameLogic gameLogic;
    protected final GamePlayer gamePlayer;
    protected boolean isDone;

    public PlayerAdapter(GamePlayer player, GameLogic gameLogic, OutputStream outputStream, GameStateMapper gameStateMapper, ObjectMapper objectMapper) {
        super(outputStream, gameStateMapper, objectMapper);
        this.gameLogic = gameLogic;
        this.gamePlayer = player;
    }

    public void run() {
        while (!isDone) {
            List<Order> orders = getNextOrders();
            if (orders == null) {
                this.isDone = true;
                return;
            }
            if (!orders.isEmpty()) {
                logger.info("{} got orders {}", this, orders);
                orders.forEach(gameLogic::handleOrder);
            }
        }
    }

    public abstract List<Order> getNextOrders();
}
