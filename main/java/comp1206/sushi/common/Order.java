package comp1206.sushi.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import comp1206.sushi.common.Order;
import comp1206.sushi.server.GenericHelp;

public class Order extends Model implements Serializable {

	private String status;
	private Map<Dish,Number> order;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User user;

	private String name;

	
	public Order() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		this.name = dtf.format(now);
		this.order = new HashMap<Dish, Number>();
	}

	public Number getDistance() {
		return 1;
	}



	@Override
	public String getName() {
		return this.name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

	public void addDishToOrder(Dish dish, Integer quantity){
		order.put(dish,quantity);
	}

	public void setOrder(Map<Dish, Number> basket){
		order = basket;
	}

	public Map<Dish, Number> getOrder() {
		return order;
	}
}
