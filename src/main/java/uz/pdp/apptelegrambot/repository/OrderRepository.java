package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByExpireDayAfterAndExpireDayBeforeAndUnlimited(LocalDateTime after, LocalDateTime before, boolean unlimited);

    Optional<Order> findByUserIdAndGroupId(Long userId, Long groupId);
}