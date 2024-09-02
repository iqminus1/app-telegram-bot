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
@Entity(name = "orders")
public class Order extends AbsLongEntity implements Serializable {
    private Long userId;

    private Long groupId;

    private LocalDateTime expireDay;

    private boolean unlimited;
}
