package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "groups")
public class Group extends AbsLongEntity implements Serializable {
    private String name;

    private Long adminId;

    private Long groupId;

    private String botToken;

    private String botUsername;

    private String cardNumber;

    private String cardFirstName;

    private LocalDateTime expireAt;

    private boolean allowPayment;

    private boolean payment;

    private boolean code;

    private boolean screenShot;

}
