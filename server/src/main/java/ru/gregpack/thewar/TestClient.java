package ru.gregpack.thewar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.gregpack.thewar.network.messages.RegisterConfirmMessage;
import ru.gregpack.thewar.network.messages.RegistrationMessage;
import ru.gregpack.thewar.network.messages.Role;

import java.io.IOException;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET,false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        RegistrationMessage message = new RegistrationMessage();
        message.setRole(Role.VIEWER);
        Socket socket = new Socket("127.0.0.1", 9999);
        socket.getOutputStream().write(objectMapper.writeValueAsBytes(message));
        RegisterConfirmMessage message1 = objectMapper.readValue(socket.getInputStream(), RegisterConfirmMessage.class);
        System.out.println(message1);
        Thread.sleep(10000);
    }
}
