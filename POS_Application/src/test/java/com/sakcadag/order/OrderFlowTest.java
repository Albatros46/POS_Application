package com.sakcadag.order;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Inventory;
import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderItem;
import com.sakcadag.entity.OrderStatus;
import com.sakcadag.entity.Product;
import com.sakcadag.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderFlowTest extends ApiTestSupport {

    private static final String ORDER_URL = "/api/orders/new-order";

    @Test
    void orderDecrementsStockAndStoresUnitPriceAndOpenStatus() throws Exception {
        Product product = createProduct("19.99");
        Inventory inventory = createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        MvcResult result = postAs(token, ORDER_URL,
                json(orderBody(customer.getId(), List.of(line(product.getId(), 3)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();
        Long orderId = bodyOf(result).get("id").asLong();

        assertThat(stockOf(inventory)).isEqualTo(7);

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.OPEN);
        assertThat(order.getCustomer().getId()).isEqualTo(customer.getId());

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(3);
        assertThat(items.get(0).getUnitPrice()).isEqualByComparingTo("19.99");
    }

    @Test
    void laterPriceChangeLeavesExistingOrderUnchanged() throws Exception {
        Product product = createProduct("19.99");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        Long oldOrderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 2);

        Product stored = productRepository.findById(product.getId()).orElseThrow();
        stored.setPrice(new BigDecimal("50.00"));
        productRepository.save(stored);

        Long newOrderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 1);

        Order oldOrder = orderRepository.findById(oldOrderId).orElseThrow();
        assertThat(oldOrder.getTotalAmount()).isEqualByComparingTo("39.98");
        List<OrderItem> oldItems = orderItemRepository.findByOrderId(oldOrderId);
        assertThat(oldItems).hasSize(1);
        assertThat(oldItems.get(0).getUnitPrice()).isEqualByComparingTo("19.99");

        Order newOrder = orderRepository.findById(newOrderId).orElseThrow();
        assertThat(newOrder.getTotalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void twoLinesForSameProductMergeIntoOneItem() throws Exception {
        Product product = createProduct("19.99");
        Inventory inventory = createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        Long orderId = bodyOf(postAs(token, ORDER_URL,
                json(orderBody(customer.getId(), List.of(line(product.getId(), 2), line(product.getId(), 1)))))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(3);
        assertThat(stockOf(inventory)).isEqualTo(7);
    }

    @Test
    void totalOf19_99Times3IsExactly59_97() throws Exception {
        Product product = createProduct("19.99");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 3);

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getTotalAmount().toPlainString()).isEqualTo("59.97");
    }

    @Test
    void insufficientStockOnSecondLineReturns409AndRollsBackFirstLine() throws Exception {
        Product first = createProduct("5.00");
        Inventory firstInventory = createInventory(first, 10);
        Product second = createProduct("7.00");
        Inventory secondInventory = createInventory(second, 1);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        // Lines are processed in ascending product id order, so `first` is the first line.
        assertThat(first.getId()).isLessThan(second.getId());

        postAs(token, ORDER_URL,
                json(orderBody(customer.getId(), List.of(line(first.getId(), 2), line(second.getId(), 5)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Not enough stock for product " + second.getId() + ": 1 available"));

        assertThat(stockOf(firstInventory)).isEqualTo(10);
        assertThat(stockOf(secondInventory)).isEqualTo(1);
        assertThat(orderRepository.findByCustomerId(customer.getId())).isEmpty();
    }

    @Test
    void productWithoutInventoryRowReturns409() throws Exception {
        Product product = createProduct("3.00");
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        postAs(token, ORDER_URL,
                json(orderBody(customer.getId(), List.of(line(product.getId(), 1)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No stock record for product " + product.getId()));

        assertThat(orderRepository.findByCustomerId(customer.getId())).isEmpty();
    }

    @Test
    void unknownProductReturns404() throws Exception {
        Product gone = createProduct("3.00");
        long missingProductId = gone.getId();
        productRepository.deleteById(missingProductId);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        postAs(token, ORDER_URL,
                json(orderBody(customer.getId(), List.of(line(missingProductId, 1)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id " + missingProductId + " not found"));

        assertThat(orderRepository.findByCustomerId(customer.getId())).isEmpty();
    }

    @Test
    void unknownCustomerReturns404() throws Exception {
        Product product = createProduct("3.00");
        createInventory(product, 5);
        Customer gone = createCustomer();
        long missingCustomerId = gone.getId();
        customerRepository.deleteById(missingCustomerId);
        String token = cashierToken(createCashier());

        postAs(token, ORDER_URL,
                json(orderBody(missingCustomerId, List.of(line(product.getId(), 1)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer with id " + missingCustomerId + " not found"));
    }

    @Test
    void emptyItemsReturns400() throws Exception {
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        postAs(token, ORDER_URL,
                json(orderBody(customer.getId(), List.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("items: must not be empty"));

        assertThat(orderRepository.findByCustomerId(customer.getId())).isEmpty();
    }

    @Test
    void zeroQuantityReturns400() throws Exception {
        Product product = createProduct("3.00");
        createInventory(product, 5);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        postAs(token, ORDER_URL,
                json(orderBody(customer.getId(), List.of(line(product.getId(), 0)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("items[0].quantity: must be greater than 0"));

        assertThat(orderRepository.findByCustomerId(customer.getId())).isEmpty();
    }

    @Test
    void itemsContainingNullReturn400() throws Exception {
        Product product = createProduct("3.00");
        createInventory(product, 5);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        String body = "{\"customerId\":" + customer.getId() + ",\"items\":[null,{\"productId\":"
                + product.getId() + ",\"quantity\":1}]}";

        postAs(token, ORDER_URL, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        assertThat(orderRepository.findByCustomerId(customer.getId())).isEmpty();
    }

    @Test
    void cancelRestoresStockAndSetsCancelled() throws Exception {
        Product product = createProduct("10.00");
        Inventory inventory = createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 4);
        assertThat(stockOf(inventory)).isEqualTo(6);

        postNoBodyAs(token, "/api/orders/" + orderId + "/cancel")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(stockOf(inventory)).isEqualTo(10);
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelTwiceReturns409AndDoesNotRestoreStockAgain() throws Exception {
        Product product = createProduct("10.00");
        Inventory inventory = createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 4);

        postNoBodyAs(token, "/api/orders/" + orderId + "/cancel").andExpect(status().isOk());

        postNoBodyAs(token, "/api/orders/" + orderId + "/cancel")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Only OPEN orders can be cancelled; order " + orderId + " is CANCELLED"));

        assertThat(stockOf(inventory)).isEqualTo(10);
    }

    @Test
    void cancelPaidOrderReturns409AndKeepsStock() throws Exception {
        Product product = createProduct("10.00");
        Inventory inventory = createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 2);

        postAs(token, "/api/payments/new-payment",
                json(Map.of("orderId", orderId, "amount", new BigDecimal("20.00"))))
                .andExpect(status().isCreated());

        postNoBodyAs(token, "/api/orders/" + orderId + "/cancel")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Only OPEN orders can be cancelled; order " + orderId + " is PAID"));

        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(stockOf(inventory)).isEqualTo(8);
    }

    @Test
    void cancelUnknownOrderReturns404() throws Exception {
        Product product = createProduct("10.00");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 1);
        orderRepository.deleteById(orderId);

        postNoBodyAs(token, "/api/orders/" + orderId + "/cancel")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with id " + orderId + " not found"));
    }
}
