package com.prinego.ontology.reasoner;

import java.util.List;

import com.prinego.domain.entity.negotiation.NegotiationType;
import com.prinego.domain.entity.response.Response;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * Created by mester on 19/10/14.
 */
public interface MyOntologyReasoner {

    Response extractResponse(
            OWLOntology ontology,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            String key,
            String agentUid,
            NegotiationType negotiationMethod,
            double utilityThreshold
    );

	Response extractResponse(
			OWLOntology ontology, 
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm, 
			String key, 
			String agentUid,
			NegotiationType negotiationMethod, 
			double utilityThreshold,
			List<Response> prevResponses
	);

	
	
		Response extractResponse(OWLOntology ontology, OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm, String key, String agentUid,
			NegotiationType negotiationMethod, double utilityThreshold,
			List<Response> prevResponses, int pointOffer, double pointWish,
			int myPoints);

		double extractUtility(OWLOntology myOntology,
				OWLDataFactory dataFactory, PrefixOWLOntologyFormat pm,
				String key, String key2, String uid);

		

		

}
