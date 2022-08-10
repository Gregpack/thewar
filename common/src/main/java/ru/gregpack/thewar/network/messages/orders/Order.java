package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gregpack.thewar.model.GameState;

@Data
@NoArgsConstructor
@JsonSubTypes({
        @JsonSubTypes.Type(value = UnitOrder.class, name = "UnitOrder"),
        @JsonSubTypes.Type(value = BuildOrder.class, name = "BuildOrder"),
        @JsonSubTypes.Type(value = DestroyOrder.class, name = "DestroyOrder"),
        @JsonSubTypes.Type(value = EmptyOrder.class, name = "EmptyOrder")}
)
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class Order {

    private int senderId;

    public abstract void execute(GameState gameState);

}
