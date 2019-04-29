package comp1206.sushi.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

public class Dish extends Model implements Serializable {

	private String name;
	private String description;
	private Number price;
	private Map <Ingredient,Number> recipe;
	private Number restockThreshold;
	private Number restockAmount;

	public Dish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.restockThreshold = restockThreshold;
		this.restockAmount = restockAmount;
		this.recipe = new HashMap<Ingredient,Number>();
		this.beingRestocked=false;
		this.restockers = 0;
//		this.batches=1;
	}

	public boolean inHand = false;

	private boolean beingRestocked;
	public void startRestocking(){
		beingRestocked=true;
		restockers+=1;
//		System.out.println(restockers);
	}
	public void endRestocking(){
		beingRestocked=false;
		restockers-=1;
//		System.out.println(restockers);
	}

//	private int batches;
//
//	public void startBatch(){
//		batches+=1;
//	}
//	public void endBatch(){
//		batches-=1;
//	}


	public int restockers;

	public int getStockPotential(){
		return getStock().intValue()+ restockers*restockAmount.intValue();
	}

	public int getStockPlusRestockers(){
		return getStock().intValue()+restockers;
	}

	public boolean isBeingRestocked() {
		return beingRestocked;
	}

	private Number stock;
	public synchronized void setStockLevel(Number stockLevel){
		this.stock = stockLevel;
//		System.out.println(getName()+" ~ "+stockLevel);
	}
	public Number takePositiveStock(Number delta)throws Exception{
		if (delta.intValue()>getStock().intValue()){
			throw new Exception("Can't take that much!!");
		}
		setStockLevel(getStock().intValue()-delta.intValue());
		return stock;
	}
	public synchronized Number getStock(){
		return stock;
	}
	public Number restockIncrement(){
		setStockLevel(getStock().intValue()+1);
		return stock;
	}
	public void restockBatch(){
		setStockLevel(getStock().intValue()+getRestockAmount().intValue());
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Number getPrice() {
		return price;
	}

	public void setPrice(Number price) {
		this.price = price;
	}

	public Map <Ingredient,Number> getRecipe() {
		return recipe;
	}

	public void setRecipe(Map <Ingredient,Number> recipe) {
		this.recipe = recipe;
	}

	public void setRestockThreshold(Number restockThreshold) {
		this.restockThreshold = restockThreshold;
	}
	
	public void setRestockAmount(Number restockAmount) {
		this.restockAmount = restockAmount;
	}

	public Number getRestockThreshold() {
		return this.restockThreshold;
	}

	public Number getRestockAmount() {
		return this.restockAmount;
	}

}
