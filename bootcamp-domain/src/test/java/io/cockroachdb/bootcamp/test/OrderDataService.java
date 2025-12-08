package io.cockroachdb.bootcamp.test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.model.Customer;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.repository.CustomerRepository;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.StreamUtils;

@Service
public class OrderDataService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllData() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active");

        orderRepository.deleteAllOrderItems();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createProducts(int numProducts, Supplier<Product> supplier) {
        IntStream.rangeClosed(1, numProducts)
                .forEach(value -> productRepository.save(supplier.get()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createCustomers(int numCustomers, Supplier<Customer> supplier) {
        List<Customer> customers = IntStream.rangeClosed(1, numCustomers)
                .mapToObj(value -> supplier.get())
                .toList();

        StreamUtils.chunkedStream(customers.stream(), 128).forEach(chunk -> {
            customerRepository.saveAll(chunk);
        });
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> T withRandomCustomersAndProducts(int customerCount, int productCount,
                                                BiConsumerAction<List<Customer>, List<Product>, T> action) {
        List<Customer> customers = customerRepository.findAllById(
                customerRepository.findRandomUniqueIds(customerCount));
        List<Product> products = productRepository.findAllById(
                productRepository.findRandomUniqueIds(productCount));
        return action.accept(customers, products);
    }
}
