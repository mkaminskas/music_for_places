package mfp;

/**
 * The class for managing POI information
 */
public class POIBean{
	
	private final int id;
	private final String name;
	private final String description;
	private final String wiki_page;
	private final String city;
	private final String country;
	
	public POIBean(int id, String name, String description, String wiki_page, String city, String country){
		this.id = id;
		this.name = name;
		this.description = description;
		this.wiki_page = wiki_page;
		this.city = city;
		this.country = country;
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public String getWikiPage(){
		return this.wiki_page;
	}
	
	public String getCity(){
		return this.city;
	}
	
	public String getCountry(){
		return this.country;
	}
	
}
