package io.cockroachdb.bootcamp.patterns.parallel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SumInventoryTask extends RecursiveTask<BigDecimal> {
    private final List<String> cities;

    private final ProductVariationRepository productVariationRepository;

    public SumInventoryTask(List<String> cities,
                            ProductVariationRepository productVariationRepository) {
        this.cities = cities;
        this.productVariationRepository = productVariationRepository;
    }

    @Override
    protected BigDecimal compute() {
        BigDecimal total = BigDecimal.ZERO;

        if (cities.size() == 1) {
            total = productVariationRepository.sumInventoryByCountry(cities.getFirst());
        } else {
            List<SumInventoryTask> subTasks = new ArrayList<>();

            cities.forEach(city -> subTasks.add(
                    new SumInventoryTask(List.of(city), productVariationRepository)));

            subTasks.forEach(ForkJoinTask::fork);

            for (SumInventoryTask sumInventoryTask : subTasks) {
                total = total.add(sumInventoryTask.join());
            }
        }

        return total;
    }
}
