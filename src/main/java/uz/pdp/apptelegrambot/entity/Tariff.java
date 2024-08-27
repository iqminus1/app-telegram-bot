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
public class Tariff extends AbsLongEntity implements Serializable {
    private Long botId;

    private ExpireType type;

    private Long price;

}
