package com.example.informationprotection.service.license;

import com.example.informationprotection.entity.license.Product;
import com.example.informationprotection.exception.NotFoundException;
import com.example.informationprotection.repository.license.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductOrFail(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
    }
}
