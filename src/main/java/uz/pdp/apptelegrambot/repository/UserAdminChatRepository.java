package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.UserAdminChat;
import uz.pdp.apptelegrambot.enums.Status;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAdminChatRepository extends JpaRepository<UserAdminChat, Long> {
    List<UserAdminChat> findAllByBotIdAndStatus(Long botId, Status status);
    Optional<UserAdminChat> findByAdminGetMessageId(Integer adminGetMessageId);
}