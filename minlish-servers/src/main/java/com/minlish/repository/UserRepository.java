package com.minlish.repository;

import com.minlish.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:40
 * File      : UserRepository
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}