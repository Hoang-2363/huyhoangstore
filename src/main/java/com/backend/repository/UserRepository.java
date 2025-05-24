package com.backend.repository;

import com.backend.model.Role;
import com.backend.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    List<User> findAllByRole(Role role, Sort sort);

    List<User> findAllByIdNot(Long id, Sort sort);

    Optional<User> findByIdAndRole(Long id, Role role);

}