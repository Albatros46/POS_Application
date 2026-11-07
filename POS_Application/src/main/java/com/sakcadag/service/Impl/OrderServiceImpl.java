package com.sakcadag.service.Impl;

import com.sakcadag.dto.CreateOrderRequest;
import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Inventory;
import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderItem;
import com.sakcadag.entity.OrderStatus;
import com.sakcadag.entity.Product;
import com.sakcadag.exception.BusinessRuleException;
import com.sakcadag.exception.InvalidRequestException;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.CustomerRepository;
import com.sakcadag.respository.InventoryRepository;
import com.sakcadag.respository.OrderItemRepository;
import com.sakcadag.respository.OrderRepository;
import com.sakcadag.respository.ProductRepository;
import com.sakcadag.service.interfaces.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    // Lines are processed in ascending product id order so concurrent orders take inventory locks in the same sequence and cannot deadlock.
    @Override
    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        Map<Long, Integer> quantities = mergeLines(request.items());

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.OPEN);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> line : quantities.entrySet()) {
            Long productId = line.getKey();
            int quantity = line.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new BusinessRuleException("No stock record for product " + productId));
            if (inventory.getStockQuantity() < quantity) {
                throw new BusinessRuleException("Not enough stock for product " + productId + ": "
                        + inventory.getStockQuantity() + " available");
            }
            inventory.setStockQuantity(inventory.getStockQuantity() - quantity);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            order.getOrderItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long id) {
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessRuleException("Only OPEN orders can be cancelled; order " + id + " is " + order.getStatus());
        }

        List<OrderItem> items = new ArrayList<>(order.getOrderItems());
        items.sort(Comparator.comparing((OrderItem item) -> item.getProduct().getId()));
        for (OrderItem item : items) {
            Long productId = item.getProduct().getId();
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new BusinessRuleException("No stock record for product " + productId));
            inventory.setStockQuantity(inventory.getStockQuantity() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    // **Müşteriye göre siparişleri listeleme**
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // **Belirli bir siparişin içindeki sipariş kalemlerini listeleme**
    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // addExact turns a quantity overflow into an explicit error instead of a silently wrapped value.
    private Map<Long, Integer> mergeLines(List<CreateOrderRequest.Line> lines) {
        Map<Long, Integer> merged = new TreeMap<>();
        for (CreateOrderRequest.Line line : lines) {
            try {
                merged.merge(line.productId(), line.quantity(), Math::addExact);
            } catch (ArithmeticException e) {
                throw new InvalidRequestException("Quantity for product " + line.productId() + " is too large");
            }
        }
        return merged;
    }
}
