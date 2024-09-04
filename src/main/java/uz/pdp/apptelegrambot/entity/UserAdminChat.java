package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import uz.pdp.apptelegrambot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrambot.enums.Status;

import java.sql.Types;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class UserAdminChat extends AbsLongEntity {
    private Long senderId;

    private Integer messageId;

    private String text;

    private Long botId;

    @Enumerated(value = EnumType.STRING)
    private Status status;

    private Integer adminGetMessageId;

    @JdbcTypeCode(value = Types.ARRAY)
    private List<String> sendingMessages;
}
