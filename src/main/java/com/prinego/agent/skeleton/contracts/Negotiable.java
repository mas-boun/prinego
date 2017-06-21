package com.prinego.agent.skeleton.contracts;

import java.util.List;

import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.response.Response;

/**
 * Created by mester on 31/08/14.
 */
public interface Negotiable {

    Response evaluate(
            PostRequest p
    );
    
    Response evaluate(
            PostRequest p,List<Response> responses
    );
    
    Response evaluate(
            PostRequest p,List<Response> responses,int pointOffer
    );

	double getUtility(PostRequest initial,PostRequest finalized,String role);

}