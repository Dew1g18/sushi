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
		this.setProgress(null);
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
				Thread.sleep(500);
//				System.out.println(getName()+" is checking");
				if (stockManager.ingNeedRestock.size()>0&&getStatus().equals("IDLE")) {
//					System.out.println(getName()+" restocking");
					Ingredient ing = stockManager.ingNeedRestock.remove(0);
					Integer waitTime = (2*ing.getSupplier().getDistance().intValue())/speed.intValue();
					setStatus("WORKING on " + ing.getName());
					System.out.println(getName()+" is "+getStatus());
//					Thread.sleep(waitTime*1000);
					theWait(waitTime,ing.getSupplier().getPostcode());
					ing.inHand=true;

					ing.restockIncrement();
					setProgress(null);
					ing.inHand=false;
					ing.endRestocking();
					setStatus("IDLE");
				}
				if (stockManager.readyOrders.size()>0&&getStatus().equals("IDLE")){
					System.out.println(getName()+" delivering!");
					Order order = stockManager.readyOrders.remove(0);
					if (!order.getStatus().equals("Cancelled")) {
                        order.setStatus("In transit");
                        setStatus("WORKING on " + order.getName());
                        System.out.println(getStatus());
                        Postcode postcode = order.getUser().getPostcode();
                        Integer waitTime = (2 * postcode.getDistance().intValue()) / speed.intValue();
                        theWait(waitTime, postcode, order);
                        order.setStatus("Complete");
                    }
					setStatus("IDLE");
				}
			}
		}catch(InterruptedException e){
//				e.printStackTrace();
			System.out.println("Probably a new config, hoping this will keep going afterwards.");
		}
	}

	public void theWait(Integer waitTime, Postcode destination)throws InterruptedException{
		try {
			for (Integer i = 0; i < waitTime+1; i++) {
				Thread.sleep(1000);
				Double progress = (i.doubleValue()/waitTime.doubleValue())*100.00;
				setProgress(progress.intValue());

				//				System.out.println(getProgress());
				if (i>waitTime/2){
					setDestination(source);
					setSource(destination);
					setStatus("Returning");
				}else{
					setSource(restaurant);
					setDestination(destination);
				}
			}
		}catch(InterruptedException e){
			System.out.println("HMM");
			throw new InterruptedException();
		}
	}

    public void theWait(Integer waitTime, Postcode destination, Order order)throws InterruptedException{
        try {
            for (Integer i = 0; i < waitTime+1; i++) {
                Thread.sleep(1000);
                Double progress = (i.doubleValue()/waitTime.doubleValue())*100.00;
                setProgress(progress.intValue());

                //				System.out.println(getProgress());
                if (order.getStatus().equals("Cancelled")&&!getStatus().equals("Returning")){
                    i+=(waitTime/2);
					System.out.println("Cancel acknowledged");
                    //todo fix this Return not happen propper
                }

                if (i>waitTime/2){
                    setDestination(source);
                    setSource(destination);
                    setStatus("Returning");
                    if (order.getStatus()!="Cancelled") {
                        order.setStatus("Complete");
                    }
                }else{
                    setSource(restaurant);
                    setDestination(destination);
                }
            }
        }catch(InterruptedException e){
            System.out.println("HMM");
            throw new InterruptedException();
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
