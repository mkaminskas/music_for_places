package mfp;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

/**
 * The main class for generating recommendations using the five competing approaches:
 * "manual","semantic","combined","autoAll", or "random" baseline
 */
public class Recommender implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public final int userId;
	public final String userName;
	public final String sessionId;
	public int currentPOIId;
	public int stepNum;
	
	public Recommender(int uId, String userName) {
		this.userId = uId;
		this.userName = userName;
		this.sessionId = generateSessionId();
		this.currentPOIId = generatePoiId();
		this.stepNum = 0;
	}
	
	public int getNextPOIId() {
		this.currentPOIId = generatePoiId();
		return currentPOIId;
	}
	
	public int getStepNum() {
		return stepNum;
	}
	
	public void increaseStepNum() {
		this.stepNum ++;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public String getUsername() {
		return userName;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	//	generate the id of a POI to display:
	//	get the least represented (and least seen by the user)
	//	POI that the user has not yet seen in this session
	private int generatePoiId() {
		int id = 0;
		try {
			Connection conn = Database.connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT pois.id, "+
												"COUNT(DISTINCT ev.session_id) c1, "+
												"COUNT(DISTINCT CASE WHEN user_id="+userId+" THEN session_id END) c2 " +
											 "FROM dbpedia_pois pois "+
											 	"LEFT JOIN dbpedia_final_evaluation ev "+
											 	"ON pois.id = ev.poi_id "+
											 "WHERE pois.id NOT IN "+
											 	"(SELECT poi_id FROM dbpedia_final_evaluation "+ 
											 	 "WHERE session_id='"+sessionId+"') "+
											 "GROUP BY pois.id "+
											 "ORDER BY c1, c2 "+
											 "LIMIT 0,1");
			rs.next();
			id = rs.getInt("id");
		    
		    rs.close();
		    conn.close();
		} catch (Exception e) {System.out.println("Exception generating poi ID: "+e);}
		return id;
	}
	
	
	//	generate a valid sessionId ("userId"_"# of last session + 1")
	private String generateSessionId() {
		String id = "";
		try {
			Connection conn = Database.connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT session_id) x "+
											 "FROM dbpedia_final_evaluation "+
											 "WHERE user_id = "+userId);
			rs.next();
			id = Integer.toString(userId)+"_"+Integer.toString(rs.getInt("x")+1);
			
		    rs.close();
		    conn.close();
		} catch (Exception e) {System.out.println("Exception generating session ID: "+e);}
		return id;
	}
	
	
	// get the top-ranked track by a specified method -
	// manual, auto, semantic, combined, or auto on all tracks -
	// possibly filtered by user's genre preferences
	private int getTopTrackByMethod(String method, boolean personalized){
		int trackId = -1;
		
		try {
			Connection conn = Database.connect();
			Statement stmt = conn.createStatement();
			
			String query = "SELECT dbpedia_track FROM new_similarity"+
					 	   " WHERE dbpedia_poi = "+currentPOIId+
					 	   " ORDER BY "+method+"_similarity DESC LIMIT 0,1";
			if (personalized){
				query = "SELECT ds.dbpedia_track "+
						 "FROM new_similarity ds, dbpedia_tracks dt "+
						 "WHERE ds.dbpedia_poi = "+currentPOIId+" AND "+
						 	"ds.dbpedia_track=dt.id AND "+
						 	"(SELECT genres FROM user WHERE id="+userId+") LIKE "+
						 		"CONCAT('%',dt.genre,'%') "+
						 "ORDER BY "+method+"_similarity DESC LIMIT 0,1";
			}
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()){
				trackId = rs.getInt("dbpedia_track");
			} else {
				// if the user didn't choose any genres, just take the top-ranked track
				trackId = getTopTrackByMethod(method, false);
			}
			rs.close();
		    conn.close();
		} catch (Exception e) {System.out.println("Exception getting "+method+" track: "+e);}
		return trackId;
	}
	
	
	// get the random track possibly filtered by user's genre preferences
	private int getRandomTrack(boolean personalized){
		int trackId = -1;
		try {
			Connection conn = Database.connect();
			Statement stmt = conn.createStatement();
			
			String query = "SELECT id FROM dbpedia_tracks "+
						   "ORDER BY RAND() LIMIT 0,1";
			if (personalized){
				query = "SELECT id FROM dbpedia_tracks "+
						"WHERE (SELECT genres FROM user WHERE id="+userId+") LIKE "+
			 				   		"CONCAT('%',genre,'%')" +
			 			"ORDER BY RAND() LIMIT 0,1";
			}
			
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()){
				trackId = rs.getInt("id");
			} else {
				// if the user didn't choose any genres, just take the top-ranked track
				trackId = getRandomTrack(false);
			}
			rs.close();
		    conn.close();
		} catch (Exception e) {System.out.println("Exception getting the random track: "+e);}
		return trackId;
	}
	
	
	// main method to generate recommendations using the different approaches
	public HashMap<Integer,String> generateNewTracks(){
		
//		boolean personalized = false;
		
		HashMap<Integer,String> tracksAndMethods = new HashMap<Integer,String>();
		int trackId = 0;
		
		String[] methods = {"manual","semantic","combined","autoAll"};
		ShuffleArray.shuffleArray(methods);
		
		for (String method : methods){
			
			trackId = getTopTrackByMethod(method, false);
			if (tracksAndMethods.containsKey(trackId)){
				String previousValue = tracksAndMethods.get(trackId);
				tracksAndMethods.put(trackId, previousValue+","+method);
			} else {
				tracksAndMethods.put(trackId, method);
			}
			
		}
		
		// try to get a random track until we find one which is not yet in tracksAndMethods
		do{
			trackId = getRandomTrack(true);
		} while (tracksAndMethods.containsKey(trackId));
		tracksAndMethods.put(trackId, "random");
		
		return tracksAndMethods;
	}
	
	
	// insert the recommended trackIds and their correcponding methods into the DB
	public void insertCurrentStep(HashMap<Integer,String> tracksAndMethods){
		Database.insertCurrentStep(tracksAndMethods, sessionId, stepNum, userId, currentPOIId);
	}
	
	// delete the record of the current step from the DB (done when the user logs-out)
	public void deleteCurrentStep(){
		Database.deleteCurrentStep(sessionId, stepNum);
	}
	
	// update the previous step with track selections that the user made
	public void updateTrackSelections(List<Integer> selectedTracks){
		Database.updateTrackSelections(selectedTracks, sessionId, stepNum);
	}
	
	// update the previous step with track selections that the user made
	public void updateTrackMisses(List<Integer> missedTracks){
		Database.updateTrackMisses(missedTracks, sessionId, stepNum);
	}
	
	// record the click of a POI's / track's URL
	public void recordUrlClick(String poiOrTrack, String itemId){
		Database.recordUrlClick(poiOrTrack, itemId, sessionId, stepNum);
	}
	
	// get the info of the current POI
	public POIBean getPOIInfo(){
		return Database.getPOIInfo(currentPOIId);
	}
	
}
