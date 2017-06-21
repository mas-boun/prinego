package com.prinego.agent.webservice.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.prinego.agent.ontologicalBase.OntologicalAgentBase;
import com.prinego.agent.skeleton.AgentSkeleton;
import com.prinego.database.handler.MyDatabaseService;
import com.prinego.domain.entity.negotiation.NegotiationIteration;
import com.prinego.domain.entity.negotiation.NegotiationType;
import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.request.UploadRequest;
import com.prinego.domain.entity.request.UtilityRequest;
import com.prinego.domain.entity.response.Response;
import com.prinego.util.json.JsonReader;
import com.prinego.util.json.JsonWriter;

/**
 * Created by mester on 05/09/14.
 * Updated by dilara
 * This class is the base of web services of every agent in the system.
 * New agent web services should extend this class. 
 */
public abstract class BaseAgentWS {

	public abstract AgentSkeleton getAgentService();

	@Inject
	private MyDatabaseService myDatabaseService;

	public String getUid() {
		return getAgentService().getUid();
	}

	/**
	 * This method gets a PostRequest p for evaluation and returns the response
	 * of the negotiator agent to the request.
	 * It calls the OntologicalAgentBase's corresponding evaluate method.
	 * 
	 * This method only used when the negotiation strategy is default.
	 * 
	 * @param p The postrequest that is offered by initiator agent for upload.
	 * @return response The response of the negotiator agent regarding the p. 
	 * */
	public Response evaluate(PostRequest p) {

		
		try {
			OntologicalAgentBase ontologicalAgentService = (OntologicalAgentBase) getAgentService();
			
		} catch ( Exception e ) {
			; // do nothing
		};

		return getAgentService().evaluate(p);
	}

	/**
	 * This method gets a HistoryRequest p for evaluation and returns the response
	 * of the negotiator agent to the request.
	 * It calls the OntologicalAgentBase's corresponding evaluate method.
	 * 
	 * This method is used when the negotiation type is not the default one.
	 * 
	 * @param agentHistory The history request that is offered by initiator agent for upload.
	 * @return response The response of the negotiator agent regarding the agentHistory. 
	 * */
	public Response evaluate(HistoryRequest agentHistory) {
		System.out.println(agentHistory);

		
		try {
			OntologicalAgentBase ontologicalAgentService = (OntologicalAgentBase) getAgentService();

		} catch ( Exception e ) {
			; // do nothing
		};
		System.out.println(getAgentService().getUid());
		//point based negotiations have point offers in evaluate function.
		if(agentHistory.getP().getNegotiationMethod().equals(NegotiationType.RPG) ||
				agentHistory.getP().getNegotiationMethod().equals(NegotiationType.RPM)){
			System.out.println("point based evaluate for included agent");
			return getAgentService().evaluate(agentHistory.getP(),agentHistory.getAgentResponses(),agentHistory.getPointOffer());
		}
		
		return getAgentService().evaluate(agentHistory.getP(),agentHistory.getAgentResponses());
	}

