package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    @Cacheable(value = "userEntity", key = "#id")
    Optional<User> findById(Long id);

    @CachePut(value = "userEntity", key = "#user.id")
    default Optional<User> saveOptional(User user) {
        return Optional.of(save(user));
    }
}