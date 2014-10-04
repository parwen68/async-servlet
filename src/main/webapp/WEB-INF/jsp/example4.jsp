<%@ page import="java.util.Map" %>
<%
    Map<String,String> result = (Map<String, String>) request.getAttribute("result");

    for (Map.Entry<String, String> entry : result.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
%>
<%=key%> >>> <%=value%> (<%=Thread.currentThread()%>)<br>
<%
    }
%>