	/**
	 * This method gets the initial upload request from the WsCallUtil in a string form.
	 * It is the first method called to start the negotiation, the call is made to
	 * initiator agent's web service.
	 * After the negotiation ends, this sends the result of it in json format.
	 * 
	 * @param requestJson the json format of the upload request.
	 * @return (json)finalizedP the json format of the finalized PostRequest that is going to be
	 * uploaded.
	 * 
	 * */
	public String upload(String requestJson) {

		UploadRequest uploadRequest = null;
		try {
			uploadRequest = (UploadRequest) JsonReader.jsonToJava(requestJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Preconditions.checkNotNull(uploadRequest);

		PostRequest p = uploadRequest.getP();
		Set<Medium> altMediums = uploadRequest.getAltMediums();
		Preconditions.checkNotNull(p);

		PostRequest finalizedP;

		//calls the internal upload method.
		finalizedP = upload(p, altMediums);



		try {
			return JsonWriter.objectToJson(finalizedP);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * This method was added later to evaluate our simulation with pairwise sharing behavior.
	 * It finds the best possible negotiation outcome for the postRequest pairs in the
	 * pairwise simulation.
	 * 
	 * @param uploadRequestJson The upload request that has the postRequests
	 * for a simulation agent pairs.
	 * */
	public void pairwise(String uploadRequestJson) {

		List<PostRequest> uploadRequest = null;
		try {
			uploadRequest = (List<PostRequest>) JsonReader.jsonToJava(uploadRequestJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Preconditions.checkNotNull(uploadRequest);
		Preconditions.checkArgument(uploadRequest.size()==2);

		PostRequest p1 = uploadRequest.get(0);
		PostRequest p2 = uploadRequest.get(1);

		//finds the best possible outcome and writes it to database.
		getAgentService().oneStepNegotiation(p1,p2);

	}
	/**
	 * This method gets a postRequest and its alternative mediums
	 * and starts the negotiation. 
	 * The negotiation methods are in the AgentSkeleton.
	 * The method calls depends on the negotiation type.
	 * 
	 * @param p post request to be negotiated.
	 * @param altMediums  alternative mediums of postrequest p.
	 * 
	 *  @return finalizedP The outcome of the negotiation.
	 * */
	private PostRequest upload(PostRequest p, Set<Medium> altMediums) {


		myDatabaseService.writeDemoLog(p, "owner",false);

		List<NegotiationIteration> iterations = new ArrayList();
		int cur = 1;
		int max = 5; // TODO: actually should be a variable, but not urgent.

		if(p.getNegotiationMethod()==NegotiationType.MP){
			myDatabaseService.writeDemoLog("Maximal-Privacy strategy is used.", "owner",false);
			Map<String,List<Response>> agentMemory = new HashMap<String, List<Response>>();
			PostRequest finalizedP = getAgentService().negotiate(
					p,
					altMediums,
					iterations,
					agentMemory,
					cur,
					max,
					false,
					p.getNegotiationMethod());

			return finalizedP;
		}
		
		if(p.getNegotiationMethod()==NegotiationType.SUCH_BASED){
			System.out.println("such based negotiation");
			PostRequest finalizedP = getAgentService().oneStepNegotiation(p);

			return finalizedP;

		}

		if(p.getNegotiationMethod()==NegotiationType.RPG || p.getNegotiationMethod()==NegotiationType.RPM){
			System.out.println("point based negotiation");
			myDatabaseService.writeDemoLog("Reciprocal-Privacy strategy is used.", "owner",false);

			Map<String,List<Response>> agentMemory = new HashMap<String, List<Response>>();
			//this one has one more allowed iteration since first one is spent on
			//sending the rejected people in order of importance.
			PostRequest finalizedP = getAgentService().pointBasedNegotiation(
					p,
					iterations,
					agentMemory,
					cur,
					max+1,
					0);

			return finalizedP;

		}
		if(p.getNegotiationMethod()==NegotiationType.HybridG){
			//in this strategy we first try to find a solution with using GEP.
			myDatabaseService.writeDemoLog("GEP PLUS RP strategy is used.", "owner",false);
			p.setNegotiationMethod(NegotiationType.GEP);

			Map<String,List<Response>> agentMemory = new HashMap<String, List<Response>>();
			PostRequest finalizedP = getAgentService().negotiate(
					p,
					altMediums,
					iterations,
					agentMemory,
					cur,
					max,
					false,
					p.getNegotiationMethod());

			//if we can't find a solution with GEP, then we use the RPG.
			if(finalizedP==null){
				myDatabaseService.writeDemoLog("Reciprocal-Privacy strategy is used.", "owner",false);
				iterations = new ArrayList();
				agentMemory = new HashMap<String, List<Response>>();
				p.setNegotiationMethod(NegotiationType.RPG);

				finalizedP = getAgentService().pointBasedNegotiation(
						p,
						iterations,
						agentMemory,
						cur,
						6,
						0);

				return finalizedP;
			}else{
				myDatabaseService.writeDemoLog("GEP was successful. RP strategy is used for points.", "owner",false);
				finalizedP.setNegotiationMethod(NegotiationType.HybridG);
				finalizedP = getAgentService().GEPMPPLUSSRPNegotiation(finalizedP, iterations.get(0));
				return finalizedP;

			}
		}

		if(p.getNegotiationMethod()==NegotiationType.HybridM){
			//in this strategy we first try to find a solution with using MP.
			myDatabaseService.writeDemoLog("MP PLUS RP strategy is used.", "owner",false);
			p.setNegotiationMethod(NegotiationType.MP);

			Map<String,List<Response>> agentMemory = new HashMap<String, List<Response>>();
			PostRequest finalizedP = getAgentService().negotiate(
					p,
					altMediums,
					iterations,
					agentMemory,
					cur,
					max,
					false,
					p.getNegotiationMethod());

			//if we can't find a solution with MP, then we use the RPM.
			if(finalizedP==null){
				myDatabaseService.writeDemoLog("Reciprocal-Privacy strategy is used.", "owner",false);
				iterations = new ArrayList();
				agentMemory = new HashMap<String, List<Response>>();
				p.setNegotiationMethod(NegotiationType.RPM);

				finalizedP = getAgentService().pointBasedNegotiation(
						p,
						iterations,
						agentMemory,
						cur,
						6,
						0);

				return finalizedP;
			}else{
				myDatabaseService.writeDemoLog("MP was successful. RP strategy is used for points.", "owner",false);
				finalizedP.setNegotiationMethod(NegotiationType.HybridM);
				finalizedP = getAgentService().GEPMPPLUSSRPNegotiation(finalizedP, iterations.get(0));
				return finalizedP;

			}
		}

		myDatabaseService.writeDemoLog("Good-Enough-Privacy strategy is used.", "owner",false);
		Map<String,List<Response>> agentMemory = new HashMap<String, List<Response>>();
		PostRequest finalizedP = getAgentService().negotiate(
				p,
				altMediums,
				iterations,
				agentMemory,
				cur,
				max,
				false,
				p.getNegotiationMethod());

		return finalizedP;

	}
	/**
	 * Updates the points of negotiator agent
	 * after the negotiation ends.
	 * 
	 * @param historyRequest Wrapper object that contains the final point offer.
	 * */
	public void updatePoint(HistoryRequest historyRequest) {

		int point = historyRequest.getPointOffer();
		String opponent = historyRequest.getP().getOwner().getUid();
		getAgentService().setPoints(getUid(),opponent,"INCLUDED",point);
	}



	/**
	 * This method is called when the negotiation ends and it is for
	 * acquiring the utilities of both parties so that we can calculate
	 * the total utility according to evaluation metrics.
	 * 
	 * @param utilityRequestJson Wrapper object that contains the initial post
	 * request and the final post request that is agreed on.
	 *  
	 * @return utility the utility of the final post request of an agent 
	 * according to its role.
	 *  
	 * */
	public double getUtility(String utilityRequestJson) {

		UtilityRequest utilityRequest = null;
		try {
			utilityRequest = (UtilityRequest) JsonReader.jsonToJava(utilityRequestJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Preconditions.checkNotNull(utilityRequest);

		if(utilityRequest.getRole().equals("negotiator")){
			OntologicalAgentBase ontologicalAgentService = (OntologicalAgentBase) getAgentService();
			return ontologicalAgentService.getUtility(utilityRequest.getinitialP(),utilityRequest.getFinalizedP(),utilityRequest.getRole());
		}
		double originalAudienceSize = utilityRequest.getinitialP().getAudience().getAudienceMembers().size();
		double newAudienceSize = utilityRequest.getFinalizedP()!=null?
				utilityRequest.getFinalizedP().getAudience().getAudienceMembers().size():0;
				return 1 - ((originalAudienceSize-newAudienceSize)/originalAudienceSize);

	}

}
