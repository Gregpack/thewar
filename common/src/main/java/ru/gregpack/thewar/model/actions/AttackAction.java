package ru.gregpack.thewar.model.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AttackAction extends Action {
    private int attackedId;

    public AttackAction(int attackerId, int attackedId) {
        super(attackerId);
        this.attackedId = attackedId;
    }
}
