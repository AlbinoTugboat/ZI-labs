package com.example.informationprotection.repository.license;

import com.example.informationprotection.entity.license.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
