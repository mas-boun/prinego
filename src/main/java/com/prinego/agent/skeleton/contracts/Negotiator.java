package com.prinego.agent.skeleton.contracts;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.prinego.domain.entity.negotiation.NegotiationIteration;
import com.prinego.domain.entity.negotiation.NegotiationType;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.response.Response;

/**
 * Created by mester on 31/08/14.
 */
public interface Negotiator {

    PostRequest negotiate(
            PostRequest p,
            Set<Medium> altMediums,
            List<NegotiationIteration> iterations,
            int cur,
            int max,
            boolean isSame
    );
    
    PostRequest negotiate(
            PostRequest p,
            Set<Medium> altMediums,
            List<NegotiationIteration> iterations,
            Map<String,List<Response>> agentMemory,
            int cur,
            int max,
            boolean isSame,
            NegotiationType negotiationType
    );

    /**
     * a helper method for negotiate function
     */
    Set<Agent> findAgentsToNegotiate(
            PostRequest p
    );

    /**
     * a helper method for negotiate function
     */
    Response ask(
            PostRequest p,
            String agentToAsk
    );
    
    Response ask(
            PostRequest p,
            String agentToAsk,
            List<Response> agentResponses
    );
    
    Response ask(
            PostRequest p,
            String agentToAsk,
            List<Response> agentResponses,
            int pointOffer
    );

    
    /**
     * a helper method for ask function
     */
    String findEndpointOf(
            String agent
    );
    
    /**
     * a helper method for ask function
     */
    String findEndpointForHistoryOf(
            String agent
    );

    /**
     * a helper method for negotiate function
     */
    PostRequest revise(
            PostRequest p,
            Set<Medium> altMediums,
            List<NegotiationIteration> iterations,
            int cur,
            int max
    );
    
    /**
     * a helper method for negotiate function
     */
    PostRequest revise(
            PostRequest p,
            Set<Medium> altMediums,
            List<NegotiationIteration> iterations,
            NegotiationType type,
            int cur,
            int max
    );
    
    /**
     * a helper method for negotiate function
     */
    HistoryRequest pointBasedRevise(
            PostRequest p,
            List<NegotiationIteration> iterations,
            int cur,
            int max
    );

	PostRequest oneStepNegotiation(PostRequest p);

	
	PostRequest pointBasedNegotiation(PostRequest p,
			List<NegotiationIteration> iterations,
			Map<String, List<Response>> agentHistory, int cur, int max,
			int pointOffer);

	String findEndpointForPointsOf(String agent);

	double getUtility(PostRequest initial,PostRequest finalized,String role);

}
