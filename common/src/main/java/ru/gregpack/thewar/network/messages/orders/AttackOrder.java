package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonTypeName("AttackOrder")
public class AttackOrder extends UnitOrder {

    private int attackedId;

    @Override
    public void execute(GameState gameState) {
        if (gameState.getUnitService().getUnitOwnerId(attackedId) == this.getSenderId()) {
            return;
        }
        Unit attacker = gameState.getUnitService().getUnitById(this.getUnitId());
        Unit attacked = gameState.getUnitService().getUnitById(attackedId);
        if (attacked == null || attacker == null) {
            return;
        }
        attacker.setDesignatedTarget(attacked);
    }
}
