package com.prinego.domain.entity.request;

import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.response.Response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dilara on 09/02/16.
 */
public @Data class HistoryRequest implements Serializable {

    private PostRequest p;
    private List<Response> agentResponses;
    private int pointOffer; 
    
    public HistoryRequest() { }

	public PostRequest getP() {
		return p;
	}

	public void setP(PostRequest p) {
		this.p = p;
	}

	public List<Response> getAgentResponses() {
		return agentResponses;
	}

	public void setAgentResponses(List<Response> agentResponses) {
		this.agentResponses = agentResponses;
	}
	
	public int getPointOffer() {
		return pointOffer;
	}

	public void setPointOffer(int pointOffer) {
		this.pointOffer = pointOffer;
	}

}
