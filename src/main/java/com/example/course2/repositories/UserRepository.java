package com.example.course2.repositories;

import com.example.course2.models.Role;
import com.example.course2.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserById(Long id);
    List<User> findByRole(Role role);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}