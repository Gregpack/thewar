package ru.gregpack.thewar.model.entities.composite.units;

import lombok.ToString;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.SkillActivator;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.util.Map;

@ToString(callSuper = true, includeFieldNames = false)
public class Assassin extends Unit {

    private static final String propertyName = "assassin";
    private final int skillRange;

    public Assassin() {
        super(propertyName);
        String prefix = defaultPropertyPrefix + "." + propertyName;
        Map<String, String> stats = PropertyUtil.getPropertyMap(prefix);
        this.skillRange = Integer.parseInt(stats.getOrDefault("skillrange", "5"));
    }

    public Assassin(Coordinate position) {
        super(propertyName, position);
        String prefix = defaultPropertyPrefix + "." + propertyName;
        Map<String, String> stats = PropertyUtil.getPropertyMap(prefix);
        this.skillRange = Integer.parseInt(stats.getOrDefault("skillrange", "5"));
    }

    @Override
    public void useSkill(SkillActivator skillActivator) {
        Coordinate newPosition = new Coordinate(skillActivator.getX(), skillActivator.getY());
        if (!getPosition().isInManhattanRange(newPosition, skillRange)) {
            throw new IllegalArgumentException("You can't teleport here - it is not in range!");
        }
        Coordinate oldPosition = this.getPosition();
        doMove(oldPosition, newPosition, oldPosition.sub(newPosition).toDirection());
    }
}
