package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
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
public class CodeGroup extends AbsLongEntity implements Serializable {
    private String code;

    private Long botId;

    private Long userId;

    private ExpireType type;

    private boolean active;

    private LocalDateTime activeAt;

    private Long tariffId;

    private Long tariffPrice;
}
