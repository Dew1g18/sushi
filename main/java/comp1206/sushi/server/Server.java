package comp1206.sushi.server;

import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JOptionPane;

import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
public class Server implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
	
	public Restaurant restaurant;
	public ArrayList<Dish> dishes = new ArrayList<Dish>();
	public ArrayList<Drone> drones = new ArrayList<Drone>();
	public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();

	public ArrayList<Order> orders = new ArrayList<Order>();

	public ArrayList<Staff> staff = new ArrayList<Staff>();
	public ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	private StockManager stockManager;
	public boolean restockDishEnabled=true;
	GenericHelp gh  = new GenericHelp();
	ThreadPoolExecutor staffPool;
	ThreadPoolExecutor dronePool;
	ThreadPoolExecutor pool;
	OrderHandler orderHandler;

	public Server() {
        logger.info("Starting up server...");
		stockManager= new StockManager(this);
//		this.staffPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);

		Postcode restaurantPostcode = new Postcode("SO17 1BJ");
		restaurant = new Restaurant("Mock Restaurant",restaurantPostcode);
		
		Postcode postcode1 = addPostcode("SO17 1TJ");
		Postcode postcode2 = addPostcode("SO17 1BX");
		Postcode postcode3 = addPostcode("SO17 2NJ");
		Postcode postcode4 = addPostcode("SO17 1TW");
		Postcode postcode5 = addPostcode("SO17 2LB");
		
		Supplier supplier1 = addSupplier("Supplier 1",postcode1);
		Supplier supplier2 = addSupplier("Supplier 2",postcode2);
		Supplier supplier3 = addSupplier("Supplier 3",postcode3);
		
		Ingredient ingredient1 = addIngredient("Ingredient 1","grams",supplier1,1,20,1);
		Ingredient ingredient2 = addIngredient("Ingredient 2","grams",supplier2,1,20,1);
		Ingredient ingredient3 = addIngredient("Ingredient 3","grams",supplier3,1,20,1);
		
		Dish dish1 = addDish("Dish 1","Dish 1",1,21,4);
		Dish dish2 = addDish("Dish 2","Dish 2",2,10,4);
		Dish dish3 = addDish("Dish 3","Dish 3",3,10,4);
		
//		orders.add(new Order());
		users.add(new User("dave","dave","neh",postcode1));
		users.add(new User("ha","","",postcode2));
		users.add(new User("as","","",postcode2));
		users.add(new User("qw","","",postcode2));

		addIngredientToDish(dish1,ingredient1,1);
		addIngredientToDish(dish1,ingredient2,2);
		addIngredientToDish(dish2,ingredient2,3);
		addIngredientToDish(dish2,ingredient3,1);
		addIngredientToDish(dish3,ingredient1,2);
		addIngredientToDish(dish3,ingredient3,1);
		
		addStaff("Staff 1");
		addStaff("Staff 2");
		addStaff("Staff 3");
		
		addDrone(1);
		addDrone(2);
		addDrone(3);

		orderHandler = new OrderHandler(this);
		startStuffAgain();

		stockManager.init1();
		stockManager.pauseDishChecking=false;
		DataServer ds = new DataServer(this);
		ds.start();
		Thread checkDataServer = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
//						System.out.println("in here");
						Thread.sleep(1000);
						updateOrders(ds.getGotOrders());
						ds.clearOrders();
					}catch(InterruptedException e){
						System.out.println("Interrupted");
					}
				}
			}
		});
		checkDataServer.start();

	}

	public void updateOrders(List<Order> updates){
		for (Order update : updates) {
			//todo first order cost = 0 why tho? <- has no basket, again why though?

//			System.out.println(update.getStatus());
//			System.out.println("Adding an order!!!");
			if (!update.getStatus().equals("REGISTER_USER")) {
				if (gh.ifInList(orders, update.getName()) == null) {//NewOrder
					update.setStatus("Received by server");
					System.out.println(update.getOrder());
					orders.add(update);
					User user = update.getUser();
					if (gh.ifInList(users, user.getName()) == null) {
						users.add(user);
					} else {
						int index = users.indexOf(gh.ifInList(users, user.getName()));
						users.set(index, user);
					}
				} else {
					int index = orders.indexOf(gh.ifInList(orders, update.getName()));
					User serverVersion = gh.ifInList(users, update.getUser().getName());
					update.setUser(serverVersion);
					serverVersion.addOrder(update);
					int indexOfUser = users.indexOf(serverVersion);
					users.set(indexOfUser, serverVersion);
					orders.set(index, update);
				}
			}else{
				User user = update.getUser();
				user.setOrderHistory(new ArrayList<>());
				users.add(user);
			}
		}
		notifyUpdate();

	}

	
	@Override
	public List<Dish> getDishes() {
		return this.dishes;
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		stockManager.pauseDishChecking=true;
		Dish newDish = new Dish(name,description,price,restockThreshold,restockAmount);
		newDish.setStockLevel(0);
		this.notifyUpdate();
		this.dishes.add(newDish);
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) throws UnableToDeleteException{
		if(gh.isDishUsed(orders,dish)){
//			System.out.println("But its used");
			throw new UnableToDeleteException("Can't delete that dish!!");
		}else {
			this.dishes.remove(dish);
			this.notifyUpdate();
		}
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
		List<Dish> dishes = getDishes();
		HashMap<Dish, Number> levels = new HashMap<Dish, Number>();
		for(Dish dish : dishes) {
			levels.put(dish,dish.getStock());
		}
//		stockManager.dishCheck();
		return levels;
	}

	
	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
		
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		restockDishEnabled=enabled;
	}

	public boolean getRestockDishEnabled(){
		return restockDishEnabled;
	}
	
	@Override
	public void setStock(Dish dish, Number stock) {
		dish.setStockLevel(stock);
//		stockManager.dishCheck();
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
		ingredient.setStockLevel(stock);
//		stockManager.ingredientCheck();
	}

	@Override
	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier,
			Number restockThreshold, Number restockAmount, Number weight) {
		Ingredient mockIngredient = new Ingredient(name,unit,supplier,restockThreshold,restockAmount,weight);
		this.ingredients.add(mockIngredient);
		mockIngredient.setStockLevel(mockIngredient.getRestockAmount());
		this.notifyUpdate();
		return mockIngredient;
	}

	@Override
	public void removeIngredient(Ingredient ingredient)throws UnableToDeleteException {
		if(gh.isIngredientUsed(dishes,ingredient)){
			throw new UnableToDeleteException("Can't delete this ingredient!!");
		}else {
			int index = this.ingredients.indexOf(ingredient);
			this.ingredients.remove(index);
			this.notifyUpdate();
		}
	}

	@Override
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Postcode postcode) {
		Supplier mock = new Supplier(name,postcode);
		this.suppliers.add(mock);
		return mock;
	}


	@Override
	public void removeSupplier(Supplier supplier) throws UnableToDeleteException{
		if(gh.isSupplierUsed(ingredients, supplier)){
			throw new UnableToDeleteException("Can't delete this supplier!!");
		}else {
			int index = this.suppliers.indexOf(supplier);
			this.suppliers.remove(index);
			this.notifyUpdate();
		}
	}

	@Override
	public List<Drone> getDrones() {
		return this.drones;
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone mock = new Drone(speed);
		mock.setStockManager(stockManager);
		mock.setRestaurant(restaurant.getLocation());
//		dronePool.submit(mock);
		this.drones.add(mock);
		return mock;
	}

	@Override
	public void removeDrone(Drone drone) throws UnableToDeleteException{
		if (!drone.getStatus().equals("IDLE")){
			throw new UnableToDeleteException("Drone busy, can't delete!");
		}else{
			int index = this.drones.indexOf(drone);
			this.drones.remove(index);
			this.notifyUpdate();
		}
	}

	@Override
	public List<Staff> getStaff() {
		return this.staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff mock = new Staff(name);
		mock.setStockManager(stockManager);
//		staffPool.submit(mock);
		this.staff.add(mock);
		return mock;
	}

	@Override
	public void removeStaff(Staff staff)throws UnableToDeleteException{
		if (!staff.getStatus().equals("IDLE")){
			throw new UnableToDeleteException("Drone busy, can't delete!");
		}else {
			this.staff.remove(staff);
			this.notifyUpdate();
		}
	}

	@Override
	public List<Order> getOrders() {
		return this.orders;
	}

	@Override
	public void removeOrder(Order order) throws UnableToDeleteException{
		if(order.getStatus().equals("Request cancel")||order.getStatus().equals("Complete")||order.getStatus().equals("Cancelled")) {
			if(order.getStatus().equals("Request cancel")) {
				order.setStatus("Cancelled");
			}
			int index = this.orders.indexOf(order);
			this.orders.remove(index);
			orderHandler.cancelOrder(order);
			this.notifyUpdate();
		}else{
			throw new UnableToDeleteException("Order in progress, request cancel or comeplete to delete!!");
		}
	}
	
	@Override
	public Number getOrderCost(Order order) {
		Double cost = 0.0;
		for (Entry<Dish,Number> entry : order.getOrder().entrySet()){
			Dish dish = entry.getKey();
			if(entry.getValue()!=null) {
				cost += entry.getValue().doubleValue() * dish.getPrice().doubleValue();
			}
		}
//		notifyUpdate();
		return cost;
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
		List<Ingredient> ingredients = getIngredients();
		HashMap<Ingredient, Number> levels = new HashMap<Ingredient, Number>();
		for(Ingredient ingredient : ingredients) {
			levels.put(ingredient,ingredient.getStock());
		}
//		stockManager.ingredientCheck();
		return levels;
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public Number getOrderDistance(Order order) {
		return order.getDistance();
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		if(quantity == Integer.valueOf(0)) {
			removeIngredientFromDish(dish,ingredient);
		} else {
			dish.getRecipe().put(ingredient,quantity);
		}
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.getRecipe().remove(ingredient);
		this.notifyUpdate();
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return dish.getRecipe();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return this.postcodes;
	}

	@Override
	public Postcode addPostcode(String code) {
		Postcode mock;
		try {
			mock = new Postcode(code, getRestaurant());
			this.postcodes.add(mock);
			this.notifyUpdate();
			return mock;
		}catch(NullPointerException e){
			return null;
		}
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		if (gh.isPostcodeUsed(this, postcode)){
			throw new UnableToDeleteException("Postcode used!!");
		}else {
			this.postcodes.remove(postcode);
			this.notifyUpdate();
		}
	}

	@Override
	public List<User> getUsers() {
		return this.users;
	}
	
	@Override
	public void removeUser(User user) throws UnableToDeleteException{
		if(gh.isUserUsed(orders, user)){
			throw new UnableToDeleteException("User has an active order!");
		}else {
			this.users.remove(user);
			this.notifyUpdate();
		}
	}

	@Override
	public void loadConfiguration(String filename){
//		staffPool.shutdownNow();
//		dronePool.shutdownNow();
		pool.shutdownNow();
//		try {
//			while (true) {
////				Thread.sleep(100);
//				if (staffPool.isTerminated()) {
//					System.out.println("Staff threads should be dead: " + staffPool.getActiveCount());
//					break;
//				}
//			}
//			while(true){
//				if (dronePool.isTerminated()) {
//					System.out.println("Drone threads should be dead: " + dronePool.getActiveCount());
//					break;
//				}
//			}
		while(true){
				if (pool.isTerminated()) {
					System.out.println("Drone threads should be dead: " + pool.getActiveCount());
					break;
				}
			}
		stockManager.kill();
		stockManager=new StockManager(this);
		wipeServer();
		System.out.println("Loaded configuration: " + filename);
		Configuration config = new Configuration(this, filename);
		this.restaurant = config.loadConfig();
		this.users = config.users;
		this.orders= config.orders;
		this.notifyUpdate();
		startStuffAgain();
		stockManager.init1();
		stockManager.pauseDishChecking=false;
	}


	public void startStuffAgain(){
//		staffPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
//		dronePool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(30);
		for (Staff staff : staff){
			pool.submit(staff);
		}
		for (Drone drone : drones){
			pool.submit(drone);
		}
		pool.submit(orderHandler);
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		for(Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
			addIngredientToDish(dish,recipeItem.getKey(),recipeItem.getValue());
		}
		stockManager.pauseDishChecking=false;
		this.notifyUpdate();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return true;
	}

	@Override
	public String getOrderStatus(Order order) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Complete";
		} else {
			return "Pending";
		}
	}
	
	@Override
	public String getDroneStatus(Drone drone) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Flying";
		}
	}
	
	@Override
	public String getStaffStatus(Staff staff) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Working";
		}
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		dish.setRestockThreshold(restockThreshold);
		dish.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestockThreshold(restockThreshold);
		ingredient.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestockAmount();
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestockAmount();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void notifyUpdate() {
//		stockManager.dishCheck();
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

	@Override
	public Postcode getDroneSource(Drone drone) {
		return drone.getSource();
	}

	@Override
	public Postcode getDroneDestination(Drone drone) {
		return drone.getDestination();
	}

	@Override
	public Number getDroneProgress(Drone drone) {
		return drone.getProgress();
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
	public Restaurant getRestaurant() {
		return restaurant;
	}

	private void wipeServer(){
		this.dishes = new ArrayList<Dish>();
		this.drones = new ArrayList<Drone>();
		this.ingredients = new ArrayList<Ingredient>();
		this.orders = new ArrayList<Order>();
		this.staff = new ArrayList<Staff>();
		this.suppliers = new ArrayList<Supplier>();
		this.users = new ArrayList<User>();
		this.postcodes = new ArrayList<Postcode>();
		this.listeners = new ArrayList<UpdateListener>();
	}


//	public User addUser(String name, String password, String location, Postcode postcode){
//		User newUser = new User(name, password,location,postcode);
//		this.users.add(newUser);
//		return newUser;
//	}
}
