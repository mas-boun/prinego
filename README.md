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

* Examples of usage are shown in *ExampleCreator.java* in the util folder.
* *ExampleCreator* file should be executed for this. 
* *NEGOTIATION_TYPE* variable should be changed to the chosen strategy, which is one of: GEP, MP, RPG, RPM, HybridG, HybridM and Default (UO in the paper). 
* Make sure that you imported the mongodb database dumps as described [here](mongodb/README.md).

## How to run a new example? ## 


