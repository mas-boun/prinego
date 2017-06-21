<%@ page import="com.prinego.agent.webservice.util.WsCallUtil" %>
<%@ page import="com.prinego.domain.entity.ontology.postrequest.PostRequest" %>
<%@ page import="com.prinego.domain.entity.ontology.medium.Medium" %>
<%@ page import="com.prinego.util.ExampleCreator" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<html>
<body>

WELCOME TO PRINEGO

<%
    List<PostRequest> p = ExampleCreator.createSimulation(); //you can use different methods here such as executePointBased().
    List<PostRequest> finalizedP = WsCallUtil.callUploadWs(p, new HashSet());
    //WsCallUtil.bestPairwiseWs(p);  //for finding the best outcome pairwise.
    //PostRequest p = ExampleMockUtil.createP_hist();
    //PostRequest finalizedP = WsCallUtil.callUploadWs(p, new HashSet());
  
%>

</body>

</html>
