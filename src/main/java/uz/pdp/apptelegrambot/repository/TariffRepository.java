package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Tariff;

import java.util.List;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    @Cacheable(value = "tariffEntity", key = "#id")
    @Override
    Optional<Tariff> findById(Long id);

    @CachePut(value = "tariffEntity", key = "#tariff.id")
    default Optional<Tariff> saveOptional(Tariff tariff) {
        return Optional.of(save(tariff));
    }

    List<Tariff> findAllByBotId(Long botId);
}