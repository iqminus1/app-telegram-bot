package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByExpireDayBeforeAndUnlimited(LocalDateTime expireDay, boolean unlimited);

    Optional<Order> findByUserIdAndGroupId(Long userId, Long groupId);

    Order save(Order order);
}