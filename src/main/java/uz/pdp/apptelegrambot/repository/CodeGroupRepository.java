package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.CodeGroup;

import java.util.Optional;

@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {
    @Cacheable(value = "codeGroupEntity", key = "#code" + "+" + "#botId")
    Optional<CodeGroup> findByCodeAndBotId(String code, Long botId);

    @CachePut(value = "codeGroupEntity", key = "#result.code" + "+" + "#result.botId")
    default CodeGroup saveOptional(CodeGroup codeGroup) {
        return save(codeGroup);
    }

}