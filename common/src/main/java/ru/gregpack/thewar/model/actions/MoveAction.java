package ru.gregpack.thewar.model.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveAction extends Action {
    private int startX;
    private int startY;
    private int toX;
    private int toY;
}
