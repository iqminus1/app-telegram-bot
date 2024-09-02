    package uz.pdp.apptelegrambot.repository;
    
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Repository;
    import uz.pdp.apptelegrambot.entity.Tariff;
    
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.ConcurrentMap;
    
    @Repository
    public interface TariffRepository extends JpaRepository<Tariff, Long> {
        ConcurrentMap<Long, Tariff> byId = new ConcurrentHashMap<>();
        ConcurrentMap<Long, List<Tariff>> allByBotId = new ConcurrentHashMap<>();
    
        default Tariff getById(Long id) {
            if (byId.containsKey(id)) {
                return byId.get(id);
            }
            byId.put(id, findById(id).orElseThrow());
            return getById(id);
        }
    
        default Tariff saveOptional(Tariff tariff) {
            Tariff save = save(tariff);
            byId.put(save.getId(), save);
            putAllByBotId(tariff);
            return save;
        }
    
        default void putAllByBotId(Tariff tariff) {
            Long botId = tariff.getBotId();
            if (allByBotId.containsKey(botId)) {
                List<Tariff> tariffs = allByBotId.get(botId);
                Optional<Tariff> first = tariffs.stream().filter(t -> t.getBotId().equals(botId)).findFirst();
                if (first.isEmpty()) {
                    tariffs.add(tariff);
                    allByBotId.put(botId, tariffs);
                    return;
                }
                tariffs.remove(first.get());
                tariffs.add(tariff);
                allByBotId.put(botId, tariffs);
            } else {
                allByBotId.put(botId, new ArrayList<>(List.of(tariff)));
            }
        }
    
    
        List<Tariff> findAllByBotId(Long botId);
    
        default List<Tariff> findAllByBotIdDefault(Long botId) {
            if (allByBotId.containsKey(botId)) {
                return allByBotId.get(botId);
            }
            allByBotId.put(botId, findAllByBotId(botId));
            return findAllByBotIdDefault(botId);
        }
    
        default void deleteDefault(Tariff tariff) {
            byId.remove(tariff.getId());
            if (allByBotId.containsKey(tariff.getBotId())) {
                List<Tariff> tariffs = allByBotId.get(tariff.getBotId());
                Optional<Tariff> first = tariffs.stream().filter(t -> t.getBotId().equals(tariff.getBotId())).findFirst();
                if (first.isPresent()) {
                    tariffs.remove(first.get());
                    allByBotId.put(tariff.getBotId(), tariffs);
                }
            }
            delete(tariff);
        }
    
        @Scheduled(cron = "0 0 4 * * ?")
        default void clearCache() {
            byId.clear();
            allByBotId.clear();
        }
    }