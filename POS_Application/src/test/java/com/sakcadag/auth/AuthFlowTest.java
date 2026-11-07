package com.sakcadag.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Inventory;
import com.sakcadag.entity.OrderStatus;
import com.sakcadag.entity.Product;
import com.sakcadag.entity.Role;
import com.sakcadag.entity.User;
import com.sakcadag.security.JwtService;
import com.sakcadag.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTest extends ApiTestSupport {

    private static final String PRODUCTS_URL = "/api/products/getAllProduct";
    private static final String REGISTER_URL = "/api/auth/register";

    @Autowired
    private JwtService jwtService;

    @Test
    void loginWithValidCredentialsReturnsTokenUsernameAndRoles() throws Exception {
        MvcResult result = postAs(null, "/api/auth/login", loginJson(adminUsername, adminPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(adminUsername))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = bodyOf(result).get("token").asText();
        assertThat(jwtService.parseUsername(token)).contains(adminUsername);
    }

    @Test
    void loginWithWrongPasswordReturns401WithErrorShape() throws Exception {
        postAs(null, "/api/auth/login", loginJson(adminUsername, "definitely-wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.errors.length()").value(0));
    }

    @Test
    void loginWithUnknownUserReturns401() throws Exception {
        postAs(null, "/api/auth/login", loginJson(uniqueName("nobody"), "some-password-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        getAs(null, PRODUCTS_URL)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void requestWithGarbageTokenReturns401() throws Exception {
        getAs("not.a.jwt", PRODUCTS_URL)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void requestWithTokenSignedByAnotherKeyReturns401() throws Exception {
        String validToken = adminToken();
        getAs(validToken, PRODUCTS_URL).andExpect(status().isOk());

        String foreignToken = new JwtService("another-signing-key-used-only-by-this-test-1", 60)
                .generateToken(adminUsername);
        getAs(foreignToken, PRODUCTS_URL)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void requestWithExpiredTokenReturns401() throws Exception {
        String validToken = adminToken();
        getAs(validToken, PRODUCTS_URL).andExpect(status().isOk());

        String expiredToken = new JwtService(jwtSecret, -1).generateToken(adminUsername);
        getAs(expiredToken, PRODUCTS_URL)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void tokenOfDeletedUserReturns401() throws Exception {
        User cashier = createCashier();
        String token = cashierToken(cashier);
        getAs(token, PRODUCTS_URL).andExpect(status().isOk());

        userRepository.deleteById(cashier.getId());

        getAs(token, PRODUCTS_URL).andExpect(status().isUnauthorized());
        assertThat(userRepository.findByUsername(cashier.getUsername())).isEmpty();
    }

    @Test
    void cashierCannotCreateProduct() throws Exception {
        String token = cashierToken(createCashier());
        String productName = uniqueName("forbidden-product");
        long categoryId = createCategory().getId();

        postAs(token, "/api/products/new-product",
                json(Map.of("name", productName, "price", 10.00, "category", Map.of("id", categoryId))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));

        assertThat(productRepository.findAll()).noneMatch(product -> productName.equals(product.getName()));
    }

    @Test
    void cashierCannotListUsers() throws Exception {
        String token = cashierToken(createCashier());

        getAs(token, "/api/users/getAllUsers")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void cashierCannotRegisterUsers() throws Exception {
        String token = cashierToken(createCashier());
        String newUsername = uniqueName("forbidden-user");

        postAs(token, REGISTER_URL, json(Map.of("username", newUsername, "password", "valid-password-123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access is denied"));

        assertThat(userRepository.findByUsername(newUsername)).isEmpty();
    }

    @Test
    void cashierCannotDeleteCustomer() throws Exception {
        String token = cashierToken(createCashier());
        long customerId = createCustomer().getId();

        deleteAs(token, "/api/customers/delCustomer/" + customerId)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access is denied"));

        assertThat(customerRepository.existsById(customerId)).isTrue();
    }

    @Test
    void cashierCanReadProductsAndPlaceOrder() throws Exception {
        Product product = createProduct("12.50");
        Inventory inventory = createInventory(product, 4);
        Customer customer = createCustomer();
        String token = cashierToken(createCashier());

        MvcResult listing = getAs(token, PRODUCTS_URL)
                .andExpect(status().isOk())
                .andReturn();
        assertThat(textValues(bodyOf(listing), "name")).contains(product.getName());

        MvcResult order = postAs(token, "/api/orders/new-order",
                json(orderBody(customer.getId(), List.of(line(product.getId(), 2)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        Long orderId = bodyOf(order).get("id").asLong();
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.OPEN);
        assertThat(stockOf(inventory)).isEqualTo(2);
    }

    @Test
    void registerWithoutTokenReturns401() throws Exception {
        String newUsername = uniqueName("anon-user");

        postAs(null, REGISTER_URL, json(Map.of("username", newUsername, "password", "valid-password-123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));

        assertThat(userRepository.findByUsername(newUsername)).isEmpty();
    }

    @Test
    void adminRegistersCashierWithHashedPassword() throws Exception {
        String username = uniqueName("new-cashier");
        String password = "new-cashier-pass-1";

        MvcResult result = postAs(adminToken(), REGISTER_URL,
                json(Map.of("username", username, "password", password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.roles[0].name").value("CASHIER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        JsonNode body = bodyOf(result);
        assertThat(body.has("password")).isFalse();

        User stored = userRepository.findByUsername(username).orElseThrow();
        assertThat(stored.getPassword()).startsWith("$2").isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, stored.getPassword())).isTrue();
        assertThat(stored.getRoles()).extracting(Role::getName).containsExactly("CASHIER");

        assertThat(login(username, password)).isNotBlank();
    }

    @Test
    void registerDuplicateUsernameReturns409() throws Exception {
        User existing = createCashier();

        postAs(adminToken(), REGISTER_URL,
                json(Map.of("username", existing.getUsername(), "password", "another-password-1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void registerPasswordOf73CharactersReturns400() throws Exception {
        String username = uniqueName("long-pass");

        postAs(adminToken(), REGISTER_URL,
                json(Map.of("username", username, "password", "a".repeat(73))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value(startsWith("password:")));

        assertThat(userRepository.findByUsername(username)).isEmpty();
    }

    @Test
    void registerPasswordOver72BytesButUnder72CharactersReturns400() throws Exception {
        String username = uniqueName("utf8-pass");
        // 37 characters pass the DTO size check but encode to 74 UTF-8 bytes, beyond BCrypt's 72-byte limit.
        String password = "ü".repeat(37);

        postAs(adminToken(), REGISTER_URL,
                json(Map.of("username", username, "password", password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password is too long"));

        assertThat(userRepository.findByUsername(username)).isEmpty();
    }

    @Test
    void adminCannotDeleteOwnAccount() throws Exception {
        long adminId = userRepository.findByUsername(adminUsername).orElseThrow().getId();

        deleteAs(adminToken(), "/api/users/delUser/" + adminId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You cannot delete your own account"));

        assertThat(userRepository.existsById(adminId)).isTrue();
    }
}
