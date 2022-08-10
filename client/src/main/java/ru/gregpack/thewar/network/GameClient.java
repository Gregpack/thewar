package ru.gregpack.thewar.network;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.network.messages.*;

import java.io.IOException;
import java.net.Socket;

public class GameClient {
    private static final Logger logger = LogManager.getLogger(GameClient.class.getName());

    private ObjectMapper objectMapper;
    private final Socket socket;
    private boolean isDown = false;

    private NextMoveProducer moveProducer;

    public GameClient(String ip, Integer port) throws IOException {
        configureObjectMapper();
        socket = new Socket(ip, port);
        logger.info("Client created successfully on ip {} and port {}", ip, port);
        moveProducer = new JNIMoveProducer(objectMapper);
    }

    public void connect() throws IOException {
        logger.info("Connecting...");
        RegistrationMessage message = new RegistrationMessage();
        message.setRole(Role.PLAYER);
        message.setName(moveProducer.getName());
        objectMapper.writeValue(socket.getOutputStream(), message);

        RegisterConfirmMessage answer = objectMapper.readValue(socket.getInputStream(), RegisterConfirmMessage.class);
        moveProducer.initAI(answer.getPlayerId());
        logger.info("Connection successful: {}", answer);
        moveProducer = new JNIMoveProducer(objectMapper);
    }

    public void run() {
        while (!isDown) {
            GameStatusMessage nextDto;
            try {
                nextDto = objectMapper.readValue(socket.getInputStream(), GameStatusMessage.class);
                if (nextDto.getGameStatus().equals(GameStatus.END)) {
                    System.out.println("The winner is " + nextDto.getGameWinner().getName());
                    break;
                }
                OrderMessage orderMessage = moveProducer.nextOrders(nextDto.getGameState());
                objectMapper.writeValue(socket.getOutputStream(), orderMessage);
            } catch (JsonProcessingException e) {
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println(e.getMessage());
                break;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        this.isDown = true;
    }

    private void configureObjectMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper = new ObjectMapper(jsonFactory);
        objectMapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
    }

}
