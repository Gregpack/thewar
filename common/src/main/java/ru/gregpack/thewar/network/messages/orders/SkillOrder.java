package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.SkillActivator;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonTypeName("SkillOrder")
public class SkillOrder extends UnitOrder {

    private SkillActivator skillActivator;

    @Override
    public void execute(GameState gameState) {
        Unit target = gameState.getUnitService().getUnitById(this.getUnitId());
        if (target == null) {
            return;
        }
        target.callSkill(skillActivator);
    }
}
