package ru.gregpack.thewar.network.messages;


import lombok.*;
import ru.gregpack.thewar.model.entities.basic.Coordinate;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ViewerConfirmMessage {

    private Integer length;
    private Integer height;
    private Integer baseLength;
    private Integer baseHeight;
    private String goldToWin;
    private List<Coordinate> bases;
    private Integer tickRate;
}
