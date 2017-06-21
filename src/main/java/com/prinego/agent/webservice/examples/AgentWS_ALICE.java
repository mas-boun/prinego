package com.prinego.agent.webservice.examples;


import com.prinego.agent.Agent_ALICE;
import com.prinego.agent.skeleton.AgentSkeleton;
import com.prinego.agent.webservice.base.BaseAgentWS;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.response.Response;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Created by mester on 31/08/14.
 */
@RestController
public class AgentWS_ALICE extends BaseAgentWS {

    @Inject
    private Agent_ALICE agentService;


    @Override
    public AgentSkeleton getAgentService() {
        return agentService;
    }


    @Override
    @RequestMapping(value = "/ALICE/ask")
    public Response evaluate(
            @RequestBody PostRequest p) {

    	return super.evaluate(p);
    }
    
    @Override
    @RequestMapping(value = "/ALICE/askHistory")
    public Response evaluate(
            @RequestBody HistoryRequest historyRequest) {
    	return super.evaluate(historyRequest);
    }
    
    
    @Override
    @RequestMapping(value = "/ALICE/updatePoint")
    public void updatePoint(
            @RequestBody HistoryRequest historyRequest) {
    	super.updatePoint(historyRequest);
    }
    
    @Override
	@RequestMapping(value = "/ALICE/getUtility")
	public double getUtility(
			@RequestBody String utilityRequest) {
    	return super.getUtility(utilityRequest);
	}
    
    
    @Override
    @RequestMapping(value = "/ALICE/upload")
    public String upload(@RequestBody String uploadRequestJson) {

    	
        return super.upload(uploadRequestJson);
    }

}
