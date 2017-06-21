package com.prinego.agent.skeleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Preconditions;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.prinego.agent.skeleton.contracts.IdentityAware;
import com.prinego.agent.skeleton.contracts.Negotiable;
import com.prinego.agent.skeleton.contracts.Negotiator;
import com.prinego.database.handler.MyDatabaseService;
import com.prinego.domain.entity.negotiation.NegotiationIteration;
import com.prinego.domain.entity.negotiation.NegotiationType;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.request.UtilityRequest;
import com.prinego.domain.entity.response.Response;
import com.prinego.domain.entity.response.reason.RejectedField;
import com.prinego.domain.entity.response.reason.RejectionReason;
import com.prinego.util.MySetUtils;
import com.prinego.util.globals.AppGlobals;
import com.prinego.util.json.JsonWriter;

/**
 * Created by mester on 31/08/14.
 */

/**
 * IdentityAware methods:
 *  // changes agent to agent
 *  String getUid()
 *  double getUtilityThreshold()
 *
 * Negotiable methods:
 *  // changes agent to agent
 *  // TODO: the variability must be simulated!
 *  Response evaluate(
 *      PostRequest postRequest)
 *
 * Negotiator methods:
 *  // is uniform throughout the agents
 *  PostRequest negotiate(
 *         PostRequest p,
 *         Set<Medium> altMediums,
 *         List<Set<RejectionReason>> reasons,
 *         int cur,
 *         int max,
 *         boolean isSame)
 *
 * // changes agent to agent
 * // However, since is not a crucial point,
 * // is left uniform throughout the simulated agents
 * Set<Agent> findAgentsToNegotiate(
 *         PostRequest p)
 *
 * // The way of asking can vary agent to agent.
 * // However, since is not a crucial point,
 * // is left uniform throughout the simulated agents
 * Response ask(
 *         PostRequest p,
 *         String agentToAsk)
 *
 * // changes agent to agent
 * // However, since is not a crucial point,
 * // is left uniform throughout the simulated agents
 * String findEndpointOf(
 *         String agent)
 *
 *  // changes agent to agent
 *  // TODO: the variability must be simulated!
 * PostRequest revise(
 *         PostRequest p,
 *         Set<Medium> altMediums,
 *         List<Set<RejectionReason>> reasons,
 *         int cur,
 *         int max)
 *
 */

/**
 * updated by dilara
 * 
 * This is the skeleton of every agent (as the name implies).
 * 
 * The methods used by the initiator agent is implemented here.
 * Initiator agent starts the negotiation, gets responses from the
 * negotiator agent and revises the post request according to different
 * negotiation strategies.
 * 
 * */
