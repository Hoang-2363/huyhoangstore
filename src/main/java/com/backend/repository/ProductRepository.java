package com.backend.repository;

import com.backend.model.Brand;
import com.backend.model.Category;
import com.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);

    Optional<Product> findTopByOrderByIdDesc();

    boolean existsByBrand(Brand brand);

    boolean existsByCategoriesContaining(Category category);

    List<Product> findByIdIn(List<Long> ids);
}
