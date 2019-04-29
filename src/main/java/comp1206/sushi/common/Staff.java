package comp1206.sushi.common;

import comp1206.sushi.common.Staff;
import comp1206.sushi.server.StockManager;

import java.util.Random;

public class Staff extends Model implements Runnable{

	private String name;
	private String status;
	private Number fatigue;
	
	public Staff(String name) {
		this.setName(name);
		this.setFatigue(0);
		this.setStatus("IDLE");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getFatigue() {
		return fatigue;
	}

	public void setFatigue(Number fatigue) {
		this.fatigue = fatigue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

	public void restockItem(Dish dish){
		Random random = new Random();
		try {
//			System.out.println(name+" is restocking "+ dish.getName());
			//TODO add the random back into this.
			Thread.sleep(20000 + random.nextInt(40000));
//			Thread.sleep(2000);
			dish.restockIncrement();
//			System.out.println(name+" has restocked "+dish.getName());
			setStatus("IDLE");
			dish.endRestocking();
		}catch (Exception e){
			e.printStackTrace();
			setStatus("IDLE");
		}
	}


	public StockManager stockManager;
	public void setStockManager(StockManager stockManager){
		this.stockManager=stockManager;
	}

	public void run(){
		try {
			while (true){
				Thread.sleep(100);
				if (stockManager.readyDishes.size()>0&&getStatus().equals("IDLE")) {
					Dish dish = stockManager.readyDishes.remove(0);
					dish.inHand=true;
					setStatus("WORKING on " + dish.getName());
					System.out.println(getName()+" is "+getStatus());
					restockBatch(dish);
					dish.inHand=false;
				}
			}
		}catch(InterruptedException e){
//				e.printStackTrace();
			System.out.println("Probably a new config, hoping this will keep going afterwards.");
		}
	}

	public void restockBatch(Dish dish){
		Random random = new Random();
		try {
//			System.out.println(name+" is restocking "+ dish.getName());
			//TODO add the random back into this.
			Thread.sleep(20000 + random.nextInt(40000));
//			Thread.sleep(2000);
			dish.restockBatch();
//			System.out.println(name+" has restocked "+dish.getName());
			System.out.println(getName()+ " restocked "+ dish.getName());
			setStatus("IDLE");
			dish.endRestocking();
		}catch (Exception e){
//			e.printStackTrace();
			setStatus("IDLE");
		}
	}


}
