package io.cockroachdb.bootcamp.patterns.parallel;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public InventoryRepository(@Autowired DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insertProducts(int inventory, String country, int numProducts) {
        jdbcTemplate.update("""
                insert into product_variation (inventory, name, price, sku, country)
                select ?,
                       md5(random()::text),
                       500.00 + random() * 500.00,
                       gen_random_uuid()::text,
                       ?
                from generate_series(1, ?) as i
                """, inventory, country, numProducts);
    }

    public BigDecimal sumInventory() {
        return jdbcTemplate.queryForObject(
                "select sum(inventory) from product_variation", BigDecimal.class);
    }

    public BigDecimal sumInventoryByCountry(String country) {
        return jdbcTemplate.queryForObject(
                "select sum(inventory) from product_variation pv where pv.country = ?",
                BigDecimal.class, country);
    }
}

