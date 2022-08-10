import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.gregpack.thewar.network.JNIMoveProducer;
import ru.gregpack.thewar.network.messages.GameStatusMessage;
import ru.gregpack.thewar.network.messages.OrderMessage;

import java.io.File;
import java.io.IOException;

public class ClientTest {

    @Test
    public void test() throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        objectMapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        JNIMoveProducer jniMoveProducer = new JNIMoveProducer(objectMapper);
        GameStatusMessage gameStatusMessage = objectMapper.readValue(new File("src/test/resources/test.json"), GameStatusMessage.class);
        jniMoveProducer.initAI(1);
        OrderMessage orderMessage = jniMoveProducer.nextOrders(gameStatusMessage.getGameState());
        orderMessage = jniMoveProducer.nextOrders(gameStatusMessage.getGameState());
        orderMessage = jniMoveProducer.nextOrders(gameStatusMessage.getGameState());
        orderMessage = jniMoveProducer.nextOrders(gameStatusMessage.getGameState());
        orderMessage = jniMoveProducer.nextOrders(gameStatusMessage.getGameState());

        Assertions.assertNotNull(orderMessage);
    }
}
