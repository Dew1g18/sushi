package comp1206.sushi.common;

import comp1206.sushi.server.StockManager;


public class Drone extends Model implements Runnable {

	private Number speed;
	private Number progress;
	
	private Number capacity;
	private Number battery;
	
	private String status;
	
	private Postcode source;
	private Postcode destination;

	public Drone(Number speed) {
		this.setSpeed(speed);
		this.setCapacity(1);
		this.setBattery(100);
		this.setStatus("IDLE");
		this.setProgress(0);
		this.setDestination(null);

	}

	public StockManager stockManager;
	public void setStockManager(StockManager stockManager){
		this.stockManager=stockManager;
	}


	@Override
	public void run() {
		try {
//			System.out.println("Drone running");
			while (true){
				Thread.sleep(100);
				if (stockManager.ingNeedRestock.size()>0&&getStatus().equals("IDLE")) {
					System.out.println("Drone restocking");
					Ingredient ing = stockManager.ingNeedRestock.remove(0);
					Integer waitTime = (2*ing.getSupplier().getDistance().intValue())/speed.intValue();
					setStatus("WORKING on " + ing.getName());
					System.out.println(getName()+" is "+getStatus());
//					Thread.sleep(waitTime*1000);
					theWait(waitTime,ing);
					ing.inHand=true;

					ing.restockIncrement();
					ing.inHand=false;
					ing.endRestocking();
					setStatus("IDLE");
				}
			}
		}catch(InterruptedException e){
//				e.printStackTrace();
			System.out.println("Probably a new config, hoping this will keep going afterwards.");
		}
	}

	public void theWait(Integer waitTime, Ingredient ingredient){
		try {
			for (Integer i = 0; i < waitTime+1; i++) {
				Thread.sleep(1000);
				Double progress = (i.doubleValue()/waitTime.doubleValue())*100.00;
				setProgress(progress.intValue());

				//				System.out.println(getProgress());
				if (i>waitTime/2){
					setDestination(source);
					setSource(ingredient.getSupplier().getPostcode());
				}else{
					setSource(restaurant);
					setDestination(ingredient.getSupplier().getPostcode());
				}
			}
		}catch(InterruptedException e){
			System.out.println("HMM");
		}
	}

	public Number getSpeed() {
		return speed;
	}

	private Postcode restaurant;
	public void setRestaurant(Postcode postcode){
		setSource(postcode);
		this.restaurant = postcode;
	}

	public Number getProgress() {
		return progress;
	}
	
	public void setProgress(Number progress) {
		this.progress = progress;
	}
	
	public void setSpeed(Number speed) {
		this.speed = speed;
	}
	
	@Override
	public String getName() {
		return "Drone (" + getSpeed() + " speed)";
	}

	public Postcode getSource() {
		return source;
	}

	public void setSource(Postcode source) {
		this.source = source;
	}

	public Postcode getDestination() {
		return destination;
	}

	public void setDestination(Postcode destination) {
		this.destination = destination;
	}

	public Number getCapacity() {
		return capacity;
	}

	public void setCapacity(Number capacity) {
		this.capacity = capacity;
	}

	public Number getBattery() {
		return battery;
	}

	public void setBattery(Number battery) {
		this.battery = battery;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}
	
}
