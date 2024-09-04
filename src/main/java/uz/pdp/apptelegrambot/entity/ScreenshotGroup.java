package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.Status;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class ScreenshotGroup extends AbsLongEntity implements Serializable {
    private Long groupId;

    private Long sendUserId;

    private String path;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime activeAt;

    private Long tariffId;

    private Long tariffPrice;

    private ExpireType type;
}
