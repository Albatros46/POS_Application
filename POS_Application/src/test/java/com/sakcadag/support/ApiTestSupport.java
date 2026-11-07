package com.sakcadag.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakcadag.entity.Category;
import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Inventory;
import com.sakcadag.entity.Product;
import com.sakcadag.entity.User;
import com.sakcadag.respository.CategoryRepository;
import com.sakcadag.respository.CustomerRepository;
import com.sakcadag.respository.InventoryRepository;
import com.sakcadag.respository.OrderItemRepository;
import com.sakcadag.respository.OrderRepository;
import com.sakcadag.respository.PaymentRepository;
import com.sakcadag.respository.ProductRepository;
import com.sakcadag.respository.RoleRepository;
import com.sakcadag.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared helpers for the HTTP-level tests. Every helper goes through MockMvc and the real
 * security filter chain; test data is created through the repositories with unique names,
 * so each test is independent of the others.
 */
public abstract class ApiTestSupport {

    public static final String CASHIER_PASSWORD = "cashier-password-1";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected InventoryRepository inventoryRepository;
    @Autowired
    protected CustomerRepository customerRepository;
    @Autowired
    protected OrderRepository orderRepository;
    @Autowired
    protected OrderItemRepository orderItemRepository;
    @Autowired
    protected PaymentRepository paymentRepository;

    @Value("${app.admin.username}")
    protected String adminUsername;
    @Value("${app.admin.password}")
    protected String adminPassword;
    @Value("${app.jwt.secret}")
    protected String jwtSecret;

    protected static String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 12);
    }

    protected String json(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    protected String loginJson(String username, String password) throws JsonProcessingException {
        return json(Map.of("username", username, "password", password));
    }

    protected static Map<String, Object> line(Long productId, int quantity) {
        return Map.of("productId", productId, "quantity", quantity);
    }

    protected static Map<String, Object> orderBody(Long customerId, List<Map<String, Object>> items) {
        return Map.of("customerId", customerId, "items", items);
    }

    protected ResultActions perform(MockHttpServletRequestBuilder request, String token) throws Exception {
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    protected ResultActions getAs(String token, String url) throws Exception {
        return perform(MockMvcRequestBuilders.get(url), token);
    }

    protected ResultActions postAs(String token, String url, String jsonBody) throws Exception {
        return perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody), token);
    }

    protected ResultActions postNoBodyAs(String token, String url) throws Exception {
        return perform(MockMvcRequestBuilders.post(url), token);
    }

    protected ResultActions deleteAs(String token, String url) throws Exception {
        return perform(MockMvcRequestBuilders.delete(url), token);
    }

    protected JsonNode bodyOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected static List<String> textValues(JsonNode array, String field) {
        List<String> values = new ArrayList<>();
        for (JsonNode element : array) {
            values.add(element.get(field).asText());
        }
        return values;
    }

    protected String login(String username, String password) throws Exception {
        MvcResult result = postAs(null, "/api/auth/login", loginJson(username, password))
                .andExpect(status().isOk())
                .andReturn();
        return bodyOf(result).get("token").asText();
    }

    protected String adminToken() throws Exception {
        return login(adminUsername, adminPassword);
    }

    protected User createCashier() {
        User user = new User();
        user.setUsername(uniqueName("cashier"));
        user.setPassword(passwordEncoder.encode(CASHIER_PASSWORD));
        user.setRoles(new HashSet<>(Set.of(roleRepository.findByName("CASHIER").orElseThrow())));
        return userRepository.save(user);
    }

    protected String cashierToken(User cashier) throws Exception {
        return login(cashier.getUsername(), CASHIER_PASSWORD);
    }

    protected Category createCategory() {
        return categoryRepository.save(new Category(uniqueName("category")));
    }

    protected Product createProduct(String price) {
        Category category = createCategory();
        return productRepository.save(new Product(uniqueName("product"), new BigDecimal(price), category));
    }

    protected Inventory createInventory(Product product, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setStockQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    protected int stockOf(Inventory inventory) {
        return inventoryRepository.findById(inventory.getId()).orElseThrow().getStockQuantity();
    }

    protected Customer createCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName(uniqueName("customer"));
        return customerRepository.save(customer);
    }

    protected Long placeOrderAndGetId(String token, Long customerId, Long productId, int quantity) throws Exception {
        MvcResult result = postAs(token, "/api/orders/new-order",
                json(orderBody(customerId, List.of(line(productId, quantity)))))
                .andExpect(status().isCreated())
                .andReturn();
        return bodyOf(result).get("id").asLong();
    }
}
