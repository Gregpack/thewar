package ru.gregpack.thewar.network.messages.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;

@EqualsAndHashCode(callSuper = true)
@Data
public class BarrackDto extends EntityDto {
    private int spawnX;
    private int spawnY;
    private int playerId;
    private UnitType unitType;
}
