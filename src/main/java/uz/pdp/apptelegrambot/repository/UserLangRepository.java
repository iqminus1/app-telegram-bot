package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.apptelegrambot.entity.UserLang;

import java.util.Optional;

public interface UserLangRepository extends JpaRepository<UserLang, Long> {
    @Cacheable(value = "userLangEntity", key = "#userId")
    @Override
    Optional<UserLang> findById(Long userId);

    @CacheEvict(value = "userLang", key = "#user.userId")
    @CachePut(value = "userLangEntity", key = "#user.userId")
    default Optional<UserLang> saveOptional(UserLang user) {
        clearCacheButton(user.getUserId());
        return Optional.of(save(user));
    }

    @CacheEvict(value = "responseButton", key = "#userId")
    default void clearCacheButton(Long userId) {

    }
}