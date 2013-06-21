package mfp;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The class containing only static methods that require DB reading/writing
 */
public class Database {
	
	// database location: 0 for root, 1 for remote connection
	public static final int LOCATION = 0;
	
	// connect to the database
	public static Connection connect() throws SQLException {
		
		Connection connection = null;
		String mydatabase, serverName, username, password; // connection parameters
		
		try {
			String driverName = "org.gjt.mm.mysql.Driver"; // MySQL MM JDBC driver
			Class.forName(driverName);
			
			switch (LOCATION){
				case 0 :
					mydatabase = "eval-lamj";
					serverName = "localhost";
					username = "root";
		        	password = "root";
					break;
				case 1 :
					mydatabase = "eval";
					serverName = "localhost";
					username = "mkaminskas";
		        	password = "mkaminskas";
					break;
				default :
					mydatabase = "eval-lamj";
					serverName = "localhost";
					username = "root";
					password = "root";
					break;
			}
			// Create a connection to the database
        	String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
        	connection = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find the database driver");
		} catch (SQLException e) {
			System.out.println("Could not connect to the database");
		}
		
		return connection;
	}
	
	// get the info of a POI
	public static POIBean getPOIInfo(int poiId){
		POIBean poi = null;
		try {
			Connection conn = Database.connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name, wiki_page, description, city, country "+
											 "FROM dbpedia_pois WHERE id ="+poiId);
		    rs.next();
		    String name = rs.getString("name");
		    String description = rs.getString("description");
		    String wiki_page = rs.getString("wiki_page");
		    String city = rs.getString("city");
		    String country = rs.getString("country");
		    
		    poi = new POIBean(poiId,name,description,wiki_page,city,country);
		    
		    rs.close();
		    conn.close();
		} catch (Exception e) {System.out.println("Exception getting POI info: "+e);}
		return poi;
	}
	
	// get the list of all music genres in DB
	public static List<String> getGenres(){
		List<String> rez = new LinkedList<String>();
		try {
			Connection conn = connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT genre FROM dbpedia_tracks WHERE genre IS NOT NULL ORDER BY genre");
		    while (rs.next()){
		    	String genre = rs.getString("genre");
		    	rez.add(genre);
		    }
		    rs.close();
		    conn.close();
		} catch (Exception e) {System.out.println("Exception getting genres: "+e);}
		
		return rez;
	}
	
	// record the click of a POI's / track's URL
	public static void recordUrlClick(String poiOrTrack, String itemId, String sessionId, int stepNum){
	    try {
	    	Connection conn = Database.connect();
	    	Statement stmt = conn.createStatement();
	    	stmt.executeUpdate("UPDATE dbpedia_final_evaluation " +
				   	   "SET "+poiOrTrack+"_url_clicked = 1 " +
				   	   "WHERE session_id = '"+sessionId+"' " +
				   	   		"AND step_num = '"+Integer.toString(stepNum-1)+"' " +
				   	   		"AND "+poiOrTrack+"_id = "+itemId);
	    	conn.close();
	        } catch (SQLException e) {System.out.println("Exception recording URL click: "+e);}
	}
	
	// update the previous step with track selections that the user made
	public static void updateTrackMisses(List<Integer> missedTracks, String sessionId, int stepNum){
		try {
			Connection conn = Database.connect();
			for (int trackId: missedTracks){
				Statement stmt = conn.createStatement();
				stmt.executeUpdate("UPDATE dbpedia_final_evaluation " +
									"SET total_miss = 1 " +
									"WHERE session_id = '"+sessionId+"' " +
									"AND step_num = '"+Integer.toString(stepNum-1)+"' " +
									"AND track_id = "+trackId);
				stmt.close();
			}
			conn.close();
		} catch (Exception e) {System.out.println("Exception updating misses: "+e);}
	}
	
	// update the track selections that the user made
	public static void updateTrackSelections(List<Integer> selectedTracks, String sessionId, int stepNum){
		try {
			Connection conn = Database.connect();
			for (int trackId: selectedTracks){
				Statement stmt = conn.createStatement();
				stmt.executeUpdate("UPDATE dbpedia_final_evaluation " +
									"SET selected = 1 " +
									"WHERE session_id = '"+sessionId+"' " +
									"AND step_num = '"+Integer.toString(stepNum-1)+"' " +
									"AND track_id = "+trackId);
					
				stmt.close();
			}
			conn.close();
		} catch (Exception e) {System.out.println("Exception updating entry: "+e);}
	}
	
	// insert the trackIds and their correcponding methods into the DB
	public static void insertCurrentStep(HashMap<Integer,String> tracksAndMethods, String sessionId, int stepNum, int userId, int poiId){
		try {
			Connection conn = Database.connect();
			for (int trackId: tracksAndMethods.keySet()){
			String method = tracksAndMethods.get(trackId);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO dbpedia_final_evaluation " +
								"(user_id, session_id, step_num, " +
								"method, poi_id, poi_url_clicked, " +
								"track_id, track_url_clicked, selected, total_miss) " +
							   "VALUES('"+userId+"','"+sessionId+"','"+stepNum+"'," +
							   	"'"+method+"',"+poiId+",'"+0+"'," +
							   	"'"+trackId+"',"+0+",'"+0+"','"+0+"')");
			stmt.close();
			}
			conn.close();
		} catch (Exception e) {System.out.println("Exception inserting entry: "+e);}
	}
	
	// delete the record of the current step from the DB (done when the user logs-out)
	public static void deleteCurrentStep(String sessionId, int stepNum){
		try {
			Connection conn = Database.connect();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM dbpedia_final_evaluation "+
								"WHERE session_id='"+sessionId+"' AND step_num="+stepNum);
			stmt.close();
			conn.close();
		} catch (Exception e) {System.out.println("Exception deleting entry: "+e);}
	}

}
