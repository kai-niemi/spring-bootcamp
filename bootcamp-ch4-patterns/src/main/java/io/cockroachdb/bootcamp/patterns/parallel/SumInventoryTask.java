package io.cockroachdb.bootcamp.patterns.parallel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SumInventoryTask extends RecursiveTask<BigDecimal> {
    private final List<String> cities;

    private final InventoryRepository inventoryRepository;

    public SumInventoryTask(List<String> cities,
                            InventoryRepository inventoryRepository) {
        this.cities = cities;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    protected BigDecimal compute() {
        BigDecimal total = BigDecimal.ZERO;

        if (cities.size() == 1) {
            total = inventoryRepository.sumInventoryByCountry(cities.getFirst());
        } else {
            List<SumInventoryTask> subTasks = new ArrayList<>();

            cities.forEach(city -> subTasks.add(
                    new SumInventoryTask(List.of(city), inventoryRepository)));

            subTasks.forEach(ForkJoinTask::fork);

            for (SumInventoryTask sumInventoryTask : subTasks) {
                total = total.add(sumInventoryTask.join());
            }
        }

        return total;
    }
}
