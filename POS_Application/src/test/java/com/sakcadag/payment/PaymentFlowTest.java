package com.sakcadag.payment;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Inventory;
import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderStatus;
import com.sakcadag.entity.Payment;
import com.sakcadag.entity.Product;
import com.sakcadag.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentFlowTest extends ApiTestSupport {

    private static final String PAYMENT_URL = "/api/payments/new-payment";

    @Test
    void exactAmountSetsOrderPaidAndStoresPayment() throws Exception {
        Product product = createProduct("19.99");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 3);

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", new BigDecimal("59.97"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(payment.getAmount()).isEqualByComparingTo("59.97");
    }

    @Test
    void amountOf125AcceptedForTotalOf125_00() throws Exception {
        Product product = createProduct("125.00");
        createInventory(product, 2);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 1);

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", 125)))
                .andExpect(status().isCreated());

        assertThat(orderRepository.findById(orderId).orElseThrow().getTotalAmount())
                .isEqualByComparingTo("125.00");
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(paymentRepository.findByOrderId(orderId).orElseThrow().getAmount())
                .isEqualByComparingTo("125");
    }

    @Test
    void wrongAmountReturns409AndOrderStaysOpen() throws Exception {
        Product product = createProduct("19.99");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 3);

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", new BigDecimal("59.98"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Payment must equal the order total of 59.97"));

        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.OPEN);
        assertThat(paymentRepository.findByOrderId(orderId)).isEmpty();
    }

    @Test
    void secondPaymentForSameOrderReturns409() throws Exception {
        Product product = createProduct("19.99");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 3);

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", new BigDecimal("59.97"))))
                .andExpect(status().isCreated());

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", new BigDecimal("59.97"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Order " + orderId + " is not open for payment"));

        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(paymentRepository.findByOrderId(orderId).orElseThrow().getAmount())
                .isEqualByComparingTo("59.97");
    }

    @Test
    void paymentForCancelledOrderReturns409() throws Exception {
        Product product = createProduct("19.99");
        Inventory inventory = createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 3);

        postNoBodyAs(token, "/api/orders/" + orderId + "/cancel").andExpect(status().isOk());

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", new BigDecimal("59.97"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Order " + orderId + " is not open for payment"));

        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(paymentRepository.findByOrderId(orderId)).isEmpty();
        assertThat(stockOf(inventory)).isEqualTo(10);
    }

    @Test
    void paymentForUnknownOrderReturns404() throws Exception {
        Product product = createProduct("19.99");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 1);
        orderRepository.deleteById(orderId);

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", new BigDecimal("19.99"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with id " + orderId + " not found"));

        assertThat(paymentRepository.findByOrderId(orderId)).isEmpty();
    }

    @Test
    void negativeAmountReturns400AndOrderStaysOpen() throws Exception {
        Product product = createProduct("19.99");
        createInventory(product, 10);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());
        Long orderId = placeOrderAndGetId(token, customer.getId(), product.getId(), 1);

        postAs(token, PAYMENT_URL, json(Map.of("orderId", orderId, "amount", new BigDecimal("-1.00"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("amount: must be greater than 0"));

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.OPEN);
        assertThat(paymentRepository.findByOrderId(orderId)).isEmpty();
    }
}
