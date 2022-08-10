package ru.gregpack.thewar.engine.network.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.engine.logic.GameLogic;
import ru.gregpack.thewar.network.messages.dto.mapping.GameStateMapper;
import ru.gregpack.thewar.network.messages.OrderMessage;
import ru.gregpack.thewar.network.messages.RegisterConfirmMessage;
import ru.gregpack.thewar.network.messages.orders.Order;
import ru.gregpack.thewar.model.entities.GamePlayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class SocketPlayerAdapter extends PlayerAdapter {
    private static final Logger logger = LogManager.getLogger(SocketPlayerAdapter.class.getName());

    private final InputStream inputStream;

    public SocketPlayerAdapter(GamePlayer gamePlayer,
                               GameLogic gameLogic,
                               InputStream inputStream,
                               OutputStream outputStream,
                               GameStateMapper gameStateMapper,
                               ObjectMapper objectMapper) {
        super(gamePlayer, gameLogic, outputStream, gameStateMapper, objectMapper);
        this.inputStream = inputStream;
    }

    @Override
    public void onGameEnd(GamePlayer winner) {
        super.onGameEnd(winner);
        try {
            isDone = true;
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<Order> getNextOrders(){
        OrderMessage orderMessage;
        try {
            orderMessage = objectMapper.readValue(inputStream, OrderMessage.class);
        } catch (IOException e) {
            logger.error("Socket for {} is no longer working. Finishing work.", this);
            return null;
        }
        List<Order> orders = orderMessage.getOrders();
        orders.forEach(order -> order.setSenderId(gamePlayer.getPlayerId()));
        return orders;
    }

    @Override
    public void initAdapter() throws IOException {
        RegisterConfirmMessage registerConfirmMessage = new RegisterConfirmMessage(gamePlayer.getPlayerId());
        objectMapper.writeValue(outputStream, registerConfirmMessage);
    }

    @Override
    public String toString() {
        return "Socket for player " + gamePlayer;
    }
}
