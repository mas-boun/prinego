package com.prinego.agent.webservice.examples;


import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prinego.agent.Agent_BOB;
import com.prinego.agent.skeleton.AgentSkeleton;
import com.prinego.agent.webservice.base.BaseAgentWS;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.response.Response;

/**
 * Created by mester on 31/08/14.
 */
@RestController
public class AgentWS_BOB extends BaseAgentWS {

    @Inject
    private Agent_BOB agentService;


    @Override
    public AgentSkeleton getAgentService() {
        return agentService;
    }


    @Override
    @RequestMapping(value = "/BOB/ask")
    public Response evaluate(
            @RequestBody PostRequest p) {

        return super.evaluate(p);
    }

    @Override
    @RequestMapping(value = "/BOB/askHistory")
    public Response evaluate(
            @RequestBody HistoryRequest historyRequest) {
    	return super.evaluate(historyRequest);
    }
    
    @Override
    @RequestMapping(value = "/BOB/upload")
    public String upload(@RequestBody String uploadRequestJson) {

        return super.upload(uploadRequestJson);
    }
    
    @Override
    @RequestMapping(value = "/BOB/updatePoint")
    public void updatePoint(
            @RequestBody HistoryRequest historyRequest) {
    	super.updatePoint(historyRequest);
    }

    @Override
	@RequestMapping(value = "/BOB/getUtility")
	public double getUtility(
			@RequestBody String utilityRequest) {
		return super.getUtility(utilityRequest);
	}

}
