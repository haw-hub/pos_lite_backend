package com.pos;

import com.pos.entity.Order;
import com.pos.entity.Product;
import com.pos.entity.User;
import com.pos.entity.Shop;
import com.pos.enums.OrderStatus;
import com.pos.enums.PaymentMethod;
import com.pos.repository.OrderRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import com.pos.repository.ShopRepository;
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
	@Autowired
	private ShopRepository shopRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void storeDataIsIsolatedByShopAndSharedByEmployees() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		Shop firstShop = createShop("Store A " + suffix);
		Shop secondShop = createShop("Store B " + suffix);
		User admin = createUser("store_a_admin_" + suffix, firstShop);
		User cashier = createUser("store_a_cashier_" + suffix, firstShop);
		User otherShopUser = createUser("store_b_" + suffix, secondShop);

		Product product = new Product();
		product.setOwner(admin);
		product.setShop(firstShop);
		product.setName("Private product");
		product.setPrice(BigDecimal.TEN);
		product.setStock(5);
		productRepository.save(product);

		Order order = new Order();
		order.setCashier(cashier);
		order.setShop(firstShop);
		order.setPaymentMethod(PaymentMethod.CASH);
		order.setStatus(OrderStatus.COMPLETED);
		order.setSubtotal(BigDecimal.TEN);
		order.setTax(BigDecimal.ZERO);
		order.setTotalAmount(BigDecimal.TEN);
		orderRepository.save(order);

		assertThat(productRepository.findByShopIdAndDeletedFalse(admin.getShop().getId())).hasSize(1);
		assertThat(productRepository.findByShopIdAndDeletedFalse(cashier.getShop().getId())).hasSize(1);
		assertThat(productRepository.findByShopIdAndDeletedFalse(otherShopUser.getShop().getId())).isEmpty();
		assertThat(orderRepository.findByShopId(admin.getShop().getId())).hasSize(1);
		assertThat(orderRepository.findByShopId(cashier.getShop().getId())).hasSize(1);
		assertThat(orderRepository.findByShopId(otherShopUser.getShop().getId())).isEmpty();
	}

	private Shop createShop(String name) {
		Shop shop = new Shop();
		shop.setName(name);
		return shopRepository.save(shop);
	}

	private User createUser(String username, Shop shop) {
		User user = new User();
		user.setUsername(username);
		user.setPassword("test-password");
		user.setFullName(username);
		user.setActive(true);
		user.setShop(shop);
		return userRepository.save(user);
	}
}
