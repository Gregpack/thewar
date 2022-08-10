package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Coordinate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("BuildOrder")
public class BuildOrder extends Order {

    private int x;
    private int y;
    private UnitType unitType;

    @Override
    public void execute(GameState gameState) {
        gameState.getBarrackService().createBarrack(new Coordinate(x, y), unitType, this.getSenderId());
    }
}
