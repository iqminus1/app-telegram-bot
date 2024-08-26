package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;

import java.io.Serializable;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity(name = "orders")
public class Order extends AbsLongEntity implements Serializable {
    private Long userId;

    private Long groupId;

    private Timestamp expireDay;

    private boolean unlimited;
}
