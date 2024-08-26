package uz.pdp.apptelegrambot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity(name = "users")
public class User implements Serializable {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private StateEnum state;


}
