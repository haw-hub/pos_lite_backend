package com.pos;

import com.pos.entity.Order;
import com.pos.entity.Product;
import com.pos.entity.User;
import com.pos.enums.OrderStatus;
import com.pos.enums.PaymentMethod;
import com.pos.repository.OrderRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private OrderRepository orderRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void storeDataIsIsolatedByUser() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		User first = createUser("store_a_" + suffix);
		User second = createUser("store_b_" + suffix);

		Product product = new Product();
		product.setOwner(first);
		product.setName("Private product");
		product.setPrice(BigDecimal.TEN);
		product.setStock(5);
		productRepository.save(product);

		Order order = new Order();
		order.setCashier(first);
		order.setPaymentMethod(PaymentMethod.CASH);
		order.setStatus(OrderStatus.COMPLETED);
		order.setSubtotal(BigDecimal.TEN);
		order.setTax(BigDecimal.ZERO);
		order.setTotalAmount(BigDecimal.TEN);
		orderRepository.save(order);

		assertThat(productRepository.findByOwnerUsernameAndDeletedFalse(first.getUsername())).hasSize(1);
		assertThat(productRepository.findByOwnerUsernameAndDeletedFalse(second.getUsername())).isEmpty();
		assertThat(orderRepository.findByCashierUsername(first.getUsername())).hasSize(1);
		assertThat(orderRepository.findByCashierUsername(second.getUsername())).isEmpty();
	}

	private User createUser(String username) {
		User user = new User();
		user.setUsername(username);
		user.setPassword("test-password");
		user.setFullName(username);
		user.setActive(true);
		return userRepository.save(user);
	}
}
