<%@page import="mfp.UserInfo"%>
<%@ page import="mfp.Recommender"%>

<jsp:useBean id="userLogin" class="mfp.UserInfo" scope="request" />

<%
// if the user has just registered, put the username into login form
if (request.getParameter("registered") != null){
	userLogin.setName(request.getParameter("registered"));
	userLogin.setErrors("login_error","Registration successful. You can now login.");
}

// if 'login' is true, pass field values to the bean and login the user
if (request.getParameter("login") != null) {
	%>
	<jsp:setProperty name="userLogin" property="*" />
	<%
	// logging-in
	if (userLogin.loginUser()) {	
		session.setAttribute("sessionRec", new Recommender(userLogin.getId(),userLogin.getName()));
		response.sendRedirect("./");
		return;
	}
}
%>

<center>
<h2>Login</h2>
<form method="post" action="?pg=login">	
	<table width="260px" border="0" cellpadding="0" cellspacing="5">
		<tr>
			<td width="30%" valign="bottom" align="right">Name:</td>
			<td width="70%">
				<font color=red><%= userLogin.getErrorMsg("nameLogin") %></font><br />
				<input type="text" name="name" value='<%= userLogin.getName()%>' />
				<% userLogin.setName(""); %>
			</td>
		</tr>
		<tr>
			<td valign="bottom" align="right">Password:</td>
			<td>
				<font color=red><%= userLogin.getErrorMsg("passwordLogin") %></font><br />
				<input type="password" name="password" value='<%= userLogin.getPassword() %>' />
				<% userLogin.setPassword(""); %>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="right">
				<input type="hidden" name="login" value="true"/>	
				<input type="submit" value=" Ok "/>
			</td>
		</tr>
	</table>
</form>
<font color=red><%= userLogin.getErrorMsg("login_error") %></font>
<% userLogin.setErrors("login_error",""); %>
<br>

<%
if (request.getParameter("registered") == null) {
%>
	New users, please <a href="./?pg=register">register</a>.
<%
}
%>
</center>