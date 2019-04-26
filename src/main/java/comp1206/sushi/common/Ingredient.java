package comp1206.sushi.common;

import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Supplier;

import java.io.Serializable;

public class Ingredient extends Model implements Serializable {

	private String name;
	private String unit;
	private Supplier supplier;
	private Number restockThreshold;
	private Number restockAmount;
	private Number weight;

	public Ingredient(String name, String unit, Supplier supplier, Number restockThreshold,
			Number restockAmount, Number weight) {
		this.setName(name);
		this.setUnit(unit);
		this.setSupplier(supplier);
		this.setRestockThreshold(restockThreshold);
		this.setRestockAmount(restockAmount);
		this.setWeight(weight);
	}

	private Number stock;
	public synchronized void setStockLevel(Number stockLevel){
		this.stock = stockLevel;
//		System.out.println(getName()+" ~ "+stockLevel);
	}
	public synchronized Number takePositiveStock(Number delta)throws Exception{
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
		setStockLevel(getStock().intValue()+restockAmount.intValue());
		return stock;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public Number getRestockThreshold() {
		return restockThreshold;
	}

	public void setRestockThreshold(Number restockThreshold) {
		this.restockThreshold = restockThreshold;
	}

	public Number getRestockAmount() {
		return restockAmount;
	}

	public void setRestockAmount(Number restockAmount) {
		this.restockAmount = restockAmount;
	}

	public Number getWeight() {
		return weight;
	}

	public void setWeight(Number weight) {
		this.weight = weight;
	}

}
