package ru.gregpack.thewar.network;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;
import ru.gregpack.thewar.network.messages.dto.PlayerDto;
import ru.gregpack.thewar.network.messages.GameStatusMessage;
import ru.gregpack.thewar.network.messages.RegistrationMessage;
import ru.gregpack.thewar.network.messages.Role;
import ru.gregpack.thewar.network.messages.ViewerConfirmMessage;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

public class ViewerClient {
    private static final Logger logger = LogManager.getLogger(ViewerClient.class.getName());

    private ObjectMapper objectMapper;
    private final Socket socket;
    private boolean isDown = false;

    public ViewerClient(String ip, Integer port) throws IOException {
        configureObjectMapper();
        socket = new Socket(ip, port);
        socket.setSoTimeout(2000);
        logger.info("Client created successfully on ip {} and port {}", ip, port);
    }

    public ViewerConfirmMessage connect() throws IOException {
        logger.info("Connecting...");
        RegistrationMessage message = new RegistrationMessage();
        message.setRole(Role.VIEWER);
        objectMapper.writeValue(socket.getOutputStream(), message);

        ViewerConfirmMessage answer = objectMapper.readValue(socket.getInputStream(), ViewerConfirmMessage.class);
        logger.info("Connection successful: {}", answer);
        return answer;
    }

    public void listen(Consumer<GameStateDto> onGameState, Runnable onFail, Consumer<PlayerDto> onGameEnd) {
        Thread thread = new Thread(() -> {
            while (!isDown) {
                GameStatusMessage nextDto;
                try {
                    nextDto = objectMapper.readValue(socket.getInputStream(), GameStatusMessage.class);
                } catch (SocketTimeoutException ignored) {
                    continue;
                } catch (JsonProcessingException e) {
                    //System.err.println(e.getMessage());
                    continue;
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    onFail.run();
                    return;
                }
                switch (nextDto.getGameStatus()) {
                    case IN_PROGRESS:
                        onGameState.accept(nextDto.getGameState());
                        break;
                    case END:
                        onGameEnd.accept(nextDto.getGameWinner());
                        shutdown();
                        break;
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void shutdown() {
        this.isDown = true;
    }

    private void configureObjectMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper = new ObjectMapper(jsonFactory);
        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
    }

}
