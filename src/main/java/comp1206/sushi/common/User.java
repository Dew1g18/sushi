package comp1206.sushi.common;

import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User extends Model implements Serializable {
	
	private String name;
	private String password;
	private String address;
	private Postcode postcode;
	private List<Order> orderHistory;
	private Map<Dish, Number> basket;

	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
		this.basket = new HashMap<>();
		this.orderHistory = new ArrayList<>();
	}

	public void setOrderHistory(List<Order> history){
		this.orderHistory = history;
	}

	public void addOrder(Order order){
		if(!order.getStatus().equals("REGISTER_USER")) {
			orderHistory.add(order);
		}
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}

	public Map<Dish, Number> getBasket() {
		return basket;
	}

	public void setBasket(Map<Dish, Number> basket) {
		this.basket = basket;
	}

	public List<Order> getOrderHistory(){
		return orderHistory;
	}

	public  Order makeOrder() {
		Order order = new Order();
		order.setOrder(getBasket());
		order.setUser(this);
		order.setStatus("PENDING");
		orderHistory.add(order);
		return order;
	}
}
