package neo4j.API;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.neo4j.driver.v1.*;
import static org.neo4j.driver.v1.Values.parameters;

public class DataBaseAdmin {
	
	private final String url = "bolt://hobby-dmobkpimjildgbkeofdgalpl.dbs.graphenedb.com:24786";
	private final String user = "usuario";
	private final String password = "b.CiTdTt5vQofF.L6XJiGO6CTQQU87Y";
	private Session session;
	private Driver driver;
	
    /**
	 * Connects to the Neo4j Database with the user in a background thread.
	 */
	public void connect() throws InterruptedException{
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run(){
			driver = GraphDatabase.driver(url, AuthTokens.basic(user, password));
			session = driver.session();
			}
		});
		
    	thread.start();
    	thread.join();
	}
	
	/**
	 * Adds one place to the database.
	 * @param p Place to be added 
	 */
	public void addPlace(Place p){
		String query = "CREATE (:" + p.getClass().getSimpleName()
				+ " {name: {name}, city: {city}, latitude: {latitude}, longitude: {longitude}, description: {description}})";
		session.run(
				query, 
				parameters( 
					"name", p.getName(), 
					"latitude", p.getLocation().getLatitude(),
					"longitude", p.getLocation().getLongitude(),
					"description", p.getDescription(),
					"city", p.getCity()
					)
				);
		relateToNearPlaces(p);
	}
	
	/**
	 * Relates the place p added to all the near places with less than 500m of distance
	 */
	private void relateToNearPlaces(Place p){
		String query = "MATCH (m1:Monument),(m2:Monument) WHERE m1.name = {name1} AND m2.name = {name2} CREATE (m1)-[:isNearby]->(m2), (m2)-[:isNearby]->(m1)";
		for(Place p2 : getNearbyPlaces(p)){
			session.run(
					query, 
					parameters( 
						"name1", p.getName(), 
						"name2", p2.getName()
						)
					);
		}
	}

	/**
	 * Returns an ArrayList of Places that are near to p in the same city
	 * @param p Place desired to get the near ones.
	 * @return PlacesOfTheCity that are closer than 500m to p
	 */
	public ArrayList<Place> getNearbyPlaces(Place p) {
		ArrayList<Place> PlacesOfTheCity = getList(p.getCity());
		Iterator<Place> PlacesIterator = PlacesOfTheCity.iterator();

		while(PlacesIterator.hasNext()){
			if(distanceOfPlaces(PlacesIterator.next(), p) < 500)
				PlacesIterator.remove();
		}
		return PlacesOfTheCity;
	}

	/**
	 * Calculate distance between two points in latitude and longitude . 
	 * Uses Haversine method as its base.
	 * 
	 * @returns Distance in Meters
	 */
	private static double distanceOfPlaces(Place origin, Place destination) {
		
		double latitude1 = origin.getLocation().getLatitude();
		double latitude2 = destination.getLocation().getLatitude();
		double longitude1 = origin.getLocation().getLongitude();
        double longitude2 = origin.getLocation().getLongitude();
	    final int R = 6371; // Radius of the earth

	    double latDistance = Math.toRadians(latitude2 - latitude1);
	    double lonDistance = Math.toRadians(longitude2 - longitude1);
	    
	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	   
	    double distance = R * c * 1000; // convert to meters

	    distance = Math.pow(distance, 2);

	    return Math.sqrt(distance);
	}

	/**
	 * Returns an ArrayList of Places in the city given.
	 * @param city Name of the city to be checked.
	 * @return PlacesOfTheCity that are inside the city
	 */
	public ArrayList<Place> getList(String city){
		ArrayList<Place> PlacesList = new ArrayList<>();
		String query = "MATCH (m:Monument {city:{city}}) RETURN m.name as name, m.city as city, m.latitude as lat, m.longitude as lon, m.description as description";
		StatementResult result = session.run(query, Collections.<String, Object>singletonMap("city", city));
	    
		while (result.hasNext()) {
			Record record = result.next();
	    	Place p = new Monument(
	    			record.get("name").asString(),
		    			new MapPosition(
		    					record.get("lon").asDouble(), 
		    					record.get("lat").asDouble()),
		    			record.get("description").asString(),
		    			record.get("city").asString()
	    			);
	        PlacesList.add(p);
	    }
		
		return PlacesList;
	}
	
	/**
	 * Closes the connection	
	 */
	public void close()
    {
        // Closing a driver immediately shuts down all open connections.
        driver.close();
    }

}
