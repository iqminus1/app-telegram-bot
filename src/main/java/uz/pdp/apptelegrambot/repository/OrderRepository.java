package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}