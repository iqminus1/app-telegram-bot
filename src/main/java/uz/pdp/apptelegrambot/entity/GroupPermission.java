package uz.pdp.apptelegrambot.entity;


import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class GroupPermission extends AbsLongEntity implements Serializable {
    private Long groupId;

    private boolean payment;
}
