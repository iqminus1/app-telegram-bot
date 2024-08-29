package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Tariff;

import java.util.List;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    @Cacheable(value = "tariffEntity", key = "#id")
    default Tariff getById(Long id) {
        return findById(id).orElseThrow();
    }

    @CacheEvict(value = "tariffEntityByBotId", key = "#result.botId")
    @CachePut(value = "tariffEntity", key = "#result.id")
    default Tariff saveOptional(Tariff tariff) {
        return save(tariff);
    }

    @Cacheable(value = "tariffEntityByBotId", key = "#botId")
    List<Tariff> findAllByBotId(Long botId);

    @CacheEvict(value = "tariffEntity", key = "#tariff.id")
    @Override
    void delete(Tariff tariff);

    @CacheEvict(value = "tariffEntityByBotId", key = "#tariff.botId")
    default void deleteAndClearCache(Tariff tariff) {
        delete(tariff);
    }
}