package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.apptelegrambot.entity.UserLang;

public interface UserLangRepository extends JpaRepository<UserLang, Long> {

}