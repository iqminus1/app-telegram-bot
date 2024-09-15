package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class Invoice extends AbsLongEntity implements Serializable {
    private Long userId;

    private Long botId;

    private Long amount;

    private Long tariffId;

    private LocalDateTime at;
}
