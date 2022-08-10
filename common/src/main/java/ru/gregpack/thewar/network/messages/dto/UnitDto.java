package ru.gregpack.thewar.network.messages.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.gregpack.thewar.model.entities.basic.Armor;
import ru.gregpack.thewar.model.entities.basic.Attack;
import ru.gregpack.thewar.model.entities.composite.units.Unit;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class UnitDto extends EntityDto {
    private double healthPoints;
    private double totalHealthPoints;
    private int moneyWorth;
    private int speed;
    private Attack attack;
    private Armor armor;
    private Unit.Behaviour currentBehaviour;
    private int behaviourTriggerUnitId;
    private UnitType unitType;
    private int playerId;
}
