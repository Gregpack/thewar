package ru.gregpack.thewar.engine.bots;

import lombok.Getter;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;
import ru.gregpack.thewar.network.AI;

public enum BotType {
    Easy(1),
    Medium(2),
    Hard(3);
    @Getter
    private int value;
    BotType(int i) {
        value = i;
    }

    public AI toAI() {
        switch (this) {
            case Easy:
                return new SimpletonAI(UnitType.FOOTMAN);
            case Medium:
                return new SimpletonFullBarrackAI();
            case Hard:
                return new SmartAI();
        }
        return new SimpletonAI(UnitType.FOOTMAN);
    }
}
