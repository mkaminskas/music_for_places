<%@ page import="mfp.UserInfo" %>
<%@ page import="mfp.Database" %>
<%@ page import="java.util.*" %>

<jsp:useBean id="userInfo" class="mfp.UserInfo" scope="request" />
<%
// If 'add' is true, pass field values to the bean and attempt to add new user
if (request.getParameter("add") != null) {		
%>
<jsp:setProperty name="userInfo" property="*" />
<%
	if (userInfo.addUser()) {	// adding the new user
		response.sendRedirect("?pg=login&registered="+userInfo.getName());
		return;
	}
}
%>
<center>
<h2>User Registration</h2> 
<form method="post" action="?pg=register">
	<table width="365px" border="0" cellpadding="0" cellspacing="5">
		<tr>
			<td width="50%" valign="bottom" align="right">Name:</td>
			<td width="50%">
				<font color=red><%= userInfo.getErrorMsg("name") %></font><br />
				<input type="text" name="name" value='<%= userInfo.getName() %>' />
				<% userInfo.setName(""); %>
			</td>
		</tr>
		<tr>
			<td valign="top" align="right"><br />Select the music genres that you like:</td>
			<td align="left">
				<br />
				<%
					List<String> genres = Database.getGenres();
						for (String genre : genres){
							String checked = "";
							if (userInfo.userLikesGenre(genre)) {
								checked = "checked='checked'";
							}
							out.println("<input type=\"checkbox\" name=\"genres\" value=\""+genre+"\" "+checked+">"+genre+"<br>");
						}
						userInfo.setGenres(new String[0]);
				%>
			</td>
		</tr>
		<tr>
			<td valign="bottom" align="right">Password:</td>
			<td>
				<font color=red><%= userInfo.getErrorMsg("password") %></font><br />
				<input type="password" name="password" value='<%= userInfo.getPassword() %>' />
				<% userInfo.setPassword(""); %>
			</td>
		</tr>
		<tr>
			<td valign="bottom" align="right">Repeat password:</td>
			<td>
				<font color=red><%= userInfo.getErrorMsg("repeatPassword") %></font><br />
				<input type="password" name="repeatPassword" value='<%= userInfo.getRepeatPassword() %>' />
				<% userInfo.setRepeatPassword(""); %>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="right">
				<input type="hidden" name="add" value="true"/>	
				<input type="submit" value=" Ok "/>
			</td>
		</tr>	
	</table>
</form>
<font color=red><%= userInfo.getErrorMsg("add_error") %></font>
<% userInfo.setErrors("add_error",""); %>
</center>