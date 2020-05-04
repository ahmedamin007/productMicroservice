package com.microservices.core.product;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.print.Pageable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.print.attribute.standard.PageRanges;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
//import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;

import com.microservices.core.product.persistence.ProductEntity;
import com.microservices.core.product.persistence.ProductRepository;


//@RunWith(SpringRunner.class)
@DataMongoTest
class PersistenceTests {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;
    
    
    @BeforeEach
   	public void setupDb() {
   		repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity);

        assertEqualsProduct(entity, savedEntity);
    }
    
    @Test  
   	public void duplicateError() {
    	ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
    	Assertions.assertThrows(DuplicateKeyException.class , () -> {
            repository.save(entity);
    	});

    }
    
    @Test
   	public void create() {

        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        repository.save(newEntity);

        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }
    

    
    @Test
    public void update() {
    	savedEntity.setName("ahmed");
    	repository.save(savedEntity);
    	ProductEntity foundEntity= repository.findById(savedEntity.getId()).get();
    	assertEquals(1, (long) foundEntity.getVersion());
    	assertEquals("ahmed", foundEntity.getName());
 
    }
    


    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }
    
    @Test
    public void duplicateKeyExcept() {
        Assertions.assertThrows(DuplicateKeyException.class, () -> {
        	ProductEntity entity=  new ProductEntity(savedEntity.getProductId(), "n", 1);
        	repository.save(entity);
          });
    }

    
    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setName("n2");
            repository.save(entity2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }
    
//    @Test
//    public void paging() {
//
//        repository.deleteAll();
//
//        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
//            .mapToObj(i -> new ProductEntity(i, "name " + i, i))
//            .collect(Collectors.toList());
//        repository.saveAll(newProducts);
//
//        Pageable nextPage = (Pageable) PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
//        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
//        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
//        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
//    }
//
//    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
//        Page<ProductEntity> productPage = (Page<ProductEntity>) repository.findAll((Sort) nextPage);
//        assertEquals(expectedProductIds, productPage.getContent().stream().map(p -> p.getProductId()).collect(Collectors.toList()).toString());
//        assertEquals(expectsNextPage, productPage.hasNext());
//        return (Pageable) productPage.nextPageable();
//    }
    
    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(),           actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
    }

}
