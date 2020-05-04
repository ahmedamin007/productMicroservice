package com.microservices.core.product;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.microservices.api.core.product.Product;
import com.microservices.core.product.persistence.ProductRepository;

import reactor.core.publisher.Mono;


//@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

	@Autowired
	private WebTestClient webTestCilent;
	@Autowired
	private ProductRepository repository;
	
    @BeforeEach
   	public void setupDb() {
   		repository.deleteAll();
    }
	
	@Test
	public void getProductById() {
		int productId = 1;
		postAndVerifyProduct(productId, HttpStatus.OK);
		assertTrue(repository.findByProductId(productId).isPresent());
		getAndVerifyProduct(String.valueOf(productId), HttpStatus.OK)
            .jsonPath("$.productId").isEqualTo(productId);
	}
	
//	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
//		return getAndVerifyProduct( productId, expectedStatus);
//	}

	@Test
	public void duplicateError() {

		int productId = 1;

		postAndVerifyProduct(productId, HttpStatus.OK);

		assertTrue(repository.findByProductId(productId).isPresent());

		postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
				/* .jsonPath("$.path").isEqualTo("/product") */
			.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);
	}

	@Test
	public void deleteProduct() {

		int productId = 1;

		postAndVerifyProduct(productId, HttpStatus.OK);
		assertTrue(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, HttpStatus.OK);
		assertFalse(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, HttpStatus.OK);
	}

	@Test
	public void getProductInvalidParameterString() {

		getAndVerifyProduct("no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/product")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getProductNotFound() {

		int productIdNotFound = 13;
		getAndVerifyProduct(String.valueOf(productIdNotFound) , HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product")
            .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}

	@Test
	public void getProductInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

		getAndVerifyProduct(String.valueOf(productIdInvalid)  , HttpStatus.UNPROCESSABLE_ENTITY)
           // .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}



	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return webTestCilent.get()
			.uri("/product?productId=" + productIdPath)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name " + productId, productId, "SA");
		return webTestCilent.post()
			.uri("/product")
			.body(Mono.just(product), Product.class)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return webTestCilent.delete()
			.uri("/product/" + productId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}

	

}
