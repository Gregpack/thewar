package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonTypeName("StayStillOrder")
public class StayStillOrder extends UnitOrder {

    @Override
    public void execute(GameState gameState) {
        Unit target = gameState.getUnitService().getUnitById(this.getUnitId());
        if (target == null) {
            return;
        }
        gameState.getUnitService().markUnitAsMoved(getUnitId());
    }
}
