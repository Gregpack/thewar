package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonTypeName;
import ru.gregpack.thewar.model.GameState;

@JsonTypeName("EmptyOrder")
public class EmptyOrder extends Order {
    @Override
    public void execute(GameState gameState) {
    }
}
