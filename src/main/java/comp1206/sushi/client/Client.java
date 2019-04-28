package comp1206.sushi.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.*;
import comp1206.sushi.server.GenericHelp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client implements ClientInterface{

    private static final Logger logger = LogManager.getLogger("Client");

    public Restaurant restaurant;
	public User currentUser = null;


	public List<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	public List<Dish> dishes = new ArrayList<Dish>();
	public List<Order> orders = new ArrayList<Order>();
	public List<User> users = new ArrayList<User>();
	public List<Order> updatedOrders = new ArrayList<>();
	private UpdateRecieveSocket updater;
	boolean objectCreated=false;
	GenericHelp gh = new GenericHelp();
	boolean thereIsListener;

	public Client() {
        logger.info("Starting up client...");

        thereIsListener=false;
        updater = new UpdateRecieveSocket(this);
        updater.start();
        writeUpdateToClient();
        recievePeriodicUpdate();

		System.out.println("Connected to server and received config");

//		pagePeriodicUpdate();
		/**
		 * This breaks things for some reason
		 * I am so fucking lost with how to do this right now.
		 */
		objectCreated=true;

	}


	public void writeUpdateToClient(){
		try {
			updater.join();
			updatedOrders=updater.orders;
			if (this.restaurant != updater.restaurant) {
				this.restaurant = updater.restaurant;
			}
			synchronized (postcodes) {
				if (this.postcodes != updater.postcodes) {
					for (Postcode postcode : updater.postcodes) {
						if (gh.ifInList(postcodes, postcode.getName()) == null) {
							postcodes.add(postcode);
						}
					}
					ArrayList<Postcode> postcodesToRemove = new ArrayList<>();
					for (Postcode postcode : postcodes) {
						if (gh.ifInList(updater.postcodes, postcode.getName()) == null) {
							postcodesToRemove.add(postcode);
						}
					}
					postcodes.removeAll(postcodesToRemove);
//				this.postcodes = updater.postcodes;
				}
			}
			synchronized (dishes) {
				if (this.dishes != updater.dishes) {
					for (Dish dish : updater.dishes) {
						if (gh.ifInList(dishes, dish.getName()) == null) {
							dishes.add(dish);
						}else{
							updater.dishChanges(gh.ifInList(dishes, dish.getName()), dish);
						}
					}
					ArrayList<Dish> dishesToRemove = new ArrayList<>();
					for (Dish dish : dishes) {
						if (gh.ifInList(updater.dishes, dish.getName()) == null) {
							dishesToRemove.add(dish);
						}
					}dishes.removeAll(dishesToRemove);

//				this.dishes = updater.dishes;
				}
			}
			synchronized (users) {
				if (this.users != updater.users) {
					this.users = updater.users;
					if (currentUser!=null&&gh.ifInList(updater.users,currentUser.getName())==null ){
						this.users.add(currentUser);
					}
					else if (gh.ifInList(updater.users,currentUser.getName())!=null){
						Map<Dish, Number> basket = currentUser.getBasket();
						users.remove(gh.ifInList(updater.users,currentUser.getName()));
						users.add(currentUser);

//						int place = updater.users.indexOf(gh.ifInList(updater.users,currentUser.getName()));

					}

//					for (User user : updater.users) {
//						if (gh.ifInList(users, user.getName()) == null) {
//							users.add(user);
//						}
//					}
//					for (User user : users) {
//						if (gh.ifInList(updater.users, user.getName()) == null) {
//							users.remove(user);
//						}
//					}
				}
			}
			if (currentUser!=null) {
				for (Order order :currentUser.getOrderHistory()) {
					Order localOrder = gh.ifInList(orders, order.getName());
					if(localOrder!=null){
						orders.set(orders.indexOf(localOrder),order);
					}
				}
			}
			notifyUpdate();
		}catch(Exception e){
//			e.printStackTrace();
		}
	}

	public void updateUpdater(){
		updater.getUpdate();
	}


	//Remove previous couple methods
	@Override
	public Restaurant getRestaurant() {
		return this.restaurant;
		//TODO do this properly because this has no connection with real data, don't know why we're doing this before we actually have
		//some infrastructure
	}
	
	@Override
	public String getRestaurantName() {
		return restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return restaurant.getLocation();
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
		User newUser = new User(username,password,address,postcode);
		this.currentUser = newUser;
		users.add(newUser);
		//TODO have this send the new user to the server.
//		notifyUpdate();
		return newUser;
	}

	@Override
	public User login(String username, String password) {
		// TODO Here I want to be using the socket to check if stuffs ok
		// TODO Auto-generated method stub
		User kill= null;
		User newUser = null;
		for (User user: getUsers()){
			if (user.getName().equals(username)&&user.getPassword().equals(password)){
				user.setBasket(new HashMap<>());
				kill=user;
				newUser = new User(username, password,user.getAddress(), user.getPostcode());
				currentUser=newUser;
//  				currentUser = user;
// 				return user;
			}
		}
		if (kill!=null){
			users.remove(kill);
		}
		if (newUser!=null){
			users.add(newUser);
		}
//		notifyUpdate();
		return currentUser;
	}

	public List<User> getUsers(){
		return users;
	}

	@Override
	public List<Postcode> getPostcodes() {
//		System.out.println("Got postcodes");
		// TODO Auto-generated method stub
		return postcodes;
	}

	@Override
	public List<Dish> getDishes() {
		// TODO Auto-generated method stub
		return dishes;
	}

	@Override
	public String getDishDescription(Dish dish) {
		// TODO Auto-generated method stub
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
		// TODO Auto-generated method stub
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
		// TODO Auto-generated method stub
		checkBasketExists(user);
		return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) {
		// TODO Auto-generated method stub
//		checkBasketExists(user);
//		this.currentUser = user;
		Double cost = 0.0;
		for (Dish dish : dishes){
			if(getBasket(user).get(dish)!=null) {
				cost += getBasket(user).get(dish).doubleValue() * dish.getPrice().doubleValue();
			}
		}
//		notifyUpdate();
		return cost;
	}


	public void checkBasketExists(User user){
		HashMap<String, Dish> baskDishes = new HashMap<>();
		for (Dish dish : user.getBasket().keySet()){
			baskDishes.put(dish.getName(),dish);
		}
		for (Map.Entry<String , Dish> entry: baskDishes.entrySet()){
			if(gh.ifInList(dishes,entry.getKey())==null){
				user.getBasket().remove(entry.getValue());
			}
		}
//		for (Map.Entry<Dish, Number> entry : user.getBasket().entrySet())
//			Dish dish = entry.getKey();
//			if(gh.ifInList(dishes,dish.getName())==null){
//				user.getBasket().remove(dish);
//			}
//		}
 	}


	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
		// TODO Auto-generated method stub
		if (quantity.intValue()!=0) {
			getBasket(user).put(dish, quantity);
		}else{
			getBasket(user).remove(dish);
		}

	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
		// TODO Auto-generated method stub
//		addDishToBasket(user,dish,quantity);
//		this.currentUser=user;
		System.out.println(dish.getName());
		Dish dishFromName = gh.ifInList(dishes, dish.getName());
		if (dishFromName==null){
			quantity=0;
		}
		if (quantity.equals(0)){
			getBasket(user).remove(dish);
		}else{
			if (getBasket(user).keySet().contains(dishFromName)){
				getBasket(user).replace(dishFromName,quantity);
			}else {
				addDishToBasket(user, dishFromName ,quantity);
			}
		}
		notifyUpdate();
	}

	@Override
	public Order checkoutBasket(User user) {
		// TODO Auto-generated method stub
//		notifyUpdate();
		Order order = user.makeOrder();
		updater.sendOrder(order);
		clearBasket(user);
		return order;
	}

	@Override
	public void clearBasket(User user) {
		// TODO Auto-generated method stub
		user.setBasket(new HashMap<>());
//		notifyUpdate();
	}

	@Override
	public List<Order> getOrders(User user) {
		// TODO Auto-generated method stub
		try{
			User thisUser = gh.ifInList(users, user.getName());
			return  thisUser.getOrderHistory();
		}catch(NullPointerException e){
			//pass
		}
		return user.getOrderHistory();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		// TODO Auto-generated method stub
		if (order.getStatus().equals("Complete")){
			return true;
		}else {
			return false;
		}
	}

	@Override
	public String getOrderStatus(Order order) {
		// TODO Auto-generated method stub
		try {
			Order orderfromlist = gh.ifInList(updatedOrders, order.getName());
			return orderfromlist.getStatus();
		}catch(NullPointerException e) {
		}
		return order.getStatus();
	}

	@Override
	public Number getOrderCost(Order order) {
		// TODO Auto-generated method stub
		Double cost = 0.0;
		for (Map.Entry<Dish, Number> dish : order.getOrder().entrySet()){
			cost+=dish.getValue().doubleValue()*dish.getKey().getPrice().doubleValue();
		}
//		notifyUpdate();
		return cost;
	}

	@Override
	public void cancelOrder(Order order) {
		// TODO Auto-generated method stub
		order.setStatus("Request cancel");
		updater.sendOrder(order);
//		notifyUpdate();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		// TODO Auto-generated method stub

		System.out.println("Update listener added");
		thereIsListener = true;
		listeners.add(listener);
	}

	public boolean isThereUser(){
		if (currentUser!=null){
			return true;
		}else return false;
	}

	@Override
	public void notifyUpdate() {
		// TODO Auto-generated method stub
		if(thereIsListener&&isThereUser()) {
			this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
		}
	}



	public void recievePeriodicUpdate(){
		Thread p_reciever = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					try {
						Thread.sleep(1000);
//						System.out.println("Receiver firing");
						updateUpdater();
						writeUpdateToClient();
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		});
		p_reciever.start();
	}

	public void pagePeriodicUpdate(){//deprecated
		Thread pageUpdater = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(10000);
						while (true) {
							Thread.sleep(3000);
							notifyUpdate();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		pageUpdater.start();
	}

}
