package com.prinego.agent.webservice.examples;

import java.util.Iterator;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prinego.agent.Agent_SIMULATION;
import com.prinego.agent.skeleton.AgentSkeleton;
import com.prinego.agent.webservice.base.BaseAgentWS;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.response.Response;

/**
 * Every agent needs to have a webservice(WS) and this is 
 * the webservice of Agent_SIMULATION. It basically is same
 * as the other WSs such as AgentWS_Alice but few exceptions.
 *
 * AgentWS_SIMULATION is a generic webservice
 * for simulation agents so the agentUid is not declared statically
 * in the Agent_SIMULATION like other agents. So at each call
 * to evaluate method, we need to know which simulation agent
 * is requested for evaluation.
 * 
 */
@RestController
public class AgentWS_SIMULATION extends BaseAgentWS {

	@Inject
	private Agent_SIMULATION agentService;


	@Override
	public AgentSkeleton getAgentService() {
		return agentService;
	}


	/**
	 *  This is where the negotiation request is sent.
	 *  Initiator agent creates a post request and asks the
	 *  negotiator agent its response. 
	 *  
	 *  This evaluation method is only used by the default negotiation strategy.
	 *  
	 *  To understand which simulator agent is the negotiator,
	 *  we get the included person in the postrequest sent for evaluation.
	 *  
	 *  @param p post request sent for evaluation by the initiator agent
	 *  
	 *  @return response Response of the negotiator agent regarding p.
	 *  
	 */
	@Override
	@RequestMapping(value = "/SIMULATION/ask")
	public Response evaluate(
			@RequestBody PostRequest p) {
		Iterator<Agent> iter = p.getMedium().getIncludedPeople().iterator(); //get the current simulation agent.
		agentService.setUid(iter.next().getUid());
		return super.evaluate(p);
	}

	/**
	 *  This is where the negotiation request is sent.
	 *  Initiator agent creates a post request and asks the
	 *  negotiator agent its response. 
	 *  
	 *  This evaluation method is only used by other negotiation
	 *  strategies than the default one.
	 *  
	 *  To understand which simulator agent is the negotiator,
	 *  we get the included person in the postrequest sent for evaluation.
	 *  
	 *  @param historyRequest Wrapper object that contains the post request sent 
	 *  for evaluation by the initiator agent
	 *  and the previous responses of the negotiator agent.
	 *  
	 *  @return response Response of the negotiator agent regarding p.
	 *  
	 */
	@Override
	@RequestMapping(value = "/SIMULATION/askHistory")
	public Response evaluate(
			@RequestBody HistoryRequest historyRequest) {
		Iterator<Agent> iter = historyRequest.getP().getMedium().getIncludedPeople().iterator();
		agentService.setUid(iter.next().getUid());
		return super.evaluate(historyRequest);
	}

	//following methods are implemented by the abstract parent class BaseAgentWS
	//they are explained there.
	@Override
	@RequestMapping(value = "/SIMULATION/updatePoint")
	public void updatePoint(
			@RequestBody HistoryRequest historyRequest) {
		super.updatePoint(historyRequest);
	}
	
	@Override
	@RequestMapping(value = "/SIMULATION/getUtility")
	public double getUtility(
			@RequestBody String utilityRequest) {
		return super.getUtility(utilityRequest);
	}

	@Override
	@RequestMapping(value = "/SIMULATION/upload")
	public String upload(@RequestBody String uploadRequestJson) {


		return super.upload(uploadRequestJson);
	}
	
	
	@RequestMapping(value = "/SIMULATION/pairwise")
	public void pairwise(@RequestBody String uploadRequestJson) {

		super.pairwise(uploadRequestJson);
	}

}
