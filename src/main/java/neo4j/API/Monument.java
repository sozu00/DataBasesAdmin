package neo4j.API;

public class Monument implements Place{

	public Monument(String name, MapPosition position, String description, String city){
		this.name = name;
		this.position = position;
		this.description = description;
		this.city = city;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public MapPosition getLocation() {
		return position;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String getCity() {
		return city;
	}

	private String name;
	private MapPosition position;
	private String description;
	private String city;
	
}
