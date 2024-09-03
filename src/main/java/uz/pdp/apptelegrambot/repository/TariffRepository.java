package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Tariff;

import java.util.List;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    default Tariff getById(long id) {
        return findById(id).orElseThrow();
    }

    default void saveOptional(Tariff tariff) {
        save(tariff);
    }


    List<Tariff> findAllByBotId(Long botId);

    default List<Tariff> findAllByBotIdDefault(Long botId) {
        return findAllByBotId(botId);
    }

    default void deleteDefault(Tariff tariff) {
        delete(tariff);
    }

}