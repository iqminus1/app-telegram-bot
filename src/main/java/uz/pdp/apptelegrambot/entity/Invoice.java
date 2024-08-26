package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrambot.enums.ExpireType;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class Invoice extends AbsLongEntity implements Serializable {
    private String number;

    private Long userId;

    private Long groupId;

    private Long amount;

    private ExpireType type;
}
