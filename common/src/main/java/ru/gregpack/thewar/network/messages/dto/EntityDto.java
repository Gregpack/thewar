package ru.gregpack.thewar.network.messages.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EntityDto extends IdentifiableDto {
    private int x;
    private int y;
}
