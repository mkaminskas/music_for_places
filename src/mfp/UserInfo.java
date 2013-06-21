package mfp;

import java.sql.*;
import java.util.Hashtable;

/**
 * The class for managing user information
 */
public class UserInfo {

	private int id;
	private String name;
	private String password;
	private String repeatPassword;
	private String[] genres;
	private Hashtable<String, String> errors;
	
	public UserInfo(){
		this.id = 0;
		this.name = "";
		this.password = "";
		this.repeatPassword = "";
		this.genres = new String[0];
		this.errors = new Hashtable<String, String>();
	}
	
	public UserInfo(int id, String name, String password, String[] genres) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.repeatPassword = "";
		this.genres = genres;
		this.errors = new Hashtable<String, String>();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRepeatPassword() {
		return repeatPassword;
	}
	
	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}
	
	public String[] getGenres() {
		return genres;
	}
	
	public void setGenres(String[] genres) {
		this.genres = genres;
	}
	
	public boolean userLikesGenre(String genre){
		boolean rez = false;
		for (String g : this.genres){
			if (g.equals(genre)) { rez = true; }
		}
		return rez;
	}
	
	public void setErrors(String key, String msg) {
	    errors.put(key,msg);
	}
	
	public String getErrorMsg(String s) {
	    String errorMsg =(String)errors.get(s.trim());
	    return (errorMsg == null) ? "":errorMsg;
	}
	
	
	public boolean addUser() {
	    boolean allOk=true;
	    
	    if (name.equals("")) {
	      errors.put("name","Please enter your name");
	      name="";
	      allOk=false;
	    }
	    if (password.equals("")) {
		      errors.put("password","Please enter your password");
		      password="";
		      allOk=false;
		}
	    if (!password.equals("") && !repeatPassword.equals(password)) {
		      errors.put("repeatPassword","Password repeated incorrectly");
		      repeatPassword="";
		      allOk=false;
		}
	    
	    // transforming array of genres into a String
	    String genreString=""; 
		if (genres != null){
			for (int i=0; i<genres.length; i++){
				genreString += genres[i];
				if (i < genres.length-1) genreString += ",";
			}
		}
	    
	    if (!name.equals("") && !password.equals("") && repeatPassword.equals(password)) {
	    	
	    	name = EscapeChars.forHTMLTag(name);
	    	String sql = "SELECT name FROM user WHERE name='"+name+"'";
	    	
	    	String sql2 = "INSERT INTO user (name, password, genres) VALUES ('" + name + "','" + password + "','" + genreString + "')";
	    	
	    	try {
	    		Connection conn = Database.connect();
	    	    Statement stmt = conn.createStatement();
	    	    
	    	    ResultSet rs = stmt.executeQuery(sql);
	    	    if (rs.next()) { // if the username is already taken
	    	    	errors.put("add_error","The user name is already taken! Choose a different one.");
	    	    	allOk=false;
	    	    } else {
	    	    	System.out.println(sql2);
	    	    	stmt.execute(sql2);
	    	    	allOk=true;
	    	    }
	    		conn.close();
	    	} catch(Exception e) {
	    		errors.put("add_error","The information couldn't be recorded!!");
	    		allOk=false;
	    		e.printStackTrace();
	    	}
	    	
	    }
	    return allOk;
	}
	
	
	public boolean loginUser(){
		boolean allOk=true;
		
		if (name.equals("")) {
		      errors.put("nameLogin","Username not entered! ");
		      name="";
		      allOk=false;
		}
		if (password.equals("")) {
		      errors.put("passwordLogin","Password not entered! ");
		      password="";
		      allOk=false;
		}
		
		if (allOk){
			try {
				Connection conn = Database.connect();
				Statement stmt = conn.createStatement();
    		    String sql = "SELECT id, name, password FROM user WHERE name='"+name+"' AND password='"+password+"'";
    		    ResultSet rs =stmt.executeQuery(sql);
    		    if (rs.next()) { this.id = rs.getInt("id"); }
    		    else {
    		    	allOk=false;
    		    	errors.put("login_error","Login failed!");
    		    }
    		    conn.close();
    		} catch(Exception e) {
    			errors.put("login_error","Login failed!");
    			allOk=false;
   				e.printStackTrace();
    		}
		}
		
		return allOk;
	}
	
}
