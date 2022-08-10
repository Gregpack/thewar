package ru.gregpack.thewar.model.entities.basic;

import lombok.*;

@Getter
@Setter
@ToString(includeFieldNames = false)
@EqualsAndHashCode
public class Attack {
    public enum AttackType {
        Normal,
        Sharp,
        Fire
    }
    public Attack() {}

    private int attackPower;
    private int attackRange;
    private int attackCooldown;
    private AttackType attackType;
}
