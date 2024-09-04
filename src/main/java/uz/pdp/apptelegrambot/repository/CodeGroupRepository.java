package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.CodeGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {

    Optional<CodeGroup> findByCodeAndBotIdAndActive(String code, Long botId, boolean active);


    List<CodeGroup> findAllByBotIdAndActiveAtBetween(Long botId, LocalDateTime from, LocalDateTime to);

    List<CodeGroup> findAllByBotIdAndActive(Long botId, boolean active);
}