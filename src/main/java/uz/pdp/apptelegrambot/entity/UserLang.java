package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class UserLang {
    @Id
    private Long userId;

    private String lang;
}
