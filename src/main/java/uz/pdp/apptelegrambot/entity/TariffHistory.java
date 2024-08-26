package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrambot.enums.ExpireType;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class TariffHistory extends AbsLongEntity implements Serializable {
    private LocalDateTime createAt;

    private Long groupId;

    private ExpireType type;

    private Long price;

    private LocalDateTime deleteAt;
}
