package io.cockroachdb.bootcamp.test;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
})
@Tag("integration-test")
//@ActiveProfiles({"dev", "default"})
public abstract class AbstractIntegrationTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected OrderDataService sampleDataService;

    protected void createCustomersAndProducts(int customerCount, int productCount) {
        sampleDataService.deleteAllData();
        sampleDataService.createCustomers(customerCount, TestDoubles::newCustomer);
        sampleDataService.createProducts(productCount, TestDoubles::newProduct);
    }
}

