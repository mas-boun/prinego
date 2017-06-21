package com.prinego.domain.entity.negotiation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.response.Response;

import lombok.Data;

/**
 * Created by mester on 21/10/14.
 */
@Data
public class NegotiationIteration implements Serializable {

    private PostRequest postRequest;
    private Set<Response> responseSet;
    private int pointOffer;
    
    public NegotiationIteration() {
        this.responseSet = new HashSet();
    }

    public NegotiationIteration(PostRequest postRequest, Set<Response> responseSet) {
        this.postRequest = postRequest;
        this.responseSet = responseSet;
    }
    
    public NegotiationIteration(PostRequest postRequest, Set<Response> responseSet,int pointOffer) {
        this.postRequest = postRequest;
        this.responseSet = responseSet;
        this.pointOffer = pointOffer;
    }

    @Override
    public String toString() {
        Preconditions.checkNotNull(postRequest);
        Preconditions.checkNotNull(responseSet);

        StringBuilder sb = new StringBuilder();
        sb.append("[NegotiationIteration:" + "\n");
        sb.append("postRequest:" + "\n");
        sb.append(postRequest + "\n");

        sb.append(responseSet.size() + " responses:" + "\n");
        for ( Response response : responseSet ) {
            sb.append(response + "\n");
        }
        sb.append("]");

        return sb.toString();
    }

	public PostRequest getPostRequest() {
		return postRequest;
	}

	public void setPostRequest(PostRequest postRequest) {
		this.postRequest = postRequest;
	}

	public Set<Response> getResponseSet() {
		return responseSet;
	}

	public void setResponseSet(Set<Response> responseSet) {
		this.responseSet = responseSet;
	}
	
	public int getPointOffer() {
		return pointOffer;
	}

	public void setPointOffer(int pointOffer) {
		this.pointOffer = pointOffer;
	}

}
