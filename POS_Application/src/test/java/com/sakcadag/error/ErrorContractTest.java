package com.sakcadag.error;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Product;
import com.sakcadag.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorContractTest extends ApiTestSupport {

    private static final String NEW_PRODUCT_URL = "/api/products/new-product";

    @Test
    void missingProductReturns404WithErrorContract() throws Exception {
        Product product = createProduct("4.00");
        long missingId = product.getId();
        productRepository.deleteById(missingId);
        String path = "/api/products/getByIdProduct/" + missingId;

        getAs(adminToken(), path)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product with id " + missingId + " not found"))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        postAs(adminToken(), NEW_PRODUCT_URL, "{\"name\": ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"))
                .andExpect(jsonPath("$.path").value(NEW_PRODUCT_URL));
    }

    @Test
    void validationErrorListsFieldInErrors() throws Exception {
        long categoryId = createCategory().getId();
        String name = uniqueName("blank-name-product");

        postAs(adminToken(), NEW_PRODUCT_URL,
                json(Map.of("name", "", "price", 4.00, "category", Map.of("id", categoryId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").value(hasItem("name: Product name is required")));

        assertThat(productRepository.findAll()).noneMatch(product -> name.equals(product.getName()));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        getAs(adminToken(), "/api/products/getByIdProduct/abc")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'id' has invalid value 'abc'"));
    }

    @Test
    void productWithNullCategoryIdReturns400() throws Exception {
        String name = uniqueName("no-category-product");
        String body = "{\"name\":\"" + name + "\",\"price\":4.00,\"category\":{\"id\":null}}";

        postAs(adminToken(), NEW_PRODUCT_URL, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category id is required"));

        assertThat(productRepository.findAll()).noneMatch(product -> name.equals(product.getName()));
    }

    @Test
    void deletingCustomerWithOrdersReturns409WithoutSqlDetails() throws Exception {
        Product product = createProduct("2.00");
        createInventory(product, 5);
        Customer customer = createCustomer();
        String token = adminToken();
        placeOrderAndGetId(token, customer.getId(), product.getId(), 1);
        String path = "/api/customers/delCustomer/" + customer.getId();

        MvcResult result = deleteAs(token, path)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("The request conflicts with existing data"))
                .andExpect(jsonPath("$.path").value(path))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).doesNotContainIgnoringCase("sql")
                .doesNotContainIgnoringCase("constraint")
                .doesNotContainIgnoringCase("foreign key")
                .doesNotContainIgnoringCase("jdbc");
        assertThat(customerRepository.existsById(customer.getId())).isTrue();
    }

    @Test
    void unknownApiPathWithValidTokenReturns404WithJsonShape() throws Exception {
        String path = "/api/no-such-endpoint";

        getAs(adminToken(), path)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value(path));
    }
}
