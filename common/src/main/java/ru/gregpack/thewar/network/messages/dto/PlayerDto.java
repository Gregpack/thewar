package ru.gregpack.thewar.network.messages.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerDto extends IdentifiableDto {
    private int money;
    private String name;
    private int baseX;
    private int baseY;
}
