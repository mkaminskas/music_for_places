<%@ page import="mfp.Recommender" %>
<%@ page import="mfp.POIBean" %>
<%@ page import="mfp.TrackBean" %>
<%@ page import="java.util.*" %>

<%
if (session.getAttribute("sessionRec")!=null) {
	
	// record the selected/missed track Ids in DB
	if (request.getParameterValues("suggested_music")!=null){
		
		List<Integer> selectedTracks = new LinkedList<Integer>();
		//List<Integer> missedTracks = new LinkedList<Integer>();
		
		for (String trackId: request.getParameterValues("suggested_music")){
			
			//if (request.getParameterValues("music_choice_"+trackId) != null){
			//	for (String choice: request.getParameterValues("music_choice_"+trackId)){
			//		if (choice.equals("good")){	selectedTracks.add(Integer.parseInt(trackId)); }
			//		else if (choice.equals("bad")){	missedTracks.add(Integer.parseInt(trackId)); }
			//	}
			//}
			
			selectedTracks.add(Integer.parseInt(trackId));
			
		}
		sessionRec.updateTrackSelections(selectedTracks);
		//sessionRec.updateTrackMisses(missedTracks);
	}
	
	// record which of the wiki urls the user clicked
	if (request.getParameterValues("poi_url_clicked")!=null && request.getParameterValues("musician_url_clicked")!=null){
		
		if (!request.getParameter("poi_url_clicked").equals("0")){
			// update poi_url_clicked
			sessionRec.recordUrlClick("poi",request.getParameter("poi_url_clicked"));
		}
		
		for (String trackId: request.getParameterValues("musician_url_clicked")){
			if (!trackId.equals("0")){
				// update track_url_clicked
				sessionRec.recordUrlClick("track",trackId);
			}
		}
		
	}

// generate the POI id to display
int poiId = sessionRec.getNextPOIId();

if (poiId > 0 && sessionRec.getStepNum() < 11) {
	
	// the current POI
	POIBean poi = sessionRec.getPOIInfo();
	
	// generate the music tracks for the current POI
	HashMap<Integer,String> tracksAndMethods = sessionRec.generateNewTracks();
	
	// record the current step into DB
	sessionRec.insertCurrentStep(tracksAndMethods);
	
%>
	<table border="0" width="100%" cellpadding="0" cellspacing="0">
	<tr>
		<td width="60%" valign="top" style="text-align:justify; border-right: thin solid #696969; padding-right:5px;">
			
			<b><%=poi.getName()%>, <%=poi.getCity()%>, <%=poi.getCountry()%></b><br>
			<a href="<%=poi.getWikiPage()%>" onclick="poiUrlClicked(<%=poiId%>)" target="_blank"><%=poi.getWikiPage()%></a>
			<br>
			<br>
			
			<table border="0" width="100%" cellpadding="0" cellspacing="0">
			<tr>
				<td style="text-align:right; vertical-align:bottom; border-right: thin solid #696969; padding-right:5px; padding-bottom:5px;">
					
					<a href="javascript:;">
						<img src="images/<%=poi.getName()%>.jpg" width="230" border="0" onClick="JustSoPicWindow('images/<%=poi.getName().replace("'","")%>-large.jpg','500','375','','#FFFFFF','hug image','2');return document.MM_returnValue">
					</a>
					
				</td>
				<td style="text-align:left; vertical-align:bottom; padding-left:5px; padding-bottom:5px;">
				
					<a href="javascript:;">
						<img src="images/<%=poi.getName()%>2.jpg" width="230" border="0" onClick="JustSoPicWindow('images/<%=poi.getName().replace("'","")%>-large2.jpg','500','333','','#FFFFFF','hug image','2');return document.MM_returnValue">
					</a>
				
				</td>
			</tr>
			<tr>
				<td colspan="2" style="text-align:center; vertical-align:top; border-top: thin solid #696969; padding-top:5px;">
				
					<a href="javascript:;">
						<img src="images/<%=poi.getName()%>3.jpg" width="230" border="0" onClick="JustSoPicWindow('images/<%=poi.getName().replace("'","")%>-large3.jpg','500','382','','#FFFFFF','hug image','2');return document.MM_returnValue">
					</a>
				
				</td>
			</tr>
			</table>
			
			<br>
			<%=poi.getDescription().replace("\n","<br />")%>
			
		</td>
		<td width="40%" valign="top">
			
			<div style="text-align:justify; padding:5px;">
				Listen to the tracks and select those that in your opinion are <b>suited</b> for
				the described place:
				<!-- Also, mark the tracks that are <b>not suited</b> for the place:  -->
			</div>
			
			<form method="post" action="./?page=main">
				<input type="hidden" id="poi_url_clicked" name="poi_url_clicked" value="0" />
				
				<table border="0" width="100%" cellspacing="0" cellpadding="5">
<%
		for (int trackId: tracksAndMethods.keySet()) {
			
			TrackBean track = new TrackBean(trackId);
    		String musicPath = "http://rerex.inf.unibz.it/tracks/"+track.getFilename();
%>
				<tr>
				<td style="text-align:center; border-bottom: thin solid #696969;">
					<input type="checkbox" name="suggested_music" value="<%=track.getId()%>">
					<input type="hidden" id="musician_url_clicked<%=track.getId()%>" name="musician_url_clicked" value="0" />
				</td>
				
				<td style="text-align:left; border-bottom: thin solid #696969;">
					<b><%=track.getMusician()%> - <%=track.getName()%></b><br>
					<a href="<%=track.getWikiPage()%>" onclick="musicianUrlClicked(<%=track.getId()%>)" target="_blank"><%=track.getWikiPage()%></a>
					<br>
					<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0" width="260" height="60" id="FMP3" align="middle">
						<param name="movie" value="FMP3.swf?mp3=<%=musicPath%>&action=stop&title=<%=track.getName()%>&color=F2F2F2&loop=no&lma=yes&textcolor=000000" />
						<param name="quality" value="high" />
						<param name="bgcolor" value="#F2F2F2" />
						<embed src="FMP3.swf?mp3=<%=musicPath%>&action=stop&title=&color=F2F2F2&loop=no&lma=yes&textcolor=000000" quality="high" bgcolor="#F2F2F2" width="260" height="60" name="FMP3" align="middle" allowScriptAccess="sameDomain" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer" />
					</object>
					<br>
					<!-- 
					Suited <input type="radio" name="music_choice_<%=track.getId()%>" value="good" /> &nbsp;&nbsp;
					Not sure <input type="radio" name="music_choice_<%=track.getId()%>" value="ugly" checked="checked" /> &nbsp;&nbsp;
					Not suited <input type="radio" name="music_choice_<%=track.getId()%>" value="bad" />
					<input type="hidden" name="suggested_music" value="<%=track.getId()%>" />
					 -->
				</td>
				</tr>
<%
		}
%>
				</table>
				
				<div align="center">
					<input type="submit" value="Submit" />
				</div>
			</form>
			
		</td>
	</tr>
	</table>
<%
	
	} else {
		out.println("<br><center>You have completed 10 sessions. Thank you for a great job.<br> "+
					"You are welcome to <a href=\"?repeat=1\">repeat the procedure</a>.</center>");
	}

} else {
	out.println("<br>Please <a href=\"?pg=login\">login</a> before using the system.");
}
%>