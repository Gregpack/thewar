package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("DestroyOrder")
public class DestroyOrder extends Order {

    private int barrackId;
    @Override
    public void execute(GameState gameState) {
        int cost = gameState.getBarrackService().removeBarrack(barrackId, this.getSenderId());
        gameState.getPlayerService().getPlayerById(this.getSenderId()).addMoney(cost / 2);
    }
}
