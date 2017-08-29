# PriNego -- Privacy Negotiation in Online Social Networks

This repository is dedicated to the implementation of the work "Preserving Privacy as Social Responsibility in Online Social
Networks" [1].

[1] Dilara Keküllüoğlu , Nadin Kökciyan, and Pınar Yolum. 2018. Preserving Privacy as Social Responsibility in Online Social
Networks. ACM Transactions on Internet Technology (TOIT) (2018). Under Revision.

## What to install? ##

* We use **Java 8** with **Maven** dependencies, deploy the program to **Tomcat 8** server
and use **mongodb** to keep track of points and utility results. You should have these components installed first.
* You can clone or download the zip file to get the source code of our project. 
* You can find missing jars in the libs folder. Put them on the .m2 repository that stores Maven libraries (tentative). You can find the pellet libraries in https://github.com/stardog-union/pellet .
* It may be good to increase the
timeout time of the server if you have a slow machine. After creating the Tomcat server,
double click it, click Open launch configuration. In Class Path’s bootstrap
entries, click Advanced. Tick the Add Classpath Variables, and click OK. In
here select the **M2_Repo**, which is the dependencies used by Maven.
Add the project to the server and then come to the project
properties. Open Deployment Assembly, click Add. Select Java Build Path
and add the Maven dependencies there if it is not there already.
Update project from right-click, Maven, Update Project.
This will update and compile the code again.

## A Walk-Through ##

* ExampleCreator creates the post requests to process.
* WsCallUtil sends the post request to the webservice of the owner agent(initiator).
* AgentWS handles the request and sends it to the upload method of the
BaseAgent_WS.
* BaseAgent_WS’s evaluation method sends the request to negotiation
according to the negotiation type.
* AgentSkeleton’s negotiation method get the post request, collects responses
and revises according to them continuously until the agreement
is reached or the negotiation terminates.
* To collect responses;
  * AgentSkeleton’s negotiation method sends a call to the webservice
  of the negotiator agent.
  * AgentWS gets the call and sends it to the OntologicalAgentBase’s
evaluation method.
  * Evaluation method uses MyOntologyReasoner’s corresponding prepareResponse
method and sends the response.

## How to reproduce the results obtained in the paper? ## 

* Examples of usage are shown in [*ExampleCreator.java*](src/main/java/com/prinego/util/ExampleCreator.java) in the util folder.
* *ExampleCreator* file should be executed for this. 
* *NEGOTIATION_TYPE* variable should be changed to the chosen strategy, which is one of: GEP, MP, RPG, RPM, HybridG, HybridM.
* Make sure that you imported the mongodb database dumps as described [here](mongodb/README.md).

## How to run a new example? ## 

* An ontology that has social media users, their relationship and their privacy rules must be created. We used [Protégé](https://protege.stanford.edu/) for creating the ontology.
* Post requests that has owner, medium and context should be created for negotiation to take place. Medium needs to have only one tagged person on it (We only support 2 people negotiation at this time). 
* You need to create webservices for the agents that will negotiate. You can either create them separately like [Alice_WS](src/main/java/com/prinego/agent/webservice/examples/AgentWS_ALICE.java) or generic agent webservice that will serve for all the agents in the negotiation like [Simulation_WS](src/main/java/com/prinego/agent/webservice/examples/AgentWS_SIMULATION.java).
* Every agent webservice has an agent class that gives specific configurations for that agent. You need to create one for your webservice. For example [Agent_ALICE](src/main/java/com/prinego/agent/Agent_ALICE.java) has the ontology address, utility threshold and importance of points for Alice. You can also have a generic agent like [Agent_SIMULATION](src/main/java/com/prinego/agent/Agent_SIMULATION.java). However if the you use a generic one then you cannot display variability in the configurations of agents. Utility thresholds etc. needs to be the same for all agents.
* If you have all these classes, you can create a method that organizes your post request and send it to the [WSCallUtil](src/main/java/com/prinego/agent/webservice/util/WsCallUtil.java) which makes the webservice calls for the negotiation,
```
List<PostRequest> p = ExampleCreator.<your own method>
List<PostRequest> finalizedP = WsCallUtil.callUploadWs(p, new HashSet());
```
in [index.jsp](src/main/webapp/index.jsp)