public abstract class AgentSkeleton
implements
IdentityAware,
Negotiable,
Negotiator
{

	@Inject
	private MyDatabaseService myDatabaseService;

	/**
	 * This negotiate function is uniform throughout the agents.
	 * This method is only used by the default strategy.
	 *
	 * @param p            :the owner’s post request to be refined and finalized
	 * @param altMediums   :alternative mediums of the owner's choice
	 * @param iterations   :represents the realized iterations
	 * @param cur          :current iteration index
	 * @param max          :allowed max number of negotiation iterations
	 *
	 * @return             :the finalized post request which is ready to publish
	 */
	@Override
	public PostRequest negotiate(
			PostRequest p,
			Set<Medium> altMediums,
			List<NegotiationIteration> iterations,
			int cur,
			int max,
			boolean isSame
			) {

		if (  cur > max || isSame) {
			return p;
		}
		if(p == null ){
			myDatabaseService.writeDemoLog("Couldn't find suitable post", "owner",false);	 
			return null;
		}

		System.out.println("\n" + "Iteration No:" + cur);
		System.out.println("PostRequest:" + p);
		displayAltMediums(altMediums);

		boolean rejected = false;
		Set<Agent> agents = findAgentsToNegotiate(p);
		Set<Response> curResponses = new HashSet();
		for ( Agent agent : agents ) {

			myDatabaseService.writeDemoLog(p.getOwner().getUid()+" sends the request to "+agent.getUid()+" to get consent", "owner",false);

			Response response = ask(p, agent.getUid());
			myDatabaseService.writeDemoLog(response, "negotiator",false);

			curResponses.add(response);

			if ( ! "Y".equals(response.getResponseCode()) ) {
				rejected = true;
			}
		}
		displayCurReponses(curResponses);
		iterations.add(new NegotiationIteration(p, curResponses));

		if ( rejected ) {
			PostRequest co = revise(p, altMediums, iterations, cur, max);
			if(co==null){
				isSame = false;
			}else{

				isSame = p.equals(co);
			}
			myDatabaseService.writeDemoLog(co, "owner",true);

			return negotiate(co, altMediums, iterations, cur + 1, max,isSame);
		} else {
			myDatabaseService.writeDemoLog("Agreement reached. <br> "+p.getOwner().getUid()+" uploades the post", "owner",false);

			return p;
		}

	}

	/**
	 * This negotiate function is uniform throughout the agents. 
	 * This method is for GEP and MP negotiation strategies.
	 *
	 * @param p            the owner’s post request to be refined and finalized
	 * @param altMediums   alternative mediums of the owner's choice
	 * @param iterations   represents the realized iterations
	 * @param agentHistory contains lists for every included agent about their previous responses.
	 * @param cur          current iteration index
	 * @param max          allowed max number of negotiation iterations
	 *
	 * @return             the finalized post request which is ready to publish
	 */
	@Override
	public PostRequest negotiate(
			PostRequest p,
			Set<Medium> altMediums,
			List<NegotiationIteration> iterations,
			Map<String,List<Response>> agentHistory,
			int cur,
			int max,
			boolean isSame,
			NegotiationType negotiationType
			) {

		if(p == null ){
			myDatabaseService.writeDemoLog("Couldn't find suitable post", "owner",false);	 
			return null;
		}
		if(negotiationType.equals(NegotiationType.GEP)){
			// when the post request does not change in the revise step in GEP,
			// it means that we are stuck in this configuration(it will not change in the future steps)
			//so we share lastly offered post request.
			if (  cur > max || isSame) {
				return p;
			}			
		}
		System.out.println("\n" + "Iteration No:" + cur);
		System.out.println("PostRequest:" + p);
		displayAltMediums(altMediums);

		boolean rejected = false;
		Set<Agent> agents = findAgentsToNegotiate(p);
		Set<Response> curResponses = new HashSet();
		for ( Agent agent : agents ) {
			myDatabaseService.writeDemoLog(p.getOwner().getUid()+" sends the request to "+agent.getUid()+" to get consent", "owner",false);


			if(agentHistory.containsKey(agent.getUid())){

				List<Response> newAgentHistory = agentHistory.get(agent.getUid());
				/*Get response using the offered post request, negotiator agent's id and the previous responses of the agent. 
				 Write the returning response to the agentHistory which keeps the responses of negotiator agents.*/
				Response response = ask(p, agent.getUid(),newAgentHistory);
				myDatabaseService.writeDemoLog(response, "negotiator",false);

				curResponses.add(response);
				newAgentHistory.add(response);
				agentHistory.put(agent.getUid(), newAgentHistory);

				if ( ! "Y".equals(response.getResponseCode()) ) {
					rejected = true;
				}
			}else{
				List<Response> newAgentHistory = new ArrayList<Response>();
				Response response = ask(p, agent.getUid(),newAgentHistory);
				myDatabaseService.writeDemoLog(response, "negotiator",false);

				curResponses.add(response);
				newAgentHistory.add(response);
				agentHistory.put(agent.getUid(), newAgentHistory);

				if ( ! "Y".equals(response.getResponseCode()) ) {
					rejected = true;
				}
			}



		}


		displayCurReponses(curResponses);
		iterations.add(new NegotiationIteration(p, curResponses));

		//if the offered post request is rejected;
		if ( rejected ) {
			PostRequest co = null;
			//revise methods changes depending on the negotiation strategy.
			if(negotiationType.equals(NegotiationType.GEP)){
				co = revise(p, altMediums, iterations, cur, max);
			}else{
				co = revise(p, altMediums, iterations,negotiationType, cur, max);
			}

			//it is important to understand if the revise method changed anything or not.
			if(co==null){
				isSame = false;
			}else{

				isSame = p.equals(co);
			}
			myDatabaseService.writeDemoLog(co, "owner",true);
			//send the newly revised post request for evaluation of the negotiator agent again.
			return negotiate(co, altMediums, iterations,agentHistory, cur + 1, max,isSame,negotiationType);
		} else {
			//if the post request was acceptable for negotiator agent.
			myDatabaseService.writeDemoLog("Agreement reached. <br>"+p.getOwner().getUid()+" uploades the post", "owner",false);

			return p;
		}

	}

	/**
	 * This method is inspired by the Such and Rovatsos(2014) paper.
	 * It is a one step negotiation that is between 2 agents. It aims
	 * to find the optimal outcome given a post request by trying
	 * every possible privacy configuration.
	 * 
	 * However since looking at every configuration is costly, this method
	 * chooses the configurations in a smart way. We know which agents are important
	 * for the negotiator agents and the initiator agent regards every audience equally(can change in the future,
	 * hence this method may need to get modified if it happens) so we start with the configuration where
	 * every audience member stays the same. Then we remove people from the audience
	 * based on the importance stated by the negotiator agent.
	 * 
	 * @param p the post request that we need to find the best configuration.
	 * 
	 * @return Best possible post request according to agents' utility functions.
	 * */

	@Override
	public PostRequest oneStepNegotiation(PostRequest p){
		System.out.println("one step nego");
		PostRequest newP = new PostRequest(p);

		// we need to have the audience with fixed indexes since
		//we create configurations by changing audiences one by one.
		//Hence the transition from Set to Array.
		String[] audience = new String[p.getAudience().getAudienceMembers().size()];

		Iterator<Agent> iter = p.getAudience().getAudienceMembers().iterator();
		int counter = 0;
		while(iter.hasNext()){
			audience[counter] = iter.next().getUid();
			counter++;
		}

		//the configuration initiator agent wants.
		int[] posterArray = new int[audience.length];
		for(int i = 0; i<posterArray.length;i++){ 
			posterArray[i] = 1;
		}

		boolean rejected = false;
		Set<Agent> agents = findAgentsToNegotiate(p);
		Set<Response> curResponses = new HashSet();
		String includedPerson = ""; 
		Response r = null;
		for ( Agent agent : agents ) {
			Response response = ask(p, agent.getUid());
			curResponses.add(response);

			if ( ! "Y".equals(response.getResponseCode()) ) {
				rejected = true;
			}
			includedPerson = agent.getUid();
			r = response;
		}
		displayCurReponses(curResponses);

		if(rejected){

			//the configuration that negotiator agent wants.
			int[] includedArray = transformToActionVector(audience,curResponses);
			//the best configuration of audience according to the Scaled Product in our paper.
			//Scaled Product = u_1*u_2*(1-|u_1-u_2|).
			int[] bestOffer = bestProposal(p,audience,includedArray,posterArray,p.getOwner().getUid(),includedPerson,r);
			//set the best audience configuration.
			newP.getAudience().setAudienceMembers(transformFromActionVector(audience,bestOffer));
			return newP;
			
		}else{
			return p;
		}
	}

	/**
	 * This the two negotiations version of the previous method.
	 * We need this to find the optimal solution to pairwise
	 * sharing behavior for agents. 
	 * 
	 * In this method the best
	 * configurations for audiences are found in the case where
	 * a pair shares post requests about each other in-order.
	 * Let's say user_1 and user_2 are in a pair then we look at
	 * the case where user_1 shares a post about user_2 first,
	 * and then user_2 shares a post about user_1. 
	 * 
	 * The result is written directly to the database since we need
	 * it only for evaluation purposes.	 
	 * 
	 * @param p1 the first post request.
	 * @param p2 the second post request
	 * 
	 * 
	 * */
	public void oneStepNegotiation(PostRequest p1,PostRequest p2){

		System.out.println("one step pairwise");

		//fix the ordering.
		String[] audience1 = new String[p1.getAudience().getAudienceMembers().size()];
		String[] audience2 = new String[p2.getAudience().getAudienceMembers().size()];

		Iterator<Agent> iter1 = p1.getAudience().getAudienceMembers().iterator();
		int counter1 = 0;
		while(iter1.hasNext()){
			audience1[counter1] = iter1.next().getUid();
			counter1++;
		}

		Iterator<Agent> iter2 = p2.getAudience().getAudienceMembers().iterator();
		int counter2 = 0;
		while(iter2.hasNext()){
			audience2[counter2] = iter2.next().getUid();
			counter2++;
		}

		//set the initiator agent's audience configuration.
		int[] posterArray1 = new int[audience1.length];
		for(int i = 0; i<posterArray1.length;i++){
			posterArray1[i] = 1;
		}

		int[] posterArray2 = new int[audience2.length];
		for(int i = 0; i<posterArray2.length;i++){
			posterArray2[i] = 1;
		}

		//get responses from the negotiator agents.
		Set<Agent> agents1 = findAgentsToNegotiate(p1);
		Set<Response> curResponses1 = new HashSet();
		String includedPerson = ""; 
		Response r1 = null;
		for ( Agent agent : agents1 ) {
			Response response = ask(p1, agent.getUid());
			curResponses1.add(response);

			includedPerson = agent.getUid();
			r1 = response;
		}

		Set<Agent> agents2 = findAgentsToNegotiate(p2);
		Set<Response> curResponses2 = new HashSet();		
		Response r2 = null;
		for ( Agent agent : agents2 ) {
			Response response = ask(p2, agent.getUid());
			curResponses2.add(response);			
			r2 = response;
		}

		//get the audience configuration of negotiator agents.
		int[] includedArray1 = transformToActionVector(audience1,curResponses1);

		int[] includedArray2 = transformToActionVector(audience2,curResponses2);

		//the utilities of the initiator agent and negotiator agent
		//for every meaningful configuration proposals.
		//the double array has two members. First one is the utility of the initiator agent
		//and the second is the utility of the negotiator agent regarding a proposal.
		List<double[]> first =  allProposals(p1,audience1,includedArray1,posterArray1,p1.getOwner().getUid(),p2.getOwner().getUid(),r1);
		List<double[]> second = allProposals(p2,audience2,includedArray2,posterArray2,p2.getOwner().getUid(),p1.getOwner().getUid(),r2);

		//we need to find the maximum scaled product given the utilities of both parties.
		double max = Double.MIN_VALUE;
		for(int i = 0 ; i<first.size(); i++){ //for every configuration of p1
			double firstUtility = first.get(i)[0]; //initiator agent = user_1
			double secondUtility = first.get(i)[1]; //negotiator agent = user_2
			
			for(int j = 0 ; j<second.size(); j++){ //for every configuration of p2
				double temp1 = second.get(j)[1]+firstUtility; // negotiator agent = user_1
				double temp2 = second.get(j)[0]+secondUtility; //initiator agent = user_2
				temp1/=2; temp2/=2;	//average
				double scaledProduct = (temp1*temp2*(1-Math.abs(temp1-temp2))); //scaled product
				if(max < scaledProduct)
					max = scaledProduct;
			}
		}
		//enter the best possible scaled product for pair of user_1 and user_2 to the database.
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DATABASE_NAME);		 
		MongoCollection<Document> collection = database.getCollection("bestOutcomePairwise");
		collection.insertOne(new Document("first", p1.getOwner().getUid())
		.append("second", p2.getOwner().getUid())
		.append("maxScaledProduct", max));

		mongoClient.close();
	}



	/**
	 * This negotiate function is uniform throughout the agents. 
	 * This method is for RP-based negotiation strategies, RPG and RPM.
	 *
	 * @param p            the owner’s post request to be refined and finalized
	 * @param iterations   represents the realized iterations
	 * @param agentHistory contains lists for every included agent about their previous responses.
	 * @param cur          current iteration index
	 * @param max          allowed max number of negotiation iterations
	 * @param pointOffer   the point offer coupled with the post request p.
	 * 
	 * @return             the finalized post request which is ready to publish
	 */
	@Override
	public PostRequest pointBasedNegotiation(
			PostRequest p,
			List<NegotiationIteration> iterations,
			Map<String,List<Response>> agentHistory,
			int cur,
			int max,
			int pointOffer){



		if (  cur > max ) { //if the maximum negotiation iteration allowed is surpassed;
			if(p.getNegotiationMethod().equals(NegotiationType.RPG)){
				/*RPG uploads the last offered post request. Since the last offered
				  one must be acceptable for the initiator and it is the best offer
				  amongst the previous offers for the negotiator agent because of the
				  nature of RPG.*/
				updatePoints(pointOffer,p);		
				return p;
			}else if(p.getNegotiationMethod().equals(NegotiationType.RPM)){

				/*
				 * RPM finds the first acceptable post request for the initiator agent.
				 * Due to nature of this strategy, the utility of the negotiator agent
				 * gets worse by every iteration. Hence the end result is bad for the
				 * negotiator agent, that is why initiator agent uploads the first post
				 * request that was acceptable for it (threshold was met) to ensure
				 * that negotiator agent is better off. 
				 * */
				HistoryRequest firstAcceptable = findFirstAcceptable(iterations);
				if(firstAcceptable == null)
					return null;
				updatePoints(firstAcceptable.getPointOffer(),firstAcceptable.getP());
				return firstAcceptable.getP();
			}

		}

		if(p==null){
			myDatabaseService.writeDemoLog("Couldn't find suitable post", "owner",false);	    	
			return null;
		}




		boolean rejected = false;
		Set<Agent> agents = findAgentsToNegotiate(p);
		Set<Response> curResponses = new HashSet();
		for ( Agent agent : agents ) {
			myDatabaseService.writeDemoLog(p.getOwner().getUid()+" sends the request to "+agent.getUid()+" to get consent", "owner",false);

			if(agentHistory.containsKey(agent.getUid())){

				List<Response> newAgentHistory = agentHistory.get(agent.getUid());
				Response response = ask(p, agent.getUid(),newAgentHistory,pointOffer);
				myDatabaseService.writeDemoLog(response, "negotiator",false);

				curResponses.add(response);
				newAgentHistory.add(response);
				agentHistory.put(agent.getUid(), newAgentHistory);

				if ( ! "Y".equals(response.getResponseCode()) ) {
					rejected = true;
				}
			}else{
				List<Response> newAgentHistory = new ArrayList<Response>();
				Response response = ask(p, agent.getUid(),newAgentHistory,pointOffer);
				myDatabaseService.writeDemoLog(response, "negotiator",false);

				curResponses.add(response);
				newAgentHistory.add(response);
				agentHistory.put(agent.getUid(), newAgentHistory);

				if ( ! "Y".equals(response.getResponseCode()) ) {
					rejected = true;
				}
			}

		}
		displayCurReponses(curResponses);
		iterations.add(new NegotiationIteration(p, curResponses,pointOffer));

		if ( rejected ) {
			//revised according to the strategy.
			HistoryRequest hr = pointBasedRevise(p,iterations, cur, max);

			//no suitable revision.
			if(hr==null)			
				return pointBasedNegotiation(null, iterations, agentHistory,cur + 1, max,-1);

			myDatabaseService.writeDemoLog(hr, "owner",true);

			System.out.println("revised post request "+hr.getP());			
			return pointBasedNegotiation(hr.getP(), iterations, agentHistory,cur + 1, max,hr.getPointOffer());
		} else {
			myDatabaseService.writeDemoLog("Agreement reached. <br> "+p.getOwner().getUid()+" uploades the post", "owner",false);
			//transaction of points after the agreement is reached.
			updatePoints(pointOffer,p);			
			return p;
		}


	}


	/**
	 * Finds the first acceptable post request offered for the initiator
	 * agent in strategy RPM. 
	 * Due to nature of this strategy, the utility of the negotiator agent
	 * gets worse by every iteration. Hence the end result is bad for the
	 * negotiator agent, that is why initiator agent uploads the first post
	 * request that was acceptable for it (threshold was met) to ensure
	 * that negotiator agent is better off.
	 * */
	private HistoryRequest findFirstAcceptable(
			List<NegotiationIteration> iterations) {
		if(iterations.isEmpty())
			return null;

		List<NegotiationIteration> newList = new ArrayList<>();
		newList.add(iterations.get(0)); // first iteration is the first acceptable
		return pointBasedRevise(newList.get(0).getPostRequest(), newList, 1, 2); // revise according to it.
	}

	/**
	 * This method will update the points of negotiating parties according to
	 * the post request p and the last point offer.
	 * 
	 * @param lastPointOffer the last point offer by the initiator agent.
	 * @param p the post request that is going to be uploaded. 
	 * */
	private void updatePoints(int lastPointOffer,PostRequest p) {
		
		Set<Agent> agents = findAgentsToNegotiate(p);
		for ( Agent agent : agents ) {
			setPoints(p.getOwner().getUid(),agent.getUid(),"INITIATOR",lastPointOffer); //internal method for initiator agent.
			updatePoints(lastPointOffer,agent.getUid(),p); // http call to the web service of the negotiator agent.
		}





	}

	/**
	 * Makes http call to the web service of the negotiator agent for point update.
	 * 
	 * @param lastPointOffer the last point offer by the initiator agent.
	 * @param p the post request that is going to be uploaded
	 * @param agentToAsk the negotiator agent
	 * */
	private void updatePoints(int lastPointOffer,String agentToAsk,PostRequest p) {
		// TODO Auto-generated method stub
		RestTemplate restTemplate = new RestTemplate();

		HistoryRequest hrequest = new HistoryRequest();
		hrequest.setP(p);
		hrequest.setPointOffer(lastPointOffer);

		Map<String, String> vars = new HashMap();
		vars.put("targetUid", agentToAsk);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Content-Type", "application/json");

		//set your entity to send
		HttpEntity<HistoryRequest> requestEntity = new HttpEntity(hrequest, headers);

		ResponseEntity<Response> responseEntity =
				restTemplate.exchange(
						findEndpointForPointsOf(agentToAsk),
						HttpMethod.POST,
						requestEntity,
						Response.class,
						vars
						);


	}

	/**
	 * Non-used method
	 * Finds whether the initiator agent has enough points to offer.
	 * */
	public boolean hasEnoughPoints(
			PostRequest p,
			NegotiationIteration iterations){

		Iterator<Response> iter = iterations.getResponseSet().iterator();
		Response response = new Response();	
		while(iter.hasNext())
			response = iter.next();

		double utility = calculateUtility(p,iterations.getPostRequest());
		double difference = utility*5/getPointWish()-getUtilityThreshold()*5/getPointWish();			

		int myPointOffer = (int)difference ;
		
		if(myPointOffer <= getPoints(p.getOwner().getUid(),response.getOwner())){
			updatePoints(myPointOffer,p);	
			return true;
		}
		return false;
	}
	
	/**
	 * This method is for the hybrid negotiation strategies.
	 * If this method is called, then the GEP or MP that is
	 * used in the hybrid strategy was able to find a solution.
	 * The points should be transferred in an appropriate amount.
	 * It is based on the outcome of the negotiation by GEP or MP. 
	 * 
	 * If the points of the initiator agent is not enough for the
	 * transfer, this method tries to find a post request that initiator
	 * agent can afford.
	 * 
	 * @param p the final outcome from the GEP or MP strategy
	 * @param iterations The first iteration of the negotiation done by the GEP or MP strategy.
	 * 
	 * @return The post request after the points are transferred (adjusted from p if necessary)
	 * */
	public PostRequest GEPMPPLUSSRPNegotiation(
			PostRequest p,
			NegotiationIteration iterations){

		Iterator<Response> iter = iterations.getResponseSet().iterator();
		Response response = new Response();	
		while(iter.hasNext())
			response = iter.next();

		//if the initial postrequest had no rejected audience by the negotiator agent
		if(response.getReason().getRejectedPeoplebyImportance() == null)
			return p;

	
		double utility = calculateUtility(p,iterations.getPostRequest());
		double difference = utility*5/getPointWish()-getUtilityThreshold()*5/getPointWish();			

		int myPointOffer = (int)difference ; //my point offer according to the suggested post request by GEP/MP.

		//if initiator agent has enough points; transfer points, send the same postrequest suggested by GEP/MP.
		if(myPointOffer <= getPoints(p.getOwner().getUid(),response.getOwner())){
			updatePoints(myPointOffer,p);	
			return p;
		}

		//if the initiator agent has not enough points; we try to find an acceptable postrequest.
		List<NegotiationIteration> negoIterations = new ArrayList<>();
		iterations.setPostRequest(p);
		negoIterations.add(iterations);
		List<PostRequest> recommendations = new ArrayList<PostRequest>();
		PostRequest newP = new PostRequest(p);
		
		/*find a new post according to iterations and previous recommendations.
		  recommendations are previously recommended solutions by findANewPost method for post request p.
		  We keep track of the recommendations so we do not recommend them again for this p.*/
		newP = findANewPost(negoIterations,newP,recommendations); 
		/*if there are no other viable solutions than p, then the points stay the same.
		  we do not change the post request.*/
		if(newP == null)
			return p;

		//if there was a viable solution, then we calculate our required point offer.
		utility = calculateUtility(newP,iterations.getPostRequest());
		difference = utility*5/getPointWish()-getUtilityThreshold()*5/getPointWish();			
		myPointOffer = (int)difference ;

		//If points are not enough or the concession made the utility of the initiator agent go below its threshold;
		//(Until the points of the initiator agent is enough and the utility threshold of its is met;)
		while(myPointOffer>getPoints(p.getOwner().getUid(),response.getOwner()) || utility<getUtilityThreshold()){

			System.out.println("points smaller than required");
			//we try to find another viable solution,
			newP = findANewPost(negoIterations,newP,recommendations);
			if(newP==null) //no viable solution again
				return p;
			//calculate the required point offer for the newly suggested post request,
			utility = calculateUtility(newP,iterations.getPostRequest());
			myPointOffer = (int)(utility*10-getUtilityThreshold()*10) ;
			recommendations.add(newP);	//add the newly suggested post request to recommendations.

		}

		//We found a viable alternative to p, update points and return the new post request.
		updatePoints(myPointOffer,newP);	
		return newP;

	}


	/**
	 * This method is used to revise post request according to the responses
	 * of the negotiator agent.
	 * It is used by the point-based negotiation strategies; RPG and RPM.
	 * 
	 * @param p The post request to get revised.
	 * @param iterations list of negotiation iterations between agents.
	 * @param cur current number of iteration.
	 * @param max maximum number of iterations allowed.
	 * 
	 * @return history request that contains the revised post request and the coupled point offer.
	 * 
	 * */
	@Override
	public HistoryRequest pointBasedRevise(PostRequest p,
			List<NegotiationIteration> iterations, int cur, int max) {




		Preconditions.checkNotNull(p);
		Preconditions.checkNotNull(p.getOwner());
		Preconditions.checkNotNull(p.getAudience());
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(p.getAudience().getAudienceMembers()));
		Preconditions.checkNotNull(iterations);
		Preconditions.checkArgument(iterations.size() == cur);
		Preconditions.checkArgument(cur >= 1 && cur <= max);

		NegotiationIteration curIteration = iterations.get(cur-1);
		Preconditions.checkNotNull(curIteration);
		Preconditions.checkNotNull(curIteration.getPostRequest());
		Preconditions.checkNotNull(curIteration.getResponseSet());
		Preconditions.checkNotNull(curIteration.getPointOffer());

		Set<Medium> altMediums = new HashSet();

		// looks if the post request p can be possibly revised.
		if ( shouldAbandonTheRequest(curIteration, altMediums) ) {
			return null;
		}

		Preconditions.checkArgument(
				getIsMediumRejectedInCurIter(curIteration) ||
				getIsPostTextRejectedInCurIter(curIteration) ||
				getIsAudienceRejectedInCurIter(curIteration) ||
				getIsCombinedRejectedInCurIter(curIteration) ||
				getIsPointOfferRejected(curIteration)
				);

		PostRequest newP = new PostRequest(p);
		HistoryRequest toReturn = new HistoryRequest();
		/*the first iteration is used for getting the rejected people by importance in point-based
		  strategies. For more info about negotiation strategies, you can read our paper or my thesis defense
		  presentation.*/
		if(iterations.size() == 1 && p.getNegotiationMethod().equals(NegotiationType.RPG)){
			//do nothing
		}else{
			/*We do not support medium related rejections right now.
			  The mechanism is there however it is hard to find
			  satisfactory solutions to medium rejections so we decided to discard them.*/
			if ( getIsCombinedMediumRejectedInCurIter(curIteration) ) {

				//				List<NegotiationIteration> onlyCurIteration = new ArrayList<NegotiationIteration>();
				//				onlyCurIteration.add(curIteration);
				//				// rejecting person, set of rejected people
				//				Map<String, Set<String>> rejectedPeopleInMedium = getRejectedPeopleInMedium(onlyCurIteration);
				//				Set<String> flattenedRejectedPeople = MySetUtils.flatten(rejectedPeopleInMedium.values());
				//
				//				// rejecting person, set of rejected locations
				//				Map<String, Set<String>> rejectedLocationsInMedium = getRejectedLocationsInMedium(onlyCurIteration);
				//				Set<String> flattenedRejectedLocations = MySetUtils.flatten(rejectedLocationsInMedium.values());
				//
				//				// rejecting person, set of rejected date takens
				//				Map<String, Set<Date>> rejectedDateTakensInMedium = getRejectedDateTakensInMedium(onlyCurIteration);
				//				Set<Date> flattenedDislikedDates = MySetUtils.flatten(rejectedDateTakensInMedium.values());
				//
				//				// rejecting person, set of rejected medium urls that are context disliked
				//				Map<String, Set<String>> contextDislikedMediums = getContextDislikedMediums(onlyCurIteration);
				//				Set<String> flattenedContextDislikedMediums = MySetUtils.flatten(contextDislikedMediums.values());
				//
				//				// rejecting person, set of rejected medium urls that are self disliked
				//				Map<String, Set<String>> selfDislikedMediums = getSelfDislikedMediums(onlyCurIteration);
				//				Set<String> flattenedSelfDislikedMediums = MySetUtils.flatten(selfDislikedMediums.values());
				//
				//				Preconditions.checkArgument(
				//						CollectionUtils.isNotEmpty(flattenedRejectedPeople) ||
				//						CollectionUtils.isNotEmpty(flattenedRejectedLocations) ||
				//						CollectionUtils.isNotEmpty(flattenedDislikedDates) ||
				//						CollectionUtils.isNotEmpty(flattenedContextDislikedMediums) ||
				//						CollectionUtils.isNotEmpty(flattenedSelfDislikedMediums)
				//						);
				//
				//				// also removes the found medium from the altmediums.
				//				Medium foundMedium = findSuitableMedium(altMediums, flattenedRejectedPeople,
				//						flattenedRejectedLocations, flattenedDislikedDates,
				//						flattenedContextDislikedMediums, flattenedSelfDislikedMediums);
				//
				//
				//				newP.setMedium(foundMedium);
				//
				//				System.out.println("new medium is "+foundMedium);
				//				altMediums.add(p.getMedium());
			}
			//same as medium, we do not support post rext related rejections right now.
			
			//if the audience is rejected or it is a combined reason(audience+medium),
			
			if ( getIsCombinedAudienceRejectedInCurIter(curIteration) ) {

				List<NegotiationIteration> onlyCurIteration = new ArrayList<NegotiationIteration>();
				onlyCurIteration.add(curIteration);

				// rejecting person, set of rejected people in this iteration.
				Map<String, Set<String>> rejectedPeopleInAudience = getRejectedPeopleInAudience(onlyCurIteration);

				//discard the rejected people in this iteration.
				discardRejectedAudienceMembers(newP, rejectedPeopleInAudience);
			}

		}
		/* point-based revisions change depending on the underlying strategy; GEP or MP.
		 * In RPG, we remove people from the audience one-by-one based on their importance
		 * to the negotiator agent (starting from most important), while in RPM 
		 * we remove every rejected person from the audience
		 * and then add people one-by-one based on the importance (starting from least important) */
		if(NegotiationType.RPG.equals(p.getNegotiationMethod())){


			//calculateThePointOffer()
			//try to find a common ground between your last offer and their last offer.
			NegotiationIteration lastNegotiation = iterations.get(iterations.size()-1);

			Iterator<Response> iter = lastNegotiation.getResponseSet().iterator();
			if(lastNegotiation.getResponseSet().size()!=1){
				System.out.println("This negotiation is only for 2 people right now");
				return null;
			}
			Response response = new Response();	
			while(iter.hasNext())
				response = iter.next();

			//if this is the first iteration, send the post request as is with 0 point offer.
			if(iterations.size()==1){
				toReturn.setP(newP);
				toReturn.setPointOffer(0);
				return toReturn;
			}
			//otherwise the negotiator must have sent an acceptable point offer for itself.
			//calculate the utility for the case where initiator accepts that offer.
			double totalUtility = calculateUtility(newP,iterations.get(0).getPostRequest()) - response.getPointOffer()*getPointWish()/5;
			
			//if the utility is acceptable and the initiator's points are enough, accept the point offer of negotiator.
			if(totalUtility >= getUtilityThreshold() && getPoints(p.getOwner().getUid(),response.getOwner())>=response.getPointOffer()){
				toReturn.setP(newP);
				toReturn.setPointOffer(response.getPointOffer());
				return toReturn;
			}

			//Otherwise, find an acceptable new post request.
			/*We try to find the negotiable audience so that we can find the next revised post request.
			 *Negotiable audience members are agents that can be removed/added to the
			 *post request to revise it in a meaningful way.
			 *
			 *Previously revised post requests in the negotiation were all acceptable for the initiator agent
			 *so there is no need to change their audience. That is why we are going to concess from the last offered
			 *post request by the initiator agent. 
			 *
			 *In current strategies, negotiator agent does not suggest removal of audience in iterations,
			 *only sends the point offer required for it to accept the offered post request. That is
			 *why the following steps are not really necessary. However for future uses where the
			 *negotiator agent can send audience suggestions, it is helpful to know which agents
			 *removed from the audience while revising the post request (the code above, at the start of the method.)
			 *
			 *This part may come complicated, even I sometimes confuse it. You can remove it until if (newP==null) and
			 *only put newP = findANewPost(iterations) here. That should not break anything. 
			 **/
			//audienceInitiatorDifference is the audience removed in the post request above while revising.
			//these are the audiences that the initiator wants.
			Set<Agent> audienceInitiatorDifference = new HashSet<Agent>(lastNegotiation.getPostRequest().getAudience().getAudienceMembers());
			audienceInitiatorDifference.removeAll(newP.getAudience().getAudienceMembers());

			//if the negotiator wanted initiator to add an agent to the audience.(i.e., instead of another audience)
			//It must be added while revising (not currently in the strategy).
			//audienceIncludedDifference keeps them.
			Set<Agent> audienceIncludedDifference = new HashSet<Agent>(newP.getAudience().getAudienceMembers()); 
			audienceIncludedDifference.removeAll(lastNegotiation.getPostRequest().getAudience().getAudienceMembers());

			if(audienceInitiatorDifference.size()==0 && audienceIncludedDifference.size()==0){ 
				//this means that there are no difference between audiences (which is the only case that can happen right now )
				newP = findANewPost(iterations);

			}else if(audienceIncludedDifference.size()==0){ //the case where negotiator suggests people to be removed from the audience.
				newP = findANewPost(iterations,0); // this case also does not happen since negotiator agent only suggests point offer.
			}else if(audienceInitiatorDifference.size()!=0 && audienceIncludedDifference.size()!=0){
				//the case where it cant happen right now!! it must be enabled for included agent to 
				//send feedback on the postrequest like erase this instead of this.
			}

			//if no new post can be found.
			if(newP == null)
				return null;


			//If there is a possible post request, find if it is acceptable.
			double utility = calculateUtility(newP,iterations.get(0).getPostRequest());
			double difference = utility*5/getPointWish()-getUtilityThreshold()*5/getPointWish();			

			int myPointOffer = (int)difference ;
			List<PostRequest> recommendations = new ArrayList<PostRequest>();
			//Until we can find a new post request that's utility is above the threshold and there are enough points,
			while(myPointOffer>getPoints(p.getOwner().getUid(),response.getOwner()) || utility<getUtilityThreshold()){
				System.out.println("points smaller than required");
				//look at new posts, careful to not send same recommendations.
				newP = findANewPost(iterations,newP,recommendations);
				if(newP==null)
					return null;
				utility = calculateUtility(newP,iterations.get(0).getPostRequest());
				myPointOffer = (int)(utility*10-getUtilityThreshold()*10);
				recommendations.add(newP);
			}
			//We found an acceptable new post request.
			toReturn.setP(newP);
			toReturn.setPointOffer(myPointOffer);
			return toReturn;


		}else if(NegotiationType.RPM.equals(p.getNegotiationMethod())){
			System.out.println("point based mp");
			NegotiationIteration lastNegotiation = iterations.get(iterations.size()-1);

			Iterator<Response> iter = lastNegotiation.getResponseSet().iterator();
			if(lastNegotiation.getResponseSet().size()!=1){
				System.out.println("This negotiation is only for 2 people right now");
				return null;
			}
			Response response = new Response();	
			while(iter.hasNext())
				response = iter.next();


			/*In the first iteration, RPM removes every rejected agent from the audience.(above in discardRejectedAudience etc.)
			 *Then tries to find an acceptable post request by adding those agents one-by-one
			 *to the audience.
			 */
			if(iterations.size()==1){

				double utility = calculateUtility(newP,iterations.get(0).getPostRequest());
				//If removing every rejected agent from the audience is acceptable to the initiator agent,
				//then send the postrequest that has the audience with the problematic people removed and send with point offer 0.
				if(utility >= getUtilityThreshold()){
					toReturn.setP(newP);
					toReturn.setPointOffer(0);
					return toReturn;
				}else{	//otherwise try to find a new post that is acceptable for the initiator agent.
					List<PostRequest> recommendations = new ArrayList<PostRequest>();
					newP = findANewPost(iterations,newP,recommendations); 
					utility = calculateUtility(newP,iterations.get(0).getPostRequest());
					double difference = utility*5/getPointWish()-getUtilityThreshold()*5/getPointWish();			
					int myPointOffer = (int)difference ;


					//Until we can find a new post request that's utility is above the threshold and there are enough points,
					while(myPointOffer>getPoints(p.getOwner().getUid(),response.getOwner()) || utility<getUtilityThreshold()){
						newP = findANewPost(iterations,newP,recommendations); //suggest new posts.
						if(newP==null)
							return null;
						utility = calculateUtility(newP,iterations.get(0).getPostRequest());
						myPointOffer = (int)((calculateUtility(newP,iterations.get(0).getPostRequest())*10-getUtilityThreshold()*10)) ;
						recommendations.add(newP);
					}
					toReturn.setP(newP);
					toReturn.setPointOffer(myPointOffer);
					return toReturn;
				}
			}
			
			//If this is not the first iteration, then find a new post as above.
			double totalUtility = calculateUtility(newP,iterations.get(0).getPostRequest()) - response.getPointOffer()*getPointWish()/5;
			if(totalUtility >= getUtilityThreshold() && getPoints(p.getOwner().getUid(),response.getOwner())>=response.getPointOffer()){
				toReturn.setP(newP);
				toReturn.setPointOffer(response.getPointOffer());
				return toReturn;
			}

			newP = findANewPost(iterations);

			if(newP == null)
				return null;


			double utility = calculateUtility(newP,iterations.get(0).getPostRequest());
			double difference = utility*5/getPointWish()-getUtilityThreshold()*5/getPointWish();			

			int myPointOffer = (int)difference ;
			
			List<PostRequest> recommendations = new ArrayList<PostRequest>();
			while(myPointOffer>getPoints(p.getOwner().getUid(),response.getOwner()) || utility<getUtilityThreshold()){
				System.out.println("points smaller than required");
				newP = findANewPost(iterations,newP,recommendations);
				if(newP==null)
					return null;
				utility = calculateUtility(newP,iterations.get(0).getPostRequest());
				myPointOffer = (int)(utility*10-getUtilityThreshold()*10) ;
				recommendations.add(newP);
			}
			toReturn.setP(newP);
			toReturn.setPointOffer(myPointOffer);
			return toReturn;
		}



		return null;

	}






	/**
	 * This method finds a new post that is acceptable by the initiator agent
	 * in a meaningful way. Meaningful as in there is a concession by at least one agent
	 * so that the negotiations are moving forward. Also it is ensured that previously
	 * suggested post requests are not suggested again.
	 * 
	 * @param iterations The list of iterations that is done between agents
	 * @param newP the post request that is going to get changed.
	 * @param recommendations the previously suggested changes to the post request.
	 * 
	 * @return A new post request that is acceptable by the initiator agent.
	 * */
	private PostRequest findANewPost(List<NegotiationIteration> iterations,
			PostRequest newP, List<PostRequest> recommendations) {

		Response response = new Response();
		Iterator<Response> iteration = iterations.get(0).getResponseSet().iterator();
		while(iteration.hasNext())
			response = iteration.next();

		//the agents included agents wants to get removed from the first post request that started the negotiation.
		List<String> audienceWish = response.getReason().getRejectedPeoplebyImportance();
		System.out.println("size of audience wish "+audienceWish.size());
		
		List<Agent> audienceAgentWish = new ArrayList<Agent>();
		//the post request that came to revise function to get revised.(not previously suggested newP's)
		PostRequest originalPR = new PostRequest(iterations.get(iterations.size()-1).getPostRequest());

		//In the case of RPG.
		if(originalPR.getNegotiationMethod().equals(NegotiationType.RPG)){
			for(int i= 0; i<audienceWish.size();i++){
				Iterator<Agent> iteration2 = iterations.get(iterations.size()-1).getPostRequest().getAudience().getAudienceMembers().iterator();
				while(iteration2.hasNext()){
					Agent a = iteration2.next();
					if(audienceWish.get(i).equalsIgnoreCase(a.getUid()))
						audienceAgentWish.add(a);
				}
			}

			/* audienceAgentWish keeps the agents that negotiator agent wanted to be removed (in order of importance) 
			 * and they are still not removed in the current iteration.*/

			//no objections
			if(audienceWish.size()==0)
				return null;


			boolean found = false; //indicates whether a unique post request is found or not.
			for(int i = 0;i<audienceAgentWish.size();i++){

				//remove agents from audience according to their importance to the negotiator agent.
				originalPR.getAudience().getAudienceMembers().remove(audienceAgentWish.get(i));
				if(isOriginal(originalPR,iterations,recommendations)){ //if it is not previously recommended.
					found = true;
					break;
				}

			}
			if(found)
				return originalPR;
			return null; //no unique post request
		}

		//In the case of RPM.
		if(originalPR.getNegotiationMethod().equals(NegotiationType.RPM)){

			if(iterations.size()==1){
				originalPR =  new PostRequest(newP);
				for(int i= 0; i<audienceWish.size();i++){
					/*every rejected agent is already removed, so possible agents to add are
					 *all of the rejected agents.*/
					Set<Agent> possibleAudience = iterations.get(0).getPostRequest().getAudience().getAudienceMembers();
					Iterator<Agent> iteration2 = possibleAudience.iterator();					
					while(iteration2.hasNext()){
						Agent a = iteration2.next();
						if(audienceWish.get(i).equalsIgnoreCase(a.getUid()))
							audienceAgentWish.add(a);

					}

				}

			}else{

				for(int i= 0; i<audienceWish.size();i++){
					/*The possible agents that can be added to the audience to find a unique post request are
					 * the last post request's audience that was sent to negotiator agent removed from the
					 * orignally rejected agents. (This is to make the program quicker. We already know the previously
					 * offered post request was acceptable for the initiator so we make the additions from there.) 
					 * */
					Set<Agent> possibleAudience = iterations.get(0).getPostRequest().getAudience().getAudienceMembers();
					possibleAudience.removeAll(iterations.get(iterations.size()-1).getPostRequest().getAudience().getAudienceMembers());
					Iterator<Agent> iteration2 = possibleAudience.iterator();

					while(iteration2.hasNext()){
						Agent a = iteration2.next();
						if(audienceWish.get(i).equalsIgnoreCase(a.getUid()))
							audienceAgentWish.add(a);

					}

				}				

			}

			/* audienceAgentWish keeps the agents that negotiator agent wanted to be removed (in order of importance) 
			 * and they are still not removed in the current iteration.*/
			System.out.println("size of audience wish "+audienceWish.size());
			if(audienceWish.size()==0)
				return null;


			boolean found = false;
			for(int i = audienceAgentWish.size()-1; i >= 0; i--){
				originalPR.getAudience().getAudienceMembers().add(audienceAgentWish.get(i));//add from the least important.
				if(isOriginal(originalPR,iterations,recommendations)){
					found = true;
					break;
				}

			}
			if(found)
				return originalPR;
			return null;
		}

		/*in the cases of hybrid negotiation strategies, we have an acceptable post request
		 *by both parties already but the initiator agent has not enough points for it.
		 *So we remove some unwanted agents from the audience in hopes of a post request that initiator
		 *can afford. 
		 */
		if(originalPR.getNegotiationMethod().equals(NegotiationType.HybridG) ||
				originalPR.getNegotiationMethod().equals(NegotiationType.HybridM)){


			originalPR =  new PostRequest(newP);
			for(int i= 0; i<audienceWish.size();i++){
				Set<Agent> possibleAudience = iterations.get(0).getPostRequest().getAudience().getAudienceMembers();
				Iterator<Agent> iteration2 = possibleAudience.iterator();					
				while(iteration2.hasNext()){
					Agent a = iteration2.next();
					if(audienceWish.get(i).equalsIgnoreCase(a.getUid()))
						audienceAgentWish.add(a);

				}

			}

			System.out.println("size of audience wish "+audienceWish.size());
			if(audienceWish.size()==0)
				return null;

			boolean found = false;
			for(int i = 0;i<audienceAgentWish.size();i++){

				originalPR.getAudience().getAudienceMembers().remove(audienceAgentWish.get(i));
				if(isOriginal(originalPR,iterations,recommendations)){
					found = true;
					break;
				}

			}
			if(found)
				return originalPR;
			return null;


		}

		return null;

	}

	//not used right now
	private PostRequest findANewPost(List<NegotiationIteration> iterations,
			int mode) {

		//		List<NegotiationIteration> onlyCurIteration = new ArrayList<NegotiationIteration>();
		//		onlyCurIteration.add(iterations.get(0));
		//
		//		Map<String, Set<String>> rejectedPeopleInAudience = getRejectedPeopleInAudience(onlyCurIteration);
		//		PostRequest includedAgentWish = new PostRequest(iterations.get(0).getPostRequest());
		//		discardRejectedAudienceMembers(includedAgentWish, rejectedPeopleInAudience);
		//
		//		Set<Agent> audienceWish = new HashSet<Agent>(iterations.get(iterations.size()-1).getPostRequest().getAudience().getAudienceMembers());
		//		audienceWish.removeAll(includedAgentWish.getAudience().getAudienceMembers());

		Response response = new Response();
		Iterator<Response> iteration = iterations.get(0).getResponseSet().iterator();
		while(iteration.hasNext())
			response = iteration.next();

		//the agents included agents wants to get removed.
		List<String> audienceWish = response.getReason().getRejectedPeoplebyImportance();
		List<Agent> audienceAgentWish = new ArrayList<Agent>();

		for(int i= 0; i<audienceWish.size();i++){
			Iterator<Agent> iteration2 = iterations.get(iterations.size()-1).getPostRequest().getAudience().getAudienceMembers().iterator();
			while(iteration2.hasNext()){
				Agent a = iteration2.next();
				if(audienceWish.get(i).equalsIgnoreCase(a.getUid()))
					audienceAgentWish.add(a);
			}
			//if(iterations.get(iterations.size()-1).getPostRequest().getAudience().getAudienceMembers().contains(new Agent(audienceWish.get(i))));
		}


		PostRequest originalPR = new PostRequest(iterations.get(0).getPostRequest());

		//		for(Agent agent:audienceWish){
		//			if(agent.getUid().equals(iterations.get(iterations.size()-1))){
		//				originalPR.getAudience().getAudienceMembers().remove(agent);
		//				if(isOriginal(originalPR,iterations,new ArrayList()))
		//					return originalPR;
		//			}
		//		}

		originalPR = new PostRequest(iterations.get(iterations.size()-1).getPostRequest());
		if(audienceWish.size()==0)
			return null;


		//		Iterator<Agent> iter = audienceWish.iterator();
		//
		//		boolean notFound = true;
		//		while(iter.hasNext() && notFound ){
		//			Agent temp = iter.next();
		//			originalPR.getAudience().getAudienceMembers().remove(temp);
		//			if(isOriginal(originalPR,iterations,new ArrayList()))
		//				notFound = false;
		//			if(notFound)
		//				originalPR.getAudience().getAudienceMembers().add(temp);
		//			iter.remove();
		//		}
		//
		//		if(notFound){
		//			return null;
		//		}

		for(int i = 0;i<audienceAgentWish.size();i++){
			originalPR.getAudience().getAudienceMembers().remove(audienceAgentWish.get(i));
			if(isOriginal(originalPR,iterations,new ArrayList()))
				break;
		}

		return originalPR;
	}

	/**
	 * First findANewPost that is called when there are no previous suggestions to check from.
	 * It has the same logic as the previously explained one.
	 * 
	 * @param iterations list of previous negotiation iterations
	 * 
	 * @return a unique post request revised in a smart way.
	 * */
	private PostRequest findANewPost(List<NegotiationIteration> iterations) {


		Response response = new Response();
		Iterator<Response> iteration = iterations.get(0).getResponseSet().iterator();
		while(iteration.hasNext())
			response = iteration.next();


		List<String> audienceWish = response.getReason().getRejectedPeoplebyImportance();
		List<Agent> audienceAgentWish = new ArrayList<Agent>();
		System.out.println("audience wish size "+audienceWish.size());
		for(int i= 0; i<audienceWish.size();i++){ // ordered people of the audience
			Iterator<Agent> iteration2 = iterations.get(0).getPostRequest().getAudience().getAudienceMembers().iterator();
			String agent = audienceWish.get(i);
			System.out.println(agent);
			while(iteration2.hasNext()){
				Agent a = iteration2.next();
				if(agent.equalsIgnoreCase(a.getUid()))
					audienceAgentWish.add(a);
			}
		}

		PostRequest originalPR = new PostRequest(iterations.get(iterations.size()-1).getPostRequest());
		System.out.println("audience wish size "+audienceAgentWish.size());
		if(audienceAgentWish.size()==0)
			return null;

		if(originalPR.getNegotiationMethod().equals(NegotiationType.RPG)){

			for(int i = 0;i<audienceAgentWish.size();i++){
				originalPR.getAudience().getAudienceMembers().remove(audienceAgentWish.get(i));
				if(isOriginal(originalPR,iterations,new ArrayList()))
					break;
			}
		}

		if(originalPR.getNegotiationMethod().equals(NegotiationType.RPM)){

			for(int i = audienceAgentWish.size()-1; i>=0; i--){
				originalPR.getAudience().getAudienceMembers().add(audienceAgentWish.get(i));
				if(isOriginal(originalPR,iterations,new ArrayList()))
					break;
			}
		}		


		return originalPR;


	}



	/**
	 * Checks whether the suggested post request is previouslty suggested or not.
	 * 
	 * @param originalPR the post request to get checked
	 * @param iterations The list of previous negotiation iterations.
	 * @param previousPostsRecommended the list of post requests that are suggested before.
	 * 
	 * @return boolean Whether the originalPR is original or not.
	 * */
	private boolean isOriginal(PostRequest originalPR,
			List<NegotiationIteration> iterations,List<PostRequest> previousPostsRecommended) {
		System.out.println("is it original ");
		for(int i = 0; i<iterations.size(); i++){
			if(iterations.get(i).getPostRequest().equals(originalPR) )
				return false;

			List<NegotiationIteration> onlyCurIteration = new ArrayList<NegotiationIteration>();
			onlyCurIteration.add(iterations.get(i));

			Map<String, Set<String>> rejectedPeopleInAudience = getRejectedPeopleInAudience(onlyCurIteration);
			PostRequest includedAgentWish = new PostRequest(iterations.get(i).getPostRequest());
			discardRejectedAudienceMembers(includedAgentWish, rejectedPeopleInAudience);
			if(includedAgentWish.equals(originalPR) && i>0)
				return false;

		}
		if(previousPostsRecommended.contains(originalPR))
			return false;
		System.out.println("yep!it is!");
		return true;
	}

	/**
	 * Given the desires of both negotiating agents, finds the best outcome in terms of
	 * Scaled Product(SP).
	 * 
	 * @param initialP the post request that is to be negotiated
	 * @param audience the names of the audience of initialP
	 * @param includedArray the action vector of negotiator's wishes.
	 * @param posterArray   the action vector of initiator's wishes.
	 * @param owner the agent id of the initiator
	 * @param includedPerson the agent id of the negotiator
	 * @param r response of the negotiator agent to the initialP
	 * 
	 * @return an action vector that defines the audience of the best outcome.
	 * */
	private int[] bestProposal(PostRequest initialP, String[] audience, int[] includedArray,
			int[] posterArray, String owner, String includedPerson, Response r) {

		/*taken from the Such's paper. Have the conflicted agents as '*'
		 *conflicted agents as in initiator and negotiator has different wishes regarding them.
		 *in action vectors 1 means 'show' and 0 means 'no show'.*/
		char[] tentative = new char[includedArray.length];

		for(int i = 0; i<includedArray.length;i++){
			if(includedArray[i]!=posterArray[i]){
				tentative[i]='*';
			}else if( includedArray[i]==1){
				tentative[i]= '1';
			}else{
				tentative[i] = '0';
			}						
		}
		//Possibilities that the final action vector can be.(in a meaningful way, not every possible action vector)
		//we find the maximum possible scaled product of utilities.
		List<char[]> possibilities = createLists(tentative,audience,r);
		double max = calculateScaledProduct(initialP,audience,possibilities.get(0),owner,includedPerson);		
		char[] maxProposal =possibilities.get(0);
		for(int i = 1; i<possibilities.size();i++){

			double temp = calculateScaledProduct(initialP,audience,possibilities.get(i),owner,includedPerson);			
			if(max < temp){
				max = temp;
				maxProposal = possibilities.get(i);
			}
		}
		int[] toReturn = new int[maxProposal.length];
		
		for(int i =0;i<maxProposal.length;i++){
			System.out.print(audience[i]+" ");//for logging purposes
			if(maxProposal[i]=='1'){
				System.out.println("1");
				toReturn[i] = 1;
			}else{
				System.out.println("0");
				toReturn[i] = 0;
			}

		}

		return toReturn;

	}

	/**
	 * Sends the utilities for both negotiating agents for every meaningful configuration of the audience.
	 * Meaningful configuration as in the importance of people regarding to the negotiator
	 * agent is considered. Otherwise looking at every possible configuration would take unnecessarily long time.
	 * This method is used to find the best outcome for the in-order sharing pattern.
	 * 
	 * @param initialP the post request that is to be negotiated
	 * @param audience the names of the audience of initialP
	 * @param includedArray the action vector of negotiator's wishes.
	 * @param posterArray   the action vector of initiator's wishes.
	 * @param owner the agent id of the initiator
	 * @param includedPerson the agent id of the negotiator
	 * @param r response of the negotiator agent to the initialP
	 * 
	 * @return a list of utilities for possible configurations.
	 * 
	 * */
	private List<double[]> allProposals(PostRequest initialP, String[] audience, int[] includedArray,
			int[] posterArray, String owner, String includedPerson, Response r) {

		List<double[]> toReturn = new ArrayList<double[]>();
		char[] tentative = new char[includedArray.length];

		for(int i = 0; i<includedArray.length;i++){
			if(includedArray[i]!=posterArray[i]){
				tentative[i]='*';
			}else if( includedArray[i]==1){
				tentative[i]= '1';
			}else{
				tentative[i] = '0';
			}						
		}
		List<char[]> possibilities = createLists(tentative,audience,r);

		for(int i = 0; i<possibilities.size();i++){

			double[] temp = calculateBothUtilities(initialP,audience,possibilities.get(i),owner,includedPerson);			
			toReturn.add(temp);
		}


		return toReturn;

	}

	

	/**
	 * Given an array of 0, 1, and *'s, returns a list of action vectors that
	 * array can be.(by changing *'s to 1 or 0). Creating every possible
	 * action vectors would exponential regarding the size of the * (conflicted agents).O(2^k)
	 * That is why we create lists in a smart way, by making the initiator agent to compromise gradually.
	 * We start with the action vector of all 1's and start changing 1's to 0's
	 * starting from the most important person accroding to the negotiator agent.
	 * So the returning list has the size of O(k) and we did not lose
	 * any meaningful audience configuration.
	 * 
	 * @param tentative the array that has the partial action vector of the audience, *'s in place of conflictions
	 * between initiator and negotiator.
	 * @param audience the ordered names of the audience synced with the tentative.
	 * @param r response of the negotiator agent to get the importance order of conflicted audiences.
	 * 
	 * @return list of action vectors that can be created from the tentative in a meaningful way.
	 * 
	 * */
	private List<char[]> createLists(char[] tentative, String[] audience,
			Response r) {

		List<char[]> toReturn = new ArrayList<char[]>();

		//firstly every confliction is in the action vector
		for(int i = 0; i<tentative.length;i++){
			if(tentative[i] == '*')
				tentative[i] = '1';
		}
		toReturn.add(tentative.clone());

		/*starting from the most important person, change 1 to 0 in the action vector
		 *and add it to the list. Continue to do this until every rejected agent
		 *has 0 in the action vector.*/
		for(int i = 0 ; i<r.getReason().getRejectedPeoplebyImportance().size(); i++){
			String person = r.getReason().getRejectedPeoplebyImportance().get(i);
			int j=0;
			for(; j<audience.length; j++){
				if(audience[j].equals(person))
					break;
			}
			tentative[j] = '0';
			toReturn.add(tentative.clone());
		}		
		System.out.println("all lists size "+toReturn.size());
		return toReturn;

	}

	/**
	 * Given the post request and an action vector, return the scaled product of
	 * utilities with both agent's utilities.
	 * 
	 * @param initialP the post request that is to be negotiated
	 * @param audience agent id's of the audience
	 * @param cs the action vector of the audience in sync with audience
	 * @param owner the initiator agent
	 * @param includedPerson the negotiator agent
	 * 
	 * @return the scaled product of utilities for negotiating agents
	 * */
	private double calculateScaledProduct(PostRequest initialP, String[] audience, char[] cs,
			String owner, String includedPerson) {

		Set<String> removedPeople = new HashSet<String>();
		for(int i=0; i<cs.length; i++){
			if(cs[i]=='0'){
				removedPeople.add(audience[i]);
			}
		}
		//the post request that the action vector cs represents the audience.
		PostRequest finalizedP = new PostRequest(initialP);
		Iterator<Agent> iter = finalizedP.getAudience().getAudienceMembers().iterator();
		while(iter.hasNext()){
			Agent a = iter.next();
			if(removedPeople.contains(a.getUid()))
				iter.remove();
		}

		//initiator agent's utility, can be calculated with internal method
		double ownerUtility = calculateUtility(finalizedP,initialP);
		System.out.println("owner u "+ownerUtility);

		UtilityRequest utilityRequest = new UtilityRequest();
		utilityRequest.setInitialP(initialP);
		utilityRequest.setFinalizedP(finalizedP);
		utilityRequest.setRole("negotiator");
		utilityRequest.setPostIndex(0);
		String utilityRequestJson = null;

		try {
			utilityRequestJson = JsonWriter.objectToJson(utilityRequest);						
		} catch (IOException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();

		Map<String, String> vars = new HashMap();
		vars.put("requesterUid", initialP.getOwner().getUid());
		vars.put("targetUid", includedPerson);


		//set your headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		//set your entity to send
		HttpEntity requestEntity = new HttpEntity(utilityRequestJson, headers);

		ResponseEntity<Double> responseEntity =
				restTemplate.exchange(
						findEndpointForUtilityOf(includedPerson),
						HttpMethod.POST,
						requestEntity,
						Double.class,
						vars
						);

		//negotiator's utility, need to call the web service of the agent to get it.
		double includedUtility = responseEntity.getBody();

		System.out.println("included u "+includedUtility);
		double scaledProduct = (ownerUtility*includedUtility*(1-Math.abs(ownerUtility-includedUtility)));
		System.out.println("scaled p u "+scaledProduct);
		return scaledProduct;
	}

	/**
	 * Given the post request and an action vector, return the utilities
	 * of both agents.
	 * The workings of it is same with the calculateScaledProduct(..) method.
	 * 
	 * @param initialP the post request that is to be negotiated
	 * @param audience agent id's of the audience
	 * @param cs the action vector of the audience in sync with audience
	 * @param owner the initiator agent
	 * @param includedPerson the negotiator agent
	 * 
	 * @return the utilities of negotiating agents
	 * */
	private double[] calculateBothUtilities(PostRequest initialP, String[] audience, char[] cs,
			String owner, String includedPerson) {

		Set<String> removedPeople = new HashSet<String>();
		for(int i=0; i<cs.length; i++){
			if(cs[i]=='0'){
				removedPeople.add(audience[i]);
			}
		}
		PostRequest finalizedP = new PostRequest(initialP);
		Iterator<Agent> iter = finalizedP.getAudience().getAudienceMembers().iterator();
		while(iter.hasNext()){
			Agent a = iter.next();
			if(removedPeople.contains(a.getUid()))
				iter.remove();
		}

		double ownerUtility = calculateUtility(finalizedP,initialP);
		System.out.println("owner u "+ownerUtility);

		UtilityRequest utilityRequest = new UtilityRequest();
		utilityRequest.setInitialP(initialP);
		utilityRequest.setFinalizedP(finalizedP);
		utilityRequest.setRole("negotiator");
		utilityRequest.setPostIndex(0);
		String utilityRequestJson = null;

		try {
			utilityRequestJson = JsonWriter.objectToJson(utilityRequest);						
		} catch (IOException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();

		Map<String, String> vars = new HashMap();
		vars.put("requesterUid", initialP.getOwner().getUid());
		vars.put("targetUid", includedPerson);


		//set your headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		//set your entity to send
		HttpEntity requestEntity = new HttpEntity(utilityRequestJson, headers);

		ResponseEntity<Double> responseEntity =
				restTemplate.exchange(
						findEndpointForUtilityOf(includedPerson),
						HttpMethod.POST,
						requestEntity,
						Double.class,
						vars
						);

		double includedUtility = responseEntity.getBody();

		System.out.println("included u "+includedUtility);

		double[] toReturn = new double[2];
		toReturn[0] = ownerUtility; toReturn[1] = includedUtility;
		return toReturn;
	}


	/**
	 * Given an array of strings indicating agent names and an int array
	 * representing an action vector, returns the set of agents that are
	 * in the audience.
	 * 
	 * @param audience agent id's.
	 * @param proposal the action vector indicating 'show' or 'no show' for agents in sync with String array audience.
	 * */
	private Set<Agent> transformFromActionVector(String[] audience,
			int[] proposal) {

		Set<Agent> toReturn = new HashSet<Agent>();
		for(int i = 0; i<proposal.length;i++){
			if(proposal[i]==1){
				toReturn.add(new Agent(audience[i]));
			}
		}

		return toReturn;
	}
	
	/**
	 * This one does the opposite of the previous method.
	 * Given an array of String indicating audience and the response
	 * of the negotiator agent, returns the action vector desired.
	 * 
	 * @param audience agent id's
	 * @param curResponses the response of the negotiator
	 * 
	 * @return the action vector according to wishes of the negotiator agent.
	 * */

	private int[] transformToActionVector(String[] audience,
			Set<Response> curResponses) {

		int[] toReturn = new int[audience.length];
		Iterator<Response> iter = curResponses.iterator();
		while(iter.hasNext()){// there is one response since only one negotiator.
			Response response = iter.next();
			for(int i = 0; i<audience.length;i++){
				if(response.getReason().getIncludedPeople().contains(audience[i])){//rejected person
					toReturn[i] = 0; //'no show'
				}else{
					toReturn[i] = 1; //'show'
				}
			}
		}

		return toReturn;
	}


	private void displayAltMediums(Set<Medium> altMediums) {
		if ( CollectionUtils.isNotEmpty(altMediums) ) {
			System.out.println("The alternative mediums are:");
			for ( Medium aMedium : altMediums ) {
				System.out.println(aMedium.toString());
			}
		}
	}

	private void displayCurReponses(Set<Response> curResponses) {		

		System.out.println(curResponses.size() + " responses:");

		for ( Response response : curResponses ) {
			System.out.println(response);
		}

	}

	/**
	 * The logic in this findAgentsToNegotiate function can vary from agent to agent.
	 * A picky agent may choose to negotiate with every agent related to the post request,
	 * whereas a more relaxed agent may skip closed friends.
	 * TODO: the variability must be simulated!
	 *
	 * @param p :the post request to be negotiated with other agents
	 *
	 * @return  :the agents to negotiate
	 */
	@Override
	public Set<Agent> findAgentsToNegotiate(
			PostRequest p
			) {

		Medium medium = p.getMedium();

		Preconditions.checkArgument( medium != null);

		Set<Agent> agents = new HashSet();

		if ( medium != null ) {
			Set<Agent> includedPeople = medium.getIncludedPeople();
			agents.addAll(includedPeople);
		}

		return agents;
	}



	/**
	 * The way of asking can vary agent to agent.
	 * However, since is not a crucial point,
	 * is left uniform throughout the simulated agents
	 *
	 * @param p         :the post request to be asked to another agent
	 * @param agentToAsk:the agent to be asked about the post request
	 *
	 * @return          :the asked agent's response about the post request
	 */
	@Override
	public Response ask(
			PostRequest p,
			String agentToAsk
			) {

		RestTemplate restTemplate = new RestTemplate();

		Map<String, String> vars = new HashMap();
		vars.put("requesterUid", p.getOwner().getUid());
		vars.put("targetUid", agentToAsk);

		//set your headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		//set your entity to send
		HttpEntity requestEntity = new HttpEntity(p, headers);

		ResponseEntity<Response> responseEntity =
				restTemplate.exchange(
						findEndpointOf(agentToAsk),
						HttpMethod.POST,
						requestEntity,
						Response.class,
						vars
						);
		Response response = responseEntity.getBody();

		return response;
	}

	/**
	 * Basically same functionality as above, however for GEP and MP.
	 * 
	 * @param p the post request to be asked to another agent
	 * @param agentToAsk the agent to be asked about the post request
	 * @param agentResponses previous responses of the agent agentToAsk in the negotiation
	 *
	 * @return the asked agent's response about the post request
	 * */
	@Override
	public Response ask(
			PostRequest p,
			String agentToAsk,
			List<Response> agentResponses
			) {

		RestTemplate restTemplate = new RestTemplate();
	
		HistoryRequest hrequest = new HistoryRequest();
		hrequest.setP(p);
		hrequest.setAgentResponses(agentResponses);

		Map<String, String> vars = new HashMap();
		vars.put("requesterUid", p.getOwner().getUid());
		vars.put("targetUid", agentToAsk);


		//set your headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		//set your entity to send
		HttpEntity requestEntity = new HttpEntity(hrequest, headers);

		ResponseEntity<Response> responseEntity =
				restTemplate.exchange(
						findEndpointForHistoryOf(agentToAsk),
						HttpMethod.POST,
						requestEntity,
						Response.class,
						vars
						);
		Response response = responseEntity.getBody();

		System.out.println("is there a response "+response);
		return response;
	}


	/**
	 * Basically same functionality as above, however for point-based
	 * strategies.
	 * 
	 * @param p the post request to be asked to another agent
	 * @param agentToAsk the agent to be asked about the post request
	 * @param agentResponses previous responses of the agent agentToAsk in the negotiation
	 * @param pointOffer the point offered to negotiator agent for p
	 *
	 * @return the asked agent's response about the post request
	 * */
	@Override
	public Response ask(
			PostRequest p, 
			String agentToAsk,
			List<Response> agentResponses, 
			int pointOffer) {

		RestTemplate restTemplate = new RestTemplate();

		HistoryRequest hrequest = new HistoryRequest();
		hrequest.setP(p);
		hrequest.setAgentResponses(agentResponses);
		hrequest.setPointOffer(pointOffer);

		Map<String, String> vars = new HashMap();
		vars.put("requesterUid", p.getOwner().getUid());
		vars.put("targetUid", agentToAsk);


		//set your headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		//set your entity to send
		HttpEntity requestEntity = new HttpEntity(hrequest, headers);

		ResponseEntity<Response> responseEntity =
				restTemplate.exchange(
						findEndpointForHistoryOf(agentToAsk),
						HttpMethod.POST,
						requestEntity,
						Response.class,
						vars
						);
		Response response = responseEntity.getBody();

		return response;
	}


	/**
	 * The logic in this findEndpointOf function can vary from agent to agent.
	 * However, since it is not a crucial point,
	 * the function will be left uniform for the simulated agents.
	 *
	 * @param agent :the agent whose endpoint is to be find
	 *
	 * @return      :the endpoint of the agent
	 */
	@Override
	public String findEndpointOf(
			String agent
			) {
		if(agent.contains("user"))
			return AppGlobals.PRINEGO_REST_BASE_URL + "SIMULATION/ask";		

		return AppGlobals.PRINEGO_REST_BASE_URL + agent + "/ask";
	}


	@Override
	public String findEndpointForHistoryOf(
			String agent
			) {
		if(agent.contains("user"))
			return AppGlobals.PRINEGO_REST_BASE_URL + "SIMULATION/askHistory";		
		return AppGlobals.PRINEGO_REST_BASE_URL + agent + "/askHistory";
	}

	@Override
	public String findEndpointForPointsOf(String agent) {
		if(agent.contains("user"))
			return AppGlobals.PRINEGO_REST_BASE_URL + "SIMULATION/updatePoint";		
		return AppGlobals.PRINEGO_REST_BASE_URL + agent + "/updatePoint";
	}

	private String findEndpointForUtilityOf(String agent) {
		if(agent.contains("user"))
			return AppGlobals.PRINEGO_REST_BASE_URL + "SIMULATION/getUtility";		
		return AppGlobals.PRINEGO_REST_BASE_URL + agent + "/getUtility";
	}
	/**
	 * The logic in this revise function can vary from agent to agent.
	 * TODO: the variability must be simulated!
	 * This is for default strategy and GEP.
	 * 
	 * @param p            :the owner’s post request to be revised
	 * @param altMediums   :alternative mediums of the owner's choice
	 * @param iterations   :represents the realized iterations
	 * @param cur          :current iteration index
	 * @param max          :allowed max number of negotiation iterations
	 *
	 * @return             :the revised post request
	 */
	@Override
	public PostRequest revise(
			PostRequest p,
			Set<Medium> altMediums,
			List<NegotiationIteration> iterations,
			int cur,
			int max
			) {

		if ( altMediums == null ) {
			altMediums = new HashSet();
		}

		Preconditions.checkNotNull(p);
		Preconditions.checkNotNull(p.getOwner());
		Preconditions.checkNotNull(p.getAudience());
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(p.getAudience().getAudienceMembers()));
		Preconditions.checkNotNull(iterations);
		Preconditions.checkArgument(iterations.size() == cur);
		Preconditions.checkArgument(cur >= 1 && cur <= max);

		NegotiationIteration curIteration = iterations.get(cur-1);
		Preconditions.checkNotNull(curIteration);
		Preconditions.checkNotNull(curIteration.getPostRequest());
		Preconditions.checkNotNull(curIteration.getResponseSet());

		if ( shouldAbandonTheRequest(curIteration, altMediums) ) {
			return null;
		}

		Preconditions.checkArgument(
				getIsMediumRejectedInCurIter(curIteration) ||
				getIsPostTextRejectedInCurIter(curIteration) ||
				getIsAudienceRejectedInCurIter(curIteration) ||
				getIsCombinedRejectedInCurIter(curIteration)
				);

		PostRequest newP = new PostRequest(p);

	
		if ( getIsAudienceRejectedInCurIter(curIteration) ) {

			// rejecting person, set of rejected people
			Map<String, Set<String>> rejectedPeopleInAudience = getRejectedPeopleInAudience(iterations);

			discardRejectedAudienceMembers(newP, rejectedPeopleInAudience);
		}



		if ( shouldAbandonTheRequest(newP) ) {
			return null;
		} else if(NegotiationType.DEFAULT.equals(p.getNegotiationMethod())){
			return newP;
		}else if(NegotiationType.GEP.equals(p.getNegotiationMethod())
				||NegotiationType.MP.equals(p.getNegotiationMethod())){
			double utility = calculateUtility(newP,iterations.get(0).getPostRequest());
			
			System.out.println("owner calculated utility " + utility);
			System.out.println("owner utility threshold" + getUtilityThreshold());

			if(utility >= getUtilityThreshold()){
				return newP;
			}
			return p;
		}else{
			return null;
		}
	}

	/**
	 * The logic in this revise function can vary from agent to agent.
	 * TODO: the variability must be simulated!
	 * This is for MP. However there seems to be no difference between
	 * this and the previous now that I see it. However there might
	 * be a reason for this, I don't want to break the system.
	 * TODO: Replace with the revise function above if unnecessary.
	 *
	 * @param p            :the owner’s post request to be revised
	 * @param altMediums   :alternative mediums of the owner's choice
	 * @param iterations   :represents the realized iterations
	 * @param type 		   :negotiation type
	 * @param cur          :current iteration index
	 * @param max          :allowed max number of negotiation iterations
	 *
	 * @return             :the revised post request
	 */
	@Override
	public PostRequest revise(
			PostRequest p,
			Set<Medium> altMediums,
			List<NegotiationIteration> iterations,
			NegotiationType type,
			int cur,
			int max
			) {

		if ( altMediums == null ) {
			altMediums = new HashSet();
		}

		Preconditions.checkNotNull(p);
		Preconditions.checkNotNull(p.getOwner());
		Preconditions.checkNotNull(p.getAudience());
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(p.getAudience().getAudienceMembers()));
		Preconditions.checkNotNull(iterations);
		Preconditions.checkArgument(iterations.size() == cur);
		Preconditions.checkArgument(cur >= 1 && cur <= max);
		Preconditions.checkArgument(type.equals(NegotiationType.MP));

		NegotiationIteration curIteration = iterations.get(cur-1);
		Preconditions.checkNotNull(curIteration);
		Preconditions.checkNotNull(curIteration.getPostRequest());
		Preconditions.checkNotNull(curIteration.getResponseSet());

		if ( shouldAbandonTheRequest(curIteration, altMediums) ) {
			return null;
		}

		Preconditions.checkArgument(
				getIsMediumRejectedInCurIter(curIteration) ||
				getIsPostTextRejectedInCurIter(curIteration) ||
				getIsAudienceRejectedInCurIter(curIteration) ||
				getIsCombinedRejectedInCurIter(curIteration)
				);

		PostRequest newP = new PostRequest(p);
	
		if ( getIsCombinedAudienceRejectedInCurIter(curIteration) ) {

			List<NegotiationIteration> onlyCurIteration = new ArrayList<NegotiationIteration>();
			onlyCurIteration.add(curIteration);

			// rejecting person, set of rejected people
			Map<String, Set<String>> rejectedPeopleInAudience = getRejectedPeopleInAudience(onlyCurIteration);

			discardRejectedAudienceMembers(newP, rejectedPeopleInAudience);
		}



		if ( shouldAbandonTheRequest(newP) ) {
			return null;
		} else if(NegotiationType.DEFAULT.equals(p.getNegotiationMethod())){
			return newP;
		}else if(NegotiationType.GEP.equals(p.getNegotiationMethod())
				||NegotiationType.MP.equals(p.getNegotiationMethod())){
			double utility = calculateUtility(newP,iterations.get(0).getPostRequest());
			
			System.out.println("owner calculated utility " + utility);
			System.out.println("owner utility threshold" + getUtilityThreshold());

			if(utility >= getUtilityThreshold()){
				return newP;
			}
			return p;
		}else{
			return null;
		}
	}


	/**
	 * calculates utility of the given post request according to the initiator agent.
	 * 
	 * @param newP the post request to evaluate for utility.
	 * @param originalPost the first post request initiator agent suggested.
	 * */
	private double calculateUtility(PostRequest newP, PostRequest originalPost) {

		//This function only considers the size of the audience.
		double originalAudienceSize = originalPost.getAudience().getAudienceMembers().size();
		double newAudienceSize = newP.getAudience().getAudienceMembers().size();
		return 1 - ((originalAudienceSize-newAudienceSize)/originalAudienceSize);
	}

	private boolean getIsCombinedRejectedInCurIter(NegotiationIteration curIteration) {

		Set<Response> responseSet = curIteration.getResponseSet();
		Iterator<Response> responseIterator = responseSet.iterator();
		while (responseIterator.hasNext()) {
			Response response = responseIterator.next();
			if ( "Y".equals(response.getResponseCode()) == false ) {
				RejectionReason reason = response.getReason();
				if ( reason != null ) {
					if (RejectedField.COMBINED.equals(reason.getField())) {
						return true;
					}
				}
			}
		}

		return false;
	}


	private boolean getIsAudienceRejectedInCurIter(NegotiationIteration curIteration) {

		Set<Response> responseSet = curIteration.getResponseSet();
		Iterator<Response> responseIterator = responseSet.iterator();
		while (responseIterator.hasNext()) {
			Response response = responseIterator.next();
			if ( "Y".equals(response.getResponseCode()) == false ) {
				RejectionReason reason = response.getReason();
				if ( reason != null ) {
					if (RejectedField.AUDIENCE.equals(reason.getField())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean getIsPostTextRejectedInCurIter(NegotiationIteration curIteration) {

		Set<Response> responseSet = curIteration.getResponseSet();
		Iterator<Response> responseIterator = responseSet.iterator();
		while (responseIterator.hasNext()) {
			Response response = responseIterator.next();
			if ( "Y".equals(response.getResponseCode()) == false ) {
				RejectionReason reason = response.getReason();
				if ( reason != null ) {
					if (RejectedField.POST_TEXT.equals(reason.getField())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean getIsMediumRejectedInCurIter(NegotiationIteration curIteration) {

		Set<Response> responseSet = curIteration.getResponseSet();
		Iterator<Response> responseIterator = responseSet.iterator();
		while (responseIterator.hasNext()) {
			Response response = responseIterator.next();
			if ( "Y".equals(response.getResponseCode()) == false ) {
				RejectionReason reason = response.getReason();
				if ( reason != null ) {
					if (RejectedField.MEDIUM.equals(reason.getField())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean getIsCombinedAudienceRejectedInCurIter(NegotiationIteration curIteration) {//TODO combined reasonlarda rejection reasonlarý anla bul.

		Set<Response> responseSet = curIteration.getResponseSet();
		Iterator<Response> responseIterator = responseSet.iterator();
		while (responseIterator.hasNext()) {
			Response response = responseIterator.next();
			if ( "Y".equals(response.getResponseCode()) == false ) {
				RejectionReason reason = response.getReason();
				if ( reason != null ) {
					if (!reason.getIncludedPeople().isEmpty()) {
						return true;
					}
				}
			}
		}

		return false;
	}


	private boolean getIsCombinedMediumRejectedInCurIter(NegotiationIteration curIteration) {

		Set<Response> responseSet = curIteration.getResponseSet();
		Iterator<Response> responseIterator = responseSet.iterator();
		while (responseIterator.hasNext()) {
			Response response = responseIterator.next();
			if ( "Y".equals(response.getResponseCode()) == false ) {
				RejectionReason reason = response.getReason();
				if ( reason != null ) {
					if (reason.getContextDislike()!=null || !reason.getIncludedLocations().isEmpty() || 
							reason.isDateTakenDislike() == true || 
							reason.isSelfDislike() == true) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean getIsPointOfferRejected(NegotiationIteration curIteration) {

		Iterator<Response> iter = curIteration.getResponseSet().iterator();
		while(iter.hasNext()){
			Response response = iter.next();
			if(response.getPointOffer()!=curIteration.getPointOffer())
				return true;
		}
		return false;
	}

	private boolean shouldAbandonTheRequest(NegotiationIteration curIteration, Set<Medium> altMediums) {

		Set<Response> responseSet = curIteration.getResponseSet();
		Iterator<Response> responseIterator = responseSet.iterator();
		while ( responseIterator.hasNext() ) {
			Response response = responseIterator.next();
			if ( "Y".equals(response.getResponseCode())  ) {
				continue;
			}
			RejectionReason reason = response.getReason();
			if ( reason == null ) {
				return true;    // if rejected without reason, abandon the post request.
			} else if ( (RejectedField.AUDIENCE.equals(reason.getField())||RejectedField.COMBINED.equals(reason.getField()))
					&& CollectionUtils.isEmpty(reason.getIncludedPeople()) ) {
				return true;
			} else if ( RejectedField.MEDIUM.equals(reason.getField())
					&& (curIteration.getPostRequest().getMedium() == null && CollectionUtils.isEmpty(altMediums) )  ) {
				return true;
			}
		}

		return false;
	}

	private Map<String, Set<String>> getRejectedPeopleInAudience(List<NegotiationIteration> iterations) {

		Map<String, Set<String>> rejectedPeopleInAudience = new HashMap();

		Iterator<NegotiationIteration> negotiationIterator = iterations.iterator();
		while ( negotiationIterator.hasNext() ) {
			NegotiationIteration iteration = negotiationIterator.next();

			Set<Response> responseSet = iteration.getResponseSet();
			Iterator<Response> responseIterator = responseSet.iterator();
			while (responseIterator.hasNext()) {
				Response response = responseIterator.next();
				if ( "Y".equals(response.getResponseCode()) == false ) {
					RejectionReason reason = response.getReason();
					if ((RejectedField.AUDIENCE.equals(reason.getField()) || RejectedField.COMBINED.equals(reason.getField()))
							&& CollectionUtils.isNotEmpty(reason.getIncludedPeople())) {

						Set<String> rejectedPeople = rejectedPeopleInAudience.get(response.getOwner());
						if (rejectedPeople == null) {
							rejectedPeople = new HashSet();
						}
						rejectedPeople.addAll(reason.getIncludedPeople());
						rejectedPeopleInAudience.put(response.getOwner(), rejectedPeople);
					}
				}
			}
		}

		return rejectedPeopleInAudience;
	}

	private void discardRejectedAudienceMembers(
			PostRequest newP,
			Map<String, Set<String>> rejectedPeopleInAudience) {

		Set<String> rejectedPeople = MySetUtils.flatten(rejectedPeopleInAudience.values());

		Iterator<Agent> agentIterator = newP.getAudience().getAudienceMembers().iterator();
		while ( agentIterator.hasNext() ) {
			Agent agent = agentIterator.next();
			if ( rejectedPeople.contains(agent.getUid()) ) {
				agentIterator.remove();
			}
		}
	}

	private Map<String, Set<String>> getRejectedPeopleInMedium(List<NegotiationIteration> iterations) {

		Map<String, Set<String>> rejectedPeopleInMedium = new HashMap();

		Iterator<NegotiationIteration> negotiationIterator = iterations.iterator();
		while ( negotiationIterator.hasNext() ) {
			NegotiationIteration iteration = negotiationIterator.next();

			Set<Response> responseSet = iteration.getResponseSet();
			Iterator<Response> responseIterator = responseSet.iterator();
			while (responseIterator.hasNext()) {
				Response response = responseIterator.next();
				if ( "Y".equals(response.getResponseCode()) == false ) {
					RejectionReason reason = response.getReason();
					if ((RejectedField.MEDIUM.equals(reason.getField()) || RejectedField.COMBINED.equals(reason.getField()))
							&& CollectionUtils.isNotEmpty(reason.getIncludedPeople())) {

						Set<String> rejectedPeople = rejectedPeopleInMedium.get(response.getOwner());
						if (rejectedPeople == null) {
							rejectedPeople = new HashSet();
						}
						rejectedPeople.addAll(reason.getIncludedPeople());
						rejectedPeopleInMedium.put(response.getOwner(), rejectedPeople);
					}
				}
			}
		}

		return rejectedPeopleInMedium;
	}

	private Map<String, Set<String>> getRejectedLocationsInMedium(List<NegotiationIteration> iterations) {

		Map<String, Set<String>> rejectedLocationsInMedium = new HashMap();

		Iterator<NegotiationIteration> negotiationIterator = iterations.iterator();
		while ( negotiationIterator.hasNext() ) {
			NegotiationIteration iteration = negotiationIterator.next();

			Set<Response> responseSet = iteration.getResponseSet();
			Iterator<Response> responseIterator = responseSet.iterator();
			while (responseIterator.hasNext()) {
				Response response = responseIterator.next();
				if ( "Y".equals(response.getResponseCode()) == false ) {
					RejectionReason reason = response.getReason();
					if ((RejectedField.MEDIUM.equals(reason.getField()) || RejectedField.COMBINED.equals(reason.getField()))
							&& CollectionUtils.isNotEmpty(reason.getIncludedLocations())) {

						Set<String> rejectedLocations = rejectedLocationsInMedium.get(response.getOwner());
						if (rejectedLocations == null) {
							rejectedLocations = new HashSet();
						}
						rejectedLocations.addAll(reason.getIncludedLocations());
						rejectedLocationsInMedium.put(response.getOwner(), rejectedLocations);
					}
				}
			}
		}

		return rejectedLocationsInMedium;
	}

	private Map<String, Set<Date>> getRejectedDateTakensInMedium(List<NegotiationIteration> iterations) {

		Map<String, Set<Date>> rejectedDateTakens = new HashMap();

		Iterator<NegotiationIteration> negotiationIterator = iterations.iterator();
		while ( negotiationIterator.hasNext() ) {
			NegotiationIteration iteration = negotiationIterator.next();

			Set<Response> responseSet = iteration.getResponseSet();
			Iterator<Response> responseIterator = responseSet.iterator();
			while (responseIterator.hasNext()) {
				Response response = responseIterator.next();
				if ( "Y".equals(response.getResponseCode()) == false ) {
					RejectionReason reason = response.getReason();
					if ((RejectedField.MEDIUM.equals(reason.getField()) || RejectedField.COMBINED.equals(reason.getField()))
							&& reason.isDateTakenDislike()) {
						Preconditions.checkNotNull(iteration.getPostRequest().getMedium());
						Preconditions.checkNotNull(iteration.getPostRequest().getMedium().getDateTaken());

						Set<Date> dislikedDates = rejectedDateTakens.get(response.getOwner());
						if (dislikedDates == null) {
							dislikedDates = new HashSet();
						}
						dislikedDates.add(iteration.getPostRequest().getMedium().getDateTaken());
						rejectedDateTakens.put(response.getOwner(), dislikedDates);
					}
				}
			}
		}

		return rejectedDateTakens;
	}

	private Map<String, Set<String>> getContextDislikedMediums(List<NegotiationIteration> iterations) {

		Map<String, Set<String>> contextDislikedMediums = new HashMap();

		Iterator<NegotiationIteration> negotiationIterator = iterations.iterator();
		while ( negotiationIterator.hasNext() ) {
			NegotiationIteration iteration = negotiationIterator.next();

			Set<Response> responseSet = iteration.getResponseSet();
			Iterator<Response> responseIterator = responseSet.iterator();
			while (responseIterator.hasNext()) {
				Response response = responseIterator.next();
				if ( "Y".equals(response.getResponseCode()) == false ) {
					RejectionReason reason = response.getReason();
					if ((RejectedField.MEDIUM.equals(reason.getField()) || RejectedField.COMBINED.equals(reason.getField()))
							&& StringUtils.isNotEmpty(reason.getContextDislike())) {
						Preconditions.checkNotNull(iteration.getPostRequest().getMedium());
						Preconditions.checkNotNull(iteration.getPostRequest().getMedium().getUrl());

						Set<String> dislikedMediums = contextDislikedMediums.get(response.getOwner());
						if (dislikedMediums == null) {
							dislikedMediums = new HashSet();
						}
						dislikedMediums.add(iteration.getPostRequest().getMedium().getUrl());
						contextDislikedMediums.put(response.getOwner(), dislikedMediums);
					}
				}
			}
		}

		return contextDislikedMediums;
	}

	/**
	 * @desc to be called at the end of the revise to check whether abandon or propose the new request.
	 */
	private boolean shouldAbandonTheRequest(PostRequest p) {

		if ( p == null ) {
			return true;
		} else if ( p.getAudience() == null ) {
			return true;
		} else if ( CollectionUtils.isEmpty(p.getAudience().getAudienceMembers())) {
			return true;
		} else if (  p.getMedium() == null ) {  //p.getPostText() == null && þimdilik çýkardým.
			return true;
		}
		return false;
	}



}