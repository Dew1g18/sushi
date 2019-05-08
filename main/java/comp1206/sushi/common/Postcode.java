package comp1206.sushi.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
//import LatLng;

import comp1206.sushi.server.GenericHelp;

public class Postcode extends Model implements Serializable {

	private String name;
	private HashMap<String,Double> latLong;
	private Number distance;

	GenericHelp ad = new GenericHelp();

	public Postcode(String code) {
		this.name = code;
		calculateLatLong();
		this.distance = Integer.valueOf(0);
	}

	public Postcode(String code, Restaurant restaurant) {
		this.name = code;
		calculateLatLong();
		Postcode rest = restaurant.getLocation();
//		calculateDistance(restaurant.getLocation());
		this.distance = distance(getLatLong().get("lat"), rest.getLatLong().get("lat"), getLatLong().get("lon"), rest.getLatLong().get("lon"), 0.0, 0.0);
		this.distance = this.distance.intValue();
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getDistance() {
		return this.distance;
	}

	public HashMap<String,Double> getLatLong() {
		return this.latLong;
	}

	protected void calculateDistance(Postcode destination) {//deprecated
		//This function needs implementing
		double desLat = destination.latLong.get("lat");
		double desLong = destination.latLong.get("lon");
		double posLat = this.latLong.get("lat");
		double posLong = this.latLong.get("lon");
		double latDistance = desLat-posLat;
		double latDistanceSq = latDistance*latDistance;
		double lonDistance = desLong-posLong;
		double lonDistanceSq = lonDistance*lonDistance;
		this.distance = Math.sqrt(latDistanceSq+lonDistanceSq);
	}

	protected void calculateLatLong() {
		//This function needs implementing
//		String getting = https://www.southampton.ac.uk/~ob1a12/postcode/postcode.php?postcode=
		try {
			String info = ad.getStuffFromAPI(this);
			String[] infoArray = info.split("#");
			double lat = Double.parseDouble(infoArray[0]);
			double longitude = Double.parseDouble(infoArray[1]);
			lat = (double) Math.round(lat * 1000000d) / 1000000d;
			longitude = (double) Math.round(longitude * 1000000d) / 1000000d;

//		System.out.println(longitude);
			this.latLong = new HashMap<>();
			latLong.put("lat", lat);
			latLong.put("lon", longitude);
			this.distance = new Integer(0);
		}catch (NullPointerException e){
			System.out.println("Postcode invalid");
		}
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 *
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 * @returns Distance in Meters
	 *
	 * CREDIT TO DAVID GEORGE STACK OVERFLOW
	 */
	public static double distance(double lat1, double lat2, double lon1,
								  double lon2, double el1, double el2) {

		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}

	/**
	 * getting distance from latlong was very complicated, got the method from a dude on stack overflow
	 * 	-https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
	 */

}
