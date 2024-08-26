package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrambot.enums.ScreenshotStatus;

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

    private Long tariffId;

    private String path;

    @Enumerated(EnumType.STRING)
    private ScreenshotStatus status;

    private LocalDateTime activeAt;
}
