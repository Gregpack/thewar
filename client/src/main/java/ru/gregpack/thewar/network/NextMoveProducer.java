package ru.gregpack.thewar.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.gregpack.thewar.network.messages.OrderMessage;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;

public interface NextMoveProducer {
    OrderMessage nextOrders(GameStateDto gameState) throws JsonProcessingException;

    void initAI(int playerId);

    String getName();
}
