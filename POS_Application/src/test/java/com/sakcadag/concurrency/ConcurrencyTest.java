package com.sakcadag.concurrency;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Inventory;
import com.sakcadag.entity.Product;
import com.sakcadag.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrencyTest extends ApiTestSupport {

    private static final String ORDER_URL = "/api/orders/new-order";
    private static final int THREADS = 8;

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    void eightConcurrentOrdersForFiveUnitsSellExactlyFive() throws Exception {
        Product product = createProduct("1.00");
        Inventory inventory = createInventory(product, 5);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        String body = json(orderBody(customer.getId(), List.of(line(product.getId(), 1))));

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                Callable<String> attempt = () -> {
                    ready.countDown();
                    if (!start.await(30, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("start signal was not released");
                    }
                    MvcResult result = postAs(token, ORDER_URL, body).andReturn();
                    return result.getResponse().getStatus() + "|" + result.getResponse().getContentAsString();
                };
                futures.add(pool.submit(attempt));
            }

            assertThat(ready.await(30, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<String> outcomes = new ArrayList<>();
            for (Future<String> future : futures) {
                outcomes.add(future.get(60, TimeUnit.SECONDS));
            }

            long created = outcomes.stream().filter(o -> o.startsWith("201|")).count();
            long rejected = outcomes.stream()
                    .filter(o -> o.startsWith("409|") && o.contains("Not enough stock for product " + product.getId()))
                    .count();
            assertThat(created).isEqualTo(5);
            assertThat(rejected).isEqualTo(3);
            assertThat(outcomes).hasSize(THREADS);
        } finally {
            pool.shutdownNow();
        }

        assertThat(stockOf(inventory)).isEqualTo(0);
        assertThat(orderRepository.findByCustomerId(customer.getId())).hasSize(5);
    }
}
