package com.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.microservices.api.core.product.Product;
import com.microservices.api.core.product.ProductService;
import com.microservices.core.product.persistence.ProductEntity;
import com.microservices.core.product.persistence.ProductRepository;
import com.microservices.util.exceptions.InvalidInputException;
import com.microservices.util.exceptions.NotFoundException;
import com.microservices.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository , ProductMapper mapper , ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
        this.productRepository= productRepository;
        this.mapper=mapper;
        
    }

    @Override
    public Product getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        ProductEntity entity = productRepository.findByProductId(productId).
        		orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
        
        Product productResponse= mapper.entityToApi(entity);
        productResponse.setServiceAddress(serviceUtil.getServiceAddress());
        return productResponse;
    }

	@Override
	public Product createProduct(Product product) {
		ProductEntity entity = mapper.apiToEntity(product);
		ProductEntity newEntity = productRepository.save(entity);
		return mapper.entityToApi(newEntity);
	}

	@Override
	public void deleteProduct(int productId) {
		productRepository.findByProductId(productId).
		ifPresent( e -> productRepository.delete(e));
		
	}
    
   

}