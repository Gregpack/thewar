package ru.gregpack.thewar.model;

import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.SkillActivator;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

public interface UnitSubscriber {
    void onAttack(Unit attacker, Unit attacked);

    void onMove(Unit unit, Coordinate oldPosition, Coordinate newPosition);

    void onSkill(Unit unit, SkillActivator skillActivator);
}
