package ru.gregpack.thewar.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.gregpack.thewar.network.messages.OrderMessage;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;

public class JNIMoveProducer implements NextMoveProducer {
    private final ObjectMapper objectMapper;
    private int playerId;

    private native String produceNextMove(String gameStatus, int playerId);
    private native String getNameNative();


    static {
        System.loadLibrary("libexample");
    }

    public JNIMoveProducer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public OrderMessage nextOrders(GameStateDto gameState) throws JsonProcessingException {
        String gameStateJson = objectMapper.writeValueAsString(gameState);
        //System.out.println("Sending json to dll: " + gameStateJson);
        String nextMove = produceNextMove(gameStateJson, playerId);
        return objectMapper.readValue(nextMove, OrderMessage.class);
    }

    @Override
    public void initAI(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public String getName() {
        return getNameNative();
    }
}
