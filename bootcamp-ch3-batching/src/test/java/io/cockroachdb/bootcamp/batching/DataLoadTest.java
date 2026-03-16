package io.cockroachdb.bootcamp.batching;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.BatchingApplication;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;
import io.cockroachdb.bootcamp.test.TestDoubles;

@SpringBootTest(classes = {BatchingApplication.class})
public class DataLoadTest extends AbstractIntegrationTest {
    @Test
    @Order(1)
    public void deleteTestDta() {
        sampleDataService.deleteAllData();
    }

    @Test
    @Order(2)
    public void createCustomers() {
        sampleDataService.createCustomers(256, TestDoubles::newCustomer);
    }

    @Test
    @Order(3)
    public void createProducts() {
        sampleDataService.createProducts(256, TestDoubles::newProduct);
    }
}
