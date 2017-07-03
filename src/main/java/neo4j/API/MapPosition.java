package neo4j.API;

public class MapPosition {
	public MapPosition(double longitude, double latitude){
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	private double longitude;
	private double latitude;
}
