package com.backend.repository;

import com.backend.model.Order;
import com.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByEmailUser(String emailUser);

    List<Order> findByUser(User user);
}
