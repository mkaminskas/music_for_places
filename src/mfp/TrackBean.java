package mfp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * The class for managing music track information
 */
public class TrackBean {
	
	private int id;
	private String name;
	private String musician;
	private String filename;
	private String genre;
	private String wiki_page;
	
	public TrackBean(int trackId){
		try {
			Connection conn = Database.connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT t.name, m.name musician, t.filename, t.genre, m.page "+
											 "FROM dbpedia_tracks t, dbpedia_musicians m " +
											 "WHERE t.id="+trackId+" AND m.musician = t.musician");
		    rs.next();
		    String trackname = rs.getString("name");
		    String musician = rs.getString("musician");
		    String filename = rs.getString("filename");
		    String genre = rs.getString("genre");
		    String wiki_page = rs.getString("page");
		    
		    this.id = trackId;
			this.name = trackname;
			this.musician = musician;
			this.filename = filename;
			this.genre = genre;
			this.wiki_page = wiki_page;
		    
		    rs.close();
		    conn.close();
		} catch (Exception e) {System.out.println("Exception creating TrackBean "+trackId+": "+e);}
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getMusician(){
		return this.musician;
	}
	
	public String getFilename(){
		return this.filename;
	}
	
	public String getGenre(){
		return this.genre;
	}
	
	public String getWikiPage(){
		return this.wiki_page;
	}
	
}
