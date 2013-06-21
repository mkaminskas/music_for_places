<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="mfp.Recommender"%>

<%
// default initial page - login
String pg="login";
if (request.getParameter("pg") != null) {
	pg = request.getParameter("pg");
}

// if the user is logged-in (i.e., if sessionRec attribute is set), get the Recommender object  
Recommender sessionRec = null;
if (session.getAttribute("sessionRec") != null) {
	sessionRec = (Recommender)session.getAttribute("sessionRec");
	
	// if the user requested to repeat a session, delete the old, and ceate a new one
	if(request.getParameter("repeat") != null){ 
		int userId = sessionRec.getUserId();
		String userName = sessionRec.getUsername();
		session.removeAttribute("sessionRec");
		session.setAttribute("sessionRec", new Recommender(userId,userName));
		sessionRec = (Recommender)session.getAttribute("sessionRec");
		response.sendRedirect("./");
		return;
	}
	
	// if the user asked for logout, delete the previous record and destroy the Recommender object
	if(request.getParameter("logout") != null){
		sessionRec.deleteCurrentStep();
		session.removeAttribute("sessionRec");
	} else { // if not, increase the stepNr, and display the main page
		sessionRec.increaseStepNum();
		pg = "main";
	}
	
}
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href="mfp.css" rel="stylesheet" type="text/css" />
	<title>Music for a Place</title>
	
<script language="JavaScript">
<!--
function poiUrlClicked(poiId) {
	document.getElementById("poi_url_clicked").value = poiId;
}

function musicianUrlClicked(trackId) {
	document.getElementById("musician_url_clicked"+trackId).value = trackId;
}

function JustSoPicWindow(imageName,imageWidth,imageHeight,alt,bgcolor,hugger,hugMargin) {
	if (bgcolor=="") {
		bgcolor="#FFFFFF";
	}
	var adj=10;
	var w = screen.width;
	var h = screen.height;
	var byFactor=1;

	if(w<740){
	  var lift=0.90;
	}
	if(w>=740 & w<835){
	  var lift=0.91;
	}
	if(w>=835){
	  var lift=0.93;
	}
	if (imageWidth>w){	
	  byFactor = w / imageWidth;			
	  imageWidth = w;
	  imageHeight = imageHeight * byFactor;
	}
	if (imageHeight>h-adj){
	  byFactor = h / imageHeight;
	  imageWidth = (imageWidth * byFactor);
	  imageHeight = h; 
	}
	   
	var scrWidth = w-adj;
	var scrHeight = (h*lift)-adj;

	if (imageHeight>scrHeight){
  	  imageHeight=imageHeight*lift;
	  imageWidth=imageWidth*lift;
	}

	var posLeft=0;
	var posTop=0;

	if (hugger == "hug image"){
	  if (hugMargin == ""){
	    hugMargin = 0;
	  }
	  var scrHeightTemp = imageHeight - 0 + 2*hugMargin;
	  if (scrHeightTemp < scrHeight) {
		scrHeight = scrHeightTemp;
	  } 
	  var scrWidthTemp = imageWidth - 0 + 2*hugMargin;
	  if (scrWidthTemp < scrWidth) {
		scrWidth = scrWidthTemp;
	  }
	  
	  if (scrHeight<100){scrHeight=100;}
	  if (scrWidth<100){scrWidth=100;}

	  posTop =  ((h-(scrHeight/lift)-adj)/2);
	  posLeft = ((w-(scrWidth)-adj)/2);
 	}

	if (imageHeight > (h*lift)-adj || imageWidth > w-adj){
		imageHeight=imageHeight-adj;
		imageWidth=imageWidth-adj;
	}
	posTop = parseInt(posTop);
	posLeft = parseInt(posLeft);		
	scrWidth = parseInt(scrWidth); 
	scrHeight = parseInt(scrHeight);
	
	newWindow = window.open("","newWindow","width="+scrWidth+",height="+scrHeight+",left="+posLeft+",top="+posTop);//vwd_justso.htm
	newWindow.document.open();
	newWindow.document.write('<html><title>'+alt+'</title><body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" bgcolor='+bgcolor+' onBlur="self.close()" onClick="self.close()">');  
	newWindow.document.write('<table width='+imageWidth+' border="0" cellspacing="0" cellpadding="0" align="center" height='+scrHeight+' ><tr><td>');
	newWindow.document.write('<img src="'+imageName+'" width='+imageWidth+' height='+imageHeight+' alt="Click screen to close" >'); 
	newWindow.document.write('</td></tr></table></body></html>');
	newWindow.document.close();
	newWindow.focus();
}
//-->
</script>

</head>
<body>

<table border="0" width="100%" cellpadding="0" cellspacing="0">
<tr>
	<td height="100%">
		&nbsp;<!-- left margin -->
	</td>

	<td width="1000" height="100%" valign="top">

		<table border="0" width="100%" cellpadding="0" cellspacing="5">
		<tr>
			<td style="height: 80px; text-align: right; vertical-align: bottom;">
				<%
				if (session.getAttribute("sessionRec")!=null) {
				
					out.println("Logged in: "+sessionRec.getUsername()+" (<a href=\"?logout=true\">logout</a>)<br>");
					
					if (sessionRec.getStepNum() < 11) {
						out.println("<div>");
						out.println("<span style=\"vertical-align: middle;\"> " +
									"Session "+sessionRec.getStepNum()+" out of 10:</span>");
    					
						for (int i=1; i<11; i++){
							String sessionNr = "1";
							String zz = "";
							if(i > sessionRec.getStepNum()){
								sessionNr = "0";
							}
							if(i>1) {zz=">";}
							out.println(zz+"<img src=\"images/session"+sessionNr+".png\" border=\"0\" width=\"30\" style=\"vertical-align:middle;\"/");
						}
						out.println("></div>");
					}
				}
				%>
			</td>
		</tr>
		
		<tr>
			<td valign="top">
				
				<% if (pg.equals("register")) { %>
					<%@ include file="register.jsp" %>
				<% } else if (pg.equals("login")) { %>
					<%@ include file="login.jsp" %>
				<% } else { %>
					<%@ include file="main.jsp" %>
				<% } %>
				
			</td>
		</tr>
		
		</table>

	</td>

	<td height="100%">
		&nbsp;<!-- right margin -->
	</td>

</tr>
</table>

</body>
</html>