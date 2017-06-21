package com.prinego.ontology.reasoner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.springframework.stereotype.Component;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.base.Preconditions;
import com.prinego.domain.entity.negotiation.NegotiationType;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.audience.Audience;
import com.prinego.domain.entity.ontology.location.Location;
import com.prinego.domain.entity.ontology.medium.Picture;
import com.prinego.domain.entity.ontology.medium.Video;
import com.prinego.domain.entity.response.Response;
import com.prinego.domain.entity.response.reason.RejectedField;
import com.prinego.domain.entity.response.reason.RejectionReason;
import com.prinego.ontology.handler.MyOntologyService;
import com.prinego.ontology.object2ontology.util.IndividualNameCreator;
import com.prinego.ontology.object2ontology.util.IndividualNameResolver;
import com.prinego.ontology.reasoner.dlqueryutil.DLQueryEngine;
import com.prinego.util.globals.AppGlobals;

/**
 * Created by mester on 19/10/14.
 * updated by dilara
 * This class handles the responses sent by the negotiator agent.
 * Actually every ontology related thing goes from here.
 */
@Component
public class MyOntologyReasonerImpl implements MyOntologyReasoner {

	@Inject
	private IndividualNameCreator indNameCreator;
	@Inject
	private IndividualNameResolver indNameResolver;
	@Inject
	private MyOntologyService myOntologyService;


	private final int maxPoint = 10;
	private int numberOfOriginalViolators = 0; 




	/**
	 * Returns response of the negotiator agent according to the post request sent by
	 * initiator agent.
	 * 
	 * This method is used when the negotiation type is default and Such-based.
	 * 
	 * @param ontology ontology of the agent sending response
	 * @param dataFactory the data factory for the ontology.
	 * @param pm Prefix Manager
	 * @param key the key of the instances of the ontology classes e.g. PostRequest_019428293(key)
	 * @param agentUid The responser's id
	 * @param negotiationMethod negotiation type
	 * @param utilityThreshold utility threshold of the responser agent.
	 * 
	 * @return response regarding the post request
	 * */
	@Override
	public Response extractResponse(
			OWLOntology ontology,
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			String agentUid,
			NegotiationType negotiationMethod,
			double utilityThreshold
			) {
		Preconditions.checkNotNull(ontology);
		Preconditions.checkNotNull(dataFactory);
		Preconditions.checkNotNull(pm);
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(agentUid);
		Preconditions.checkNotNull(negotiationMethod);
		Preconditions.checkNotNull(utilityThreshold);




		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology); // THIS SHOULD BE NONBUFFERING AS HERE!
		
		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
		OWLOntology inferredOntology = ontology ;
   		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner);
        iog.fillOntology(ontManager, inferredOntology);
        
		MyExplanationGenerator meg = new MyExplanationGenerator(inferredOntology);

		DLQueryEngine dlQueryEngine = new DLQueryEngine(reasoner, shortFormProvider);

		String p_indName = indNameCreator.getPostRequestIndName(key);
		OWLNamedIndividual p_ind = myOntologyService.getNamedIndividual(p_indName, dataFactory, pm);
		Preconditions.checkNotNull(p_ind);

		String audience_indName = indNameCreator.getAudienceIndName(key);
		OWLNamedIndividual audience_ind = myOntologyService.getNamedIndividual(audience_indName, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind);

		Set<OWLNamedIndividual> rejectingAgentSet = dlQueryEngine.getObjectPropertyValues(p_ind, AppGlobals.OBJ_PROP_NAME_REJECTS);	//TODO date should be converted to object property 

		if(NegotiationType.DEFAULT.equals(negotiationMethod)){

			boolean isRejected = findIsRejected(rejectingAgentSet);

			Response response = new Response();
			response.setOwner(agentUid);
			response.setResponseCode(isRejected ? "N" : "Y");

			if (!isRejected) {
				return response;
			} else { //send every rejected person as response
				RejectionReason reason = prepareRejectionReason(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, p_ind);
				response.setReason(reason);
				return response;
			}
		}else if(NegotiationType.SUCH_BASED.equals(negotiationMethod)){
			boolean isRejected = findIsRejected(rejectingAgentSet);

			Response response = new Response();
			response.setOwner(agentUid);
			response.setResponseCode(isRejected ? "N" : "Y");

			if (!isRejected) {
				return response;
			} else { //send every rejection reason
				RejectionReason reason = prepareAllRejectionReasons(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, p_ind);
				reason.setRejectedPeoplebyImportance(allRejectedPeoplebyImportanceAudience(rejectingAgentSet, dataFactory, pm, meg,key,dlQueryEngine));
				response.setReason(reason);
				return response;
			}
		}else{
			return null;
		}





	} // end of extractResponse method

	/**
	 * Returns response of the negotiator agent according to the post request sent by
	 * initiator agent.
	 * 
	 * This method is used when the negotiation type is GEP or MP.
	 * 
	 * @param ontology ontology of the agent sending response
	 * @param dataFactory the data factory for the ontology.
	 * @param pm Prefix Manager
	 * @param key the key of the instances of the ontology classes e.g. PostRequest_019428293(key)
	 * @param agentUid The responser's id
	 * @param negotiationMethod negotiation type
	 * @param utilityThreshold utility threshold of the responser agent.
	 * @param prevResponses previous sent by this agent in the negotiation
	 * 
	 * @return response regarding the post request
	 * */
	@Override
	public Response extractResponse(
			OWLOntology ontology,
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			String agentUid,
			NegotiationType negotiationMethod,
			double utilityThreshold,
			List<Response> prevResponses
			) {
		Preconditions.checkNotNull(ontology);
		Preconditions.checkNotNull(dataFactory);
		Preconditions.checkNotNull(pm);
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(agentUid);
		Preconditions.checkNotNull(negotiationMethod);
		Preconditions.checkNotNull(utilityThreshold);
		Preconditions.checkNotNull(prevResponses);


		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology); // THIS SHOULD BE NONBUFFERING AS HERE!
		
		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
		OWLOntology inferredOntology = ontology ;
   		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner);
        iog.fillOntology(ontManager, inferredOntology);
        
		MyExplanationGenerator meg = new MyExplanationGenerator(inferredOntology);
		
		DLQueryEngine dlQueryEngine = new DLQueryEngine(reasoner, shortFormProvider);

		String p_indName = indNameCreator.getPostRequestIndName(key);
		OWLNamedIndividual p_ind = myOntologyService.getNamedIndividual(p_indName, dataFactory, pm);
		Preconditions.checkNotNull(p_ind);
		
		String audience_indName = indNameCreator.getAudienceIndName(key);
		OWLNamedIndividual audience_ind = myOntologyService.getNamedIndividual(audience_indName, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind);

		Set<OWLNamedIndividual> rejectingAgentSet = dlQueryEngine.getObjectPropertyValues(p_ind, AppGlobals.OBJ_PROP_NAME_REJECTS);	//TODO date should be converted to object property 

		if(NegotiationType.MP.equals(negotiationMethod)){

			boolean isRejected = findIsRejected(rejectingAgentSet);

			Response response = new Response();
			response.setOwner(agentUid);
			response.setResponseCode(isRejected ? "N" : "Y");

			if (!isRejected) {
				return response;
			} else {
				if(prevResponses.isEmpty()){
					//send all the reasons 
					RejectionReason reason = prepareAllRejectionReasons(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, p_ind);
					reason.setRejectedPeoplebyImportance(allRejectedPeoplebyImportanceAudience(rejectingAgentSet, dataFactory, pm, meg,key,dlQueryEngine));
					reason.setOriginalViolators(numberOfOriginalViolators);//this step is important, we need to know the original violators for utility calculation.
					response.setReason(reason);

					return response;
				}else{
					//look at the previous offers made by the agent itself and also post request
					numberOfOriginalViolators = prevResponses.get(0).getReason().getOriginalViolators();
					System.out.println(" number of origina "+numberOfOriginalViolators);
					RejectionReason reason = prepareRejectionReason(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, p_ind,audience_ind,prevResponses,rejectingAgentSet,meg,utilityThreshold);
					if(reason == null){
						response.setResponseCode("Y");
					
					}else{
						response.setReason(reason);

					}
					System.out.println("combined response "+response);
					System.out.println("response code "+response.getResponseCode());
					return response;

				}

			}

		}else if(NegotiationType.GEP.equals(negotiationMethod)){
			System.out.println("new utility based");
			Response response = new Response();
			response.setOwner(agentUid);

			boolean isRejected = findIsRejected(rejectingAgentSet);
			if(!isRejected){

				response.setResponseCode(isRejected ? "N" : "Y");
				return response;

			}else{
				List<String> allRejections = new ArrayList<String>();
				if(prevResponses.isEmpty()){
					allRejections = allRejectedPeoplebyImportanceAudience(rejectingAgentSet, dataFactory, pm, meg,key,dlQueryEngine);
				}else{
					numberOfOriginalViolators = prevResponses.get(0).getReason().getOriginalViolators();
				}
					
				SWRLRule mostImp = findMostImportantRule(rejectingAgentSet, audience_ind, dataFactory, pm, meg,utilityThreshold, dlQueryEngine);
				
				if(mostImp==null){
					isRejected = false;
					response.setResponseCode(isRejected ? "N" : "Y");
					RejectionReason rj = new RejectionReason();
					rj.setRejectedPeoplebyImportance(allRejections);
					response.setReason(rj);
					return response;
				}else{
					
					RejectionReason reason = prepareRejectionReason(rejectingAgentSet,dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, audience_ind, mostImp, meg); ; // add rejection reason
					
					if(prevResponses.isEmpty()){
						reason.setOriginalViolators(numberOfOriginalViolators);
						reason.setRejectedPeoplebyImportance(allRejections);
						System.out.println("original violators "+numberOfOriginalViolators);
					}
					
					response.setResponseCode(isRejected ? "N" : "Y");
					response.setReason(reason);
					return response;
				}
			}
		}else{
			return null;
		}
	} 


	/**
	 * Returns response of the negotiator agent according to the post request sent by
	 * initiator agent.
	 * 
	 * This method is used when the negotiation type is point-based.
	 * 
	 * @param ontology ontology of the agent sending response
	 * @param dataFactory the data factory for the ontology.
	 * @param pm Prefix Manager
	 * @param key the key of the instances of the ontology classes e.g. PostRequest_019428293(key)
	 * @param agentUid The responser's id
	 * @param negotiationMethod negotiation type
	 * @param utilityThreshold utility threshold of the responser agent.
	 * @param prevResponses previous sent by this agent in the negotiation
	 * @param pointOffer the offer of the initiator agent
	 * @param pointWish the point wish of the negotiator agent
	 * @param myPoints the points of the negotiator agent 
	 * 
	 * @return response regarding the post request
	 * */
	@Override
	public Response extractResponse(OWLOntology ontology,
			OWLDataFactory dataFactory, PrefixOWLOntologyFormat pm, String key,
			String agentUid, NegotiationType negotiationMethod,
			double utilityThreshold, List<Response> prevResponses,
			int pointOffer,
			double pointWish,
			int myPoints) {

		if(!NegotiationType.RPG.equals(negotiationMethod) &&
				!NegotiationType.RPM.equals(negotiationMethod))
			return null;

		Preconditions.checkNotNull(ontology);
		Preconditions.checkNotNull(dataFactory);
		Preconditions.checkNotNull(pm);
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(agentUid);
		Preconditions.checkNotNull(negotiationMethod);
		Preconditions.checkNotNull(utilityThreshold);
		Preconditions.checkNotNull(prevResponses);


		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology); // THIS SHOULD BE NONBUFFERING AS HERE!
		
		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
		OWLOntology inferredOntology = ontology ;
   		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner);
        iog.fillOntology(ontManager, inferredOntology);
        
		MyExplanationGenerator meg = new MyExplanationGenerator(inferredOntology);
		
		DLQueryEngine dlQueryEngine = new DLQueryEngine(reasoner, shortFormProvider);

		String p_indName = indNameCreator.getPostRequestIndName(key);
		OWLNamedIndividual p_ind = myOntologyService.getNamedIndividual(p_indName, dataFactory, pm);
		Preconditions.checkNotNull(p_ind);

		String audience_indName = indNameCreator.getAudienceIndName(key);
		OWLNamedIndividual audience_ind = myOntologyService.getNamedIndividual(audience_indName, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind);

		Set<OWLNamedIndividual> rejectingAgentSet = dlQueryEngine.getObjectPropertyValues(p_ind, AppGlobals.OBJ_PROP_NAME_REJECTS);	//TODO date should be converted to object property 
		System.out.println("rejected ? " +rejectingAgentSet.size());
//		Iterator<OWLNamedIndividual> iter = rejectingAgentSet.iterator();

		boolean isRejected = findIsRejected(rejectingAgentSet);

		Response response = new Response();
		response.setOwner(agentUid);
		response.setResponseCode(isRejected ? "N" : "Y");
		long start = System.currentTimeMillis();
		if (!isRejected) {
			return response;
		} else {

			if(prevResponses.size()==0){

				response.setReason(prepareAllRejectionReasons(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, p_ind));
				System.out.println("all rejection reasons "+(System.currentTimeMillis()-start));
				start = System.currentTimeMillis();
				response.getReason().setRejectedPeoplebyImportance(allRejectedPeoplebyImportanceAudience(rejectingAgentSet, dataFactory, pm, meg,key,dlQueryEngine));
				response.getReason().setOriginalViolators(numberOfOriginalViolators);
				System.out.println("set original violators "+numberOfOriginalViolators);
				System.out.println("ordered by importance "+(System.currentTimeMillis()-start));
				start = System.currentTimeMillis();
				response.setPointOffer(10);
				
			}else{
				//look at the previous offers made by the agent itself and also post request
				//do not add new responses only send the point you would like
				numberOfOriginalViolators = prevResponses.get(0).getReason().getOriginalViolators();
				System.out.println("number of original violators "+numberOfOriginalViolators);
				start = System.currentTimeMillis();
				Response newResponse = prepareResponse(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, 
						prevResponses,rejectingAgentSet,pointOffer,myPoints,pointWish,utilityThreshold, audience_ind,meg);
				
				System.out.println("prepare Response "+(System.currentTimeMillis()-start));
				
				
				if(isReasonNull(newResponse.getReason()) && newResponse.getPointOffer()==pointOffer){
					response.setResponseCode("Y");					
				}else{
					if(newResponse.getResponseCode()!=null)
						response.setResponseCode(newResponse.getResponseCode());
					response.setReason(newResponse.getReason());
					response.setPointOffer(newResponse.getPointOffer());
				}
			}

			System.out.println("INCLUDED AGENT RESPONSE = "+response);
			return response;



		}

	}


	/**
	 * Prepares response of the negotiator agent according to the post request sent by
	 * initiator agent.
	 * 
	 * This method is used when the negotiation type is point-based.
	 * 
	 * @param dataFactory the data factory for the ontology.
	 * @param pm Prefix Manager
	 * @param key the key of the instances of the ontology classes e.g. PostRequest_019428293(key)
	 * @param agentUid The responser's id
	 * @param shortFormProvider Not used actually
	 * @param dlQueryEngine provides answers to ontology queries
	 * @param prevResponses previous sent by this agent in the negotiation
	 * @param rejectingAgentSet the users that has swrl rules fired by the post request
	 * @param opponentPointOffer the point offer of the opponent
	 * @param myPoint the points of the negotiator agent 
	 * @param pointWish the point wish of the negotiator agent
	 * @param utilityThreshold utility threshold of the responser agent.
	 * @param audience_ind the instance of the audience of post request
	 * @param meg My Explanation Generator(instead of pellet's explanation generator-it was too slow)
	 * 
	 * 
	 * @return response regarding the post request
	 * */
	private Response prepareResponse(
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm, 
			String key, 
			String agentUid,
			ShortFormProvider shortFormProvider, 
			DLQueryEngine dlQueryEngine,
			List<Response> prevResponses,
			Set<OWLNamedIndividual> rejectingAgentSet,
			int opponentPointOffer,
			int myPoint,
			double pointWish,
			double utilityThreshold,
			OWLNamedIndividual audience_ind,
			MyExplanationGenerator meg) {

		/*if the offered point is enough to compensate utility deficit,
		 * we accept the offer. Otherwise calculate the points needed to
		 * compensate it and send it as a response.*/
		Response response = new Response();
		double privacyUtility = calculateUtility(rejectingAgentSet,  dataFactory, pm, utilityThreshold,
				dlQueryEngine, audience_ind,meg);
		double totalUtility = privacyUtility + opponentPointOffer*pointWish/5;
		System.out.println("total ut "+totalUtility );
		if(totalUtility>=utilityThreshold){
			System.out.println("YESSS");
			response.setResponseCode("Y");
			return response;
		}

		System.out.println("privacy utility "+privacyUtility);

		int desiredPoint = (int)(utilityThreshold*5/pointWish - (int)(privacyUtility*5/pointWish));
		System.out.println("desired point "+desiredPoint);
		System.out.println("my point "+myPoint);
		
		response.setPointOffer(desiredPoint);
		response.setReason(new RejectionReason());
		return response;
		
	}




	/**
	 * This method sends every rejected audience by the negotiator agent
	 * ordered by their importance to it.
	 * 
	 * Also find the number of original violators (sometimes can be used only for that)
	 * 
	 * */
	private List<String> allRejectedPeoplebyImportanceAudience(
			Set<OWLNamedIndividual> rejectingAgentSet,
			 OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm, MyExplanationGenerator meg,
			 String key, DLQueryEngine dlQueryEngine) {

		List<String> toReturn = new ArrayList<String>();
		Iterator <OWLNamedIndividual> iter = rejectingAgentSet.iterator();
		OWLNamedIndividual owner = iter.next();

		
		HashMap<String,Integer> affectedPeople = new HashMap<String,Integer>();
		String audience_indName = indNameCreator.getAudienceIndName(key);
		OWLNamedIndividual audience_ind = myOntologyService.getNamedIndividual(audience_indName, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind);
		
		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(audience_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);

		Iterator<OWLNamedIndividual> it = becauseOfSet.iterator();
		int violatingAgents = 0;
		//For every rejected agent, find the fired rules because of it. Then calculate importance with them.
		while(it.hasNext()){

			OWLNamedIndividual becauseOfAgent = (OWLNamedIndividual) it.next();
			Set<SWRLRule> firedRules = meg.findTheFiredRules(owner, becauseOfAgent);
			Iterator<SWRLRule> it1 = firedRules.iterator();
			violatingAgents+=firedRules.size();
			System.out.println("fired rules "+firedRules.size());
			while(it1.hasNext()){




				SWRLRule swl = (SWRLRule) it1.next();
				Set <OWLAnnotation> ann = swl.getAnnotations();
				Iterator<OWLAnnotation> it4 = ann.iterator();

				while(it4.hasNext()){

					OWLAnnotation annotation = it4.next();

					if(annotation.getProperty().toString().contains("weight")){

						OWLLiteral lit = (OWLLiteral) annotation.getValue();
						int weight = Integer.parseInt(lit.getLiteral().toString());
						if(affectedPeople.containsKey(becauseOfAgent.getIRI().getShortForm())){
							
							String aff = becauseOfAgent.getIRI().getShortForm();
							
							System.out.println(aff);
							affectedPeople.put(aff,affectedPeople.get(aff)+weight);
						}else{
							
							
							String aff = becauseOfAgent.getIRI().getShortForm();
							
							System.out.println(aff);
							affectedPeople.put(aff,weight);
						}

					}
				}

			}

		}
		

		numberOfOriginalViolators = violatingAgents;


		Iterator<String> iterator = affectedPeople.keySet().iterator();
		//treemap ensures that the map is ordered according to the key values. Actually keeps the reversed affected people
		//so we can order people according to their importance.
		Map<Integer,Set<String>> mostImportantPeople = new TreeMap<Integer,Set<String>>(Collections.reverseOrder());

		while(iterator.hasNext()){
			String name = iterator.next();
			int importance = affectedPeople.get(name);
			
			if(mostImportantPeople.containsKey(importance)){
				Set<String> tempSet = mostImportantPeople.get(importance);
				tempSet.add(name);
				mostImportantPeople.put(importance, tempSet);
			}else{
				Set<String> tempSet = new HashSet<String>();
				tempSet.add(name);
				mostImportantPeople.put(importance, tempSet);
			}
		}

		
		for (Entry<Integer, Set<String>> entry : mostImportantPeople.entrySet()) {
			System.out.println("point of person "+entry.getKey());
			Set<String> curValue = entry.getValue();
			toReturn.addAll(curValue);
		}
		System.out.println("people by importance "+toReturn.size());
		return toReturn;
	}

	/**
	 * calculates the utility of the given post request by the initiator agent.
	 * gets the fired rules and the amount of the affected people by the rule. 
	 * 
	 * */
	private double calculateUtility(
			Set<OWLNamedIndividual> rejectingAgentSet,
			OWLDataFactory factory, 
			PrefixOWLOntologyFormat pm, 
			double utilityThreshold,
			DLQueryEngine dlQueryEngine,
			OWLNamedIndividual audience_ind,
			MyExplanationGenerator meg) {
		Iterator <OWLNamedIndividual> iter = rejectingAgentSet.iterator();

		OWLNamedIndividual owner = iter.next();

		HashMap<SWRLRule,int[]> rules = new HashMap<SWRLRule,int[]>();

		
		int violatingAgents = 0;


		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(audience_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);

		Iterator<OWLNamedIndividual> it = becauseOfSet.iterator();

		while(it.hasNext()){

			OWLNamedIndividual becauseOfAgent = (OWLNamedIndividual) it.next();
			Set<SWRLRule> firedRules = meg.findTheFiredRules(owner, becauseOfAgent);
			Iterator<SWRLRule> it1 = firedRules.iterator();

			while(it1.hasNext()){


				SWRLRule swl = it1.next();
				violatingAgents++;
				boolean mediumRule = swl.getSimplified().toString().contains("isDisliked")||
						swl.getSimplified().toString().contains("hasDateTaken"); //that means medium needs to change
				Set <OWLAnnotation> ann = swl.getAnnotations();
				Iterator<OWLAnnotation> it3 = ann.iterator();

				while(it3.hasNext()){

					OWLAnnotation annotation = it3.next();

					if(annotation.getProperty().toString().contains("weight")){

						OWLLiteral lit = (OWLLiteral) annotation.getValue();

						if(rules.containsKey(swl)){
							int[] a = rules.get(swl);
							a[1] = a[1]+1;
							rules.put(swl, a);
						}else{
							int[] a = new int[3];
							a[0] = Integer.parseInt(lit.getLiteral().toString());
							a[1] = 1;
							if(mediumRule){
								a[2] = 1;
							}else{
								a[2] = 0;
							}

							rules.put(swl, a);
						}

					}
				}
			}

		}

		//System.out.println(violatingAgents);

		double toReturn =calculateUtility(rules,violatingAgents);
		return toReturn;

	}

	/**
	 * Given two post requests, initial and final, sends the utility of the final post request.
	 * @param key  the key of the initial postrequest's instances.
	 * @param key2 the key of the final postrequest's instances.
	 * 
	 * @return utility of the final postrequest.
	 * */
	@Override
	public double extractUtility(OWLOntology ontology,
			OWLDataFactory dataFactory, PrefixOWLOntologyFormat pm, String key,
			String key2, String agentUid) {

		Preconditions.checkNotNull(ontology);
		Preconditions.checkNotNull(dataFactory);
		Preconditions.checkNotNull(pm);
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(agentUid);
		Preconditions.checkNotNull(key2);


		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology); // THIS SHOULD BE NONBUFFERING AS HERE!
		
		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
		OWLOntology inferredOntology = ontology ;
   		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner);
        iog.fillOntology(ontManager, inferredOntology); //this one fills the ontology with the inferred axioms.
        //this one was required because of some complications of the ontology.getAxioms(some object property)
        //methods to not work in the API.
        
		MyExplanationGenerator meg = new MyExplanationGenerator(inferredOntology);
		
		DLQueryEngine dlQueryEngine = new DLQueryEngine(reasoner, shortFormProvider);

		String p_indName = indNameCreator.getPostRequestIndName(key);
		OWLNamedIndividual p_ind = myOntologyService.getNamedIndividual(p_indName, dataFactory, pm);
		Preconditions.checkNotNull(p_ind);

		String audience_indName = indNameCreator.getAudienceIndName(key);
		OWLNamedIndividual audience_ind = myOntologyService.getNamedIndividual(audience_indName, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind);

		String p_indName2 = indNameCreator.getPostRequestIndName(key2);
		OWLNamedIndividual p_ind2 = myOntologyService.getNamedIndividual(p_indName2, dataFactory, pm);
		Preconditions.checkNotNull(p_ind2);
		
		String audience_indName2 = indNameCreator.getAudienceIndName(key2);
		OWLNamedIndividual audience_ind2 = myOntologyService.getNamedIndividual(audience_indName2, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind2);


		Set<OWLNamedIndividual> rejectingAgentSet = dlQueryEngine.getObjectPropertyValues(p_ind, AppGlobals.OBJ_PROP_NAME_REJECTS);	//TODO date should be converted to object property 

		//all rejection reasons of the first post request, used for understanding if there was a rejection originally
		RejectionReason rr  = prepareAllRejectionReasons(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, p_ind);
		//this is only used to find the # of originalViolators.
		allRejectedPeoplebyImportanceAudience(rejectingAgentSet, dataFactory, pm, meg,key,dlQueryEngine);
		

		if(rr==null){
			return 1;
		}

		return calculateUtility(rejectingAgentSet, dataFactory, pm, 0,
				dlQueryEngine, audience_ind2,  meg);


	}


	private boolean isReasonNull(RejectionReason reason) {
		RejectionReason newReason = new RejectionReason();


		return newReason.equals(reason);
	}

	private boolean findIsRejected(Set<OWLNamedIndividual> rejectingAgentSet) {
		Preconditions.checkNotNull(rejectingAgentSet);

		if (CollectionUtils.isEmpty(rejectingAgentSet)) {
			return false;
		} else if (rejectingAgentSet.size() == 1) {
			return true;
		} else {
			Preconditions.checkArgument(false); //rejectingAgentSet should not be more than 1, so throw error.
			return false;
		}
	}

	/**
	 * Given the post request, find the most important rule by calculating the
	 * individual utilities of them.
	 * */
	private SWRLRule findMostImportantRule(Set<OWLNamedIndividual> rejectingAgentSet,OWLNamedIndividual audience_ind,
			OWLDataFactory factory, PrefixOWLOntologyFormat pm, MyExplanationGenerator meg, 
			double utilityThreshold,DLQueryEngine dlQueryEngine) {

		Preconditions.checkNotNull(rejectingAgentSet);
		Preconditions.checkNotNull(audience_ind);
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(pm);
		Preconditions.checkNotNull(meg);
		Preconditions.checkNotNull(utilityThreshold);


		System.out.println("find the most important rule");

		Iterator <OWLNamedIndividual> iter = rejectingAgentSet.iterator();
		OWLNamedIndividual owner = iter.next();
		System.out.println("rejecting agent "+owner.getIRI().getShortForm());
		HashMap<SWRLRule,int[]> rules = new HashMap<SWRLRule,int[]>();

		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(audience_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);
		System.out.println("because of set "+becauseOfSet.size());
		Iterator<OWLNamedIndividual> it = becauseOfSet.iterator();
		int violatingAgents = 0;
		while(it.hasNext()){

			OWLNamedIndividual becauseOfAgent = (OWLNamedIndividual) it.next();
			System.out.println("agent name "+becauseOfAgent.getIRI().getShortForm());
			Set<SWRLRule> firedRules = meg.findTheFiredRules(owner, becauseOfAgent);
			System.out.println("fired rules "+firedRules.size());
			Iterator<SWRLRule> it1 = firedRules.iterator();
			violatingAgents+=firedRules.size();
			while(it1.hasNext()){

				SWRLRule swl = (SWRLRule) it1.next();
				
				boolean mediumRule = swl.getSimplified().toString().contains("isDisliked")||
						swl.getSimplified().toString().contains("hasDateTaken"); //that means medium needs to change
				Set <OWLAnnotation> ann = swl.getAnnotations();
				Iterator<OWLAnnotation> it3 = ann.iterator();

				while(it3.hasNext()){

					OWLAnnotation annotation = it3.next();

					if(annotation.getProperty().toString().contains("weight")){

						OWLLiteral lit = (OWLLiteral) annotation.getValue();

						if(rules.containsKey(swl)){
							int[] a = rules.get(swl);
							a[1] = a[1]+1;
							rules.put(swl, a);
						}else{
							int[] a = new int[3];
							a[0] = Integer.parseInt(lit.getLiteral().toString());
							a[1] = 1;
							if(mediumRule){
								a[2] = 1;
							}else{
								a[2] = 0;
							}

							rules.put(swl, a);
						}

					}
				}

			}
		}


		double utility  = calculateUtility(rules,violatingAgents);

		System.out.println("calculated utility " + utility);
		System.out.println("utility threshold" + utilityThreshold);

		if(utility>utilityThreshold){
			return null;
		}else{
			Iterator<SWRLRule> iterator = rules.keySet().iterator();
			double maxValue = 0;
			SWRLRule mostImportant = null;
			while(iterator.hasNext()){
				SWRLRule temp = (SWRLRule) iterator.next();
				double individualUtility = individualUtility(rules.get(temp));
				if(maxValue < individualUtility ){
					maxValue = individualUtility;
					mostImportant = temp;
				}

			}

			return mostImportant;

		}


	}


	/**
	 * Returns utility according to the rules, their affected agent amount and the negotiation type.
	 * */
	private double calculateUtility(HashMap<SWRLRule, int[]> rules,int violatingAgents) {
		// for this method we need to find a good utility function.
		//utility values are between 0 and 1 , 1 being the most desired result.
		//enable variability in the future.
		int maxRuleWeight = 10;

		double maxUtility = 1.0;
		double utilitySum = 0;


		Iterator<SWRLRule> iterator = rules.keySet().iterator();
		System.out.println("rules size "+rules.size());
		while(iterator.hasNext()){

			SWRLRule temp = (SWRLRule) iterator.next();
			utilitySum+= individualUtility(rules.get(temp));
			System.out.println("rule "+temp.getSimplified().toString()+" \n and total utility "+utilitySum);
		}
		System.out.println("original violators " + numberOfOriginalViolators);

		return maxUtility - (utilitySum / (maxRuleWeight*numberOfOriginalViolators));
	}

	/**
	 * Individual utility of a given rule based on their weight and the amount of affected people.
	 * */
	private double individualUtility(int[] array) {
		
		return array[0]*array[1];     // enable variability in the future.
		
	}

	/**
	 * Eliminate the lowest important violation in a rejection reason.
	 * This method is used with MP, when you remove rules one-by-one from the lowest.
	 * 
	 * */
	private RejectionReason eliminateLowestViolations(
			String key,
			String agentUid, 
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine, 
			Set<OWLNamedIndividual> rejectingAgentSet,
			OWLNamedIndividual audience_ind, 
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm, 
			MyExplanationGenerator meg,
			int iteration,
			double utilityThreshold) {

		System.out.println("eliminating lowest rules");
		RejectionReason rejReason = new RejectionReason();
		//given the number of iterations, returns the set of important rules
		Set<SWRLRule> mostImportantRules = mostImportantRules(rejectingAgentSet, audience_ind, dataFactory, pm, meg,dlQueryEngine, iteration,utilityThreshold); 
		System.out.println("came back from most important rules");
		if(mostImportantRules==null){
			return null;
		}else if(mostImportantRules.isEmpty()){
			rejReason.setField(RejectedField.COMBINED);//this was required since it caused complications in revise method in skeleton.
			return rejReason;
		}else{
		Iterator it = mostImportantRules.iterator();

		while(it.hasNext()){//for every rule combine the rejected audience.
			SWRLRule curRule = (SWRLRule) it.next();
			System.out.println("important rule "+curRule);
			RejectionReason curReason = prepareRejectionReason(rejectingAgentSet,dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, audience_ind, curRule, meg); ; 
			rejReason = combineReasons(rejReason,curReason);			 //burası redundant mi acaba?	
			System.out.println("came back from combining "+rejReason);
		}


		return rejReason;
		}
	}

	/**
	 * This method combines multiple SWRL rules as a rejection reason.
	 * When the response needs to have more than one rejection reason,
	 * this combines them.
	 * */
	private RejectionReason combineReasons(RejectionReason rejReason,
			RejectionReason curReason) {

		RejectionReason combinedReason =  rejReason;
		combinedReason.setField(RejectedField.COMBINED);

		Set<String> includedPeople = combinedReason.getIncludedPeople();
		if(curReason.getField() == RejectedField.AUDIENCE) includedPeople.addAll(curReason.getIncludedPeople());

		if(curReason.getField() == RejectedField.MEDIUM){
			includedPeople.addAll(curReason.getIncludedPeople());
			combinedReason.getIncludedLocations().addAll(curReason.getIncludedLocations());
			if(curReason.isDateTakenDislike()) combinedReason.setDateTakenDislike(true);
			if(curReason.isSelfDislike()) combinedReason.setSelfDislike(true);
			if(curReason.getContextDislike() != null && combinedReason.getContextDislike() == null) combinedReason.setContextDislike(curReason.getContextDislike());

		}

		if(curReason.getField() == RejectedField.POST_TEXT) {
			combinedReason.getMentionedLocations().addAll(curReason.getMentionedLocations());
			combinedReason.getMentionedPeople().addAll(curReason.getMentionedPeople());

		}

		combinedReason.setIncludedPeople(includedPeople);	

		return combinedReason;
	}

	/**
	 * Given the iteration number i, sends the most important
	 * i rules as a set. 
	 * */
	private Set<SWRLRule> mostImportantRules(
			Set<OWLNamedIndividual> rejectingAgentSet,
			OWLNamedIndividual audience_ind,
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm, 
			MyExplanationGenerator meg,
			DLQueryEngine dlQueryEngine,
			int iteration,
			double utilityThreshold) {

		Preconditions.checkNotNull(rejectingAgentSet);
		Preconditions.checkNotNull(audience_ind);
		Preconditions.checkNotNull(dataFactory);
		Preconditions.checkNotNull(pm);
		Preconditions.checkNotNull(meg);

		Iterator <OWLNamedIndividual> iter = rejectingAgentSet.iterator();
		OWLNamedIndividual owner = iter.next();

		HashMap<SWRLRule,int[]> rules = new HashMap<SWRLRule,int[]>();

		
		System.out.println("agent "+owner.toString());
		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(audience_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);

		Iterator<OWLNamedIndividual> it = becauseOfSet.iterator();
		int violatingAgents = 0;
		long startTime = System.currentTimeMillis();
		while(it.hasNext()){
			startTime = System.currentTimeMillis();
			OWLNamedIndividual becauseOfAgent = (OWLNamedIndividual) it.next();
			Set<SWRLRule> firedRules = meg.findTheFiredRules(owner, becauseOfAgent);
			violatingAgents+=firedRules.size();
			Iterator<SWRLRule> it1 = firedRules.iterator();
			System.out.println("fired rules "+(System.currentTimeMillis()-startTime));
			while(it1.hasNext()){
				SWRLRule swl = (SWRLRule) it1.next();
		
				System.out.println("weight assignment "+swl.getSimplified().toString());

					boolean mediumRule = swl.getSimplified().toString().contains("isDisliked")||
							swl.getSimplified().toString().contains("hasDateTaken"); //that means medium needs to change
					Set <OWLAnnotation> ann = swl.getAnnotations();
					Iterator<OWLAnnotation> it3 = ann.iterator();

					while(it3.hasNext()){

						OWLAnnotation annotation = it3.next();

						if(annotation.getProperty().toString().contains("weight")){

							OWLLiteral lit = (OWLLiteral) annotation.getValue();

							if(rules.containsKey(swl)){
								int[] a = rules.get(swl);
								a[1] = a[1]+1;
								rules.put(swl, a);
							}else{
								int[] a = new int[3];
								a[0] = Integer.parseInt(lit.getLiteral().toString());
								a[1] = 1;
								if(mediumRule){
									a[2] = 1;
								}else{
									a[2] = 0;
								}

								rules.put(swl, a);
							}

						}
					}

				
			}

		}
		
		double utility  = calculateUtility(rules,violatingAgents);

		System.out.println("calculated utility " + utility);
		System.out.println("utility threshold" + utilityThreshold);

		if(utility>utilityThreshold){
			return null;
		}
		Iterator<SWRLRule> iterator = rules.keySet().iterator();
		Map<Double,Set<SWRLRule>> mostImportantRules = new TreeMap<Double,Set<SWRLRule>>();
		System.out.println("violated rules finding most important");

		while(iterator.hasNext()){
			SWRLRule temp = (SWRLRule) iterator.next();
			System.out.println(temp.getSimplified().toString());
			double individualUtility = individualUtility(rules.get(temp));
			System.out.println("individual utility "+individualUtility);

			individualUtility = individualUtility*-1;

			if(mostImportantRules.containsKey(individualUtility)){
				System.out.println("same individual utility");				
				Set<SWRLRule> tempSet = mostImportantRules.get(individualUtility);
				tempSet.add(temp);
				mostImportantRules.put(individualUtility, tempSet);
			}else{
				System.out.println("different individual utility add");				
				Set<SWRLRule> tempSet = new HashSet<SWRLRule>();
				tempSet.add(temp);
				mostImportantRules.put(individualUtility, tempSet);
			}

		}

		Set<SWRLRule> ruleToSend = new HashSet<SWRLRule>();
		int ruleNumber = rules.size()-iteration; //at every iteration we send one less swrl rule as rejection.
		System.out.println("accepted rules "+ruleNumber);
		if(ruleNumber<0){
			return ruleToSend;
		}
		for (Entry<Double, Set<SWRLRule>> entry : mostImportantRules.entrySet()) {

			Set<SWRLRule> curValue = entry.getValue();
			Iterator<SWRLRule> iter2 = curValue.iterator();
			while(iter2.hasNext() && ruleNumber > 0){
				SWRLRule temp = iter2.next();
				ruleNumber--;
				ruleToSend.add(temp);
			}

		}



		return ruleToSend;
	}

	/**
	 * Sends every rejection reason.
	 * */
	private RejectionReason prepareAllRejectionReasons(
			OWLDataFactory dataFactory, 
			PrefixOWLOntologyFormat pm, 
			String key,
			String agentUid, 
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine, 
			OWLNamedIndividual p_ind) {


		Set<OWLNamedIndividual> rejectedInSet = dlQueryEngine.getObjectPropertyValues(p_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_IN);

		RejectionReason audienceReason = null,posttextReason = null ,mediumReason = null;
		if ( isAudienceRejected(shortFormProvider, rejectedInSet) ) {	//TODO Use ontological domain information
			audienceReason = prepareAudienceReason(dataFactory, pm, key, shortFormProvider, dlQueryEngine);
		} 
		
		if ( isMediumRejected(shortFormProvider, rejectedInSet) ) {
			mediumReason = prepareMediumReason(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine);
		} 

		if(audienceReason!=null || posttextReason!=null || mediumReason!=null){
			RejectionReason combinedReason =  new RejectionReason();
			combinedReason.setField(RejectedField.COMBINED);

			Set<String> includedPeople = new HashSet<String>();
			if(audienceReason!=null && !audienceReason.getIncludedPeople().isEmpty()) includedPeople.addAll(audienceReason.getIncludedPeople());

			if(mediumReason!=null){
				combinedReason.setIncludedLocations(mediumReason.getIncludedLocations());

				combinedReason.setDateTakenDislike(mediumReason.isDateTakenDislike());

				combinedReason.setSelfDislike(mediumReason.isSelfDislike());

				combinedReason.setContextDislike(mediumReason.getContextDislike());

				//if(!mediumReason.getIncludedPeople().isEmpty()) includedPeople.addAll(mediumReason.getIncludedPeople());
			}

			combinedReason.setIncludedPeople(includedPeople);

			if(posttextReason!=null){
				combinedReason.setMentionedPeople(posttextReason.getMentionedPeople());

				combinedReason.setMentionedLocations(posttextReason.getMentionedLocations());
			}



			System.out.println("combinedReason "+ combinedReason);

			return combinedReason;
		}else{
			return null;
		}



	}

	/**
	 * RejectionReason generator used by MP method.
	 * */
	private RejectionReason prepareRejectionReason(
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm, 
			String key, 
			String agentUid,
			ShortFormProvider shortFormProvider, 
			DLQueryEngine dlQueryEngine,
			OWLNamedIndividual p_ind, 
			OWLNamedIndividual audience_ind, 
			List<Response> prevResponses,
			Set<OWLNamedIndividual> rejectingAgentSet,
			MyExplanationGenerator meg,
			double utilityThreshold
			) {

		Response response = new Response();
		response.setOwner(agentUid);
		response.setResponseCode("N");
		RejectionReason allReasons = prepareAllRejectionReasons(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine, p_ind);
		response.setReason(allReasons);

		int iteration = prevResponses.size();   

		if(iteration != 0){
			System.out.println("reasons rejected concession");
			response.setReason(eliminateLowestViolations(key, agentUid, shortFormProvider, dlQueryEngine, rejectingAgentSet,audience_ind, dataFactory, pm, meg,iteration,utilityThreshold));

			return response.getReason();

		}else{

			return allReasons;
		}

	}



	/**
	 * RejectionReason generator used by default method.
	 * */
	private RejectionReason prepareRejectionReason(
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			String agentUid,
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine,
			OWLNamedIndividual p_ind
			) {

		Set<OWLNamedIndividual> rejectedInSet = dlQueryEngine.getObjectPropertyValues(p_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_IN);

		if ( isAudienceRejected(shortFormProvider, rejectedInSet) ) {	//TODO Use ontological domain information
			return prepareAudienceReason(dataFactory, pm, key, shortFormProvider, dlQueryEngine);
		}else if ( isMediumRejected(shortFormProvider, rejectedInSet) ) {
			return prepareMediumReason(dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine);
		} else {
			return null; // the reason is not specified, so return null
		}
	}

	/**
	 * RejectionReason generator used by gep method.
	 * */
	private RejectionReason prepareRejectionReason(
			Set<OWLNamedIndividual> rejectingAgentSet,
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			String agentUid,
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine,
			OWLNamedIndividual audience_ind,
			SWRLRule violatedRule,
			MyExplanationGenerator meg) {

		
		
		String p_indName = indNameCreator.getPostRequestIndName(key);
		OWLNamedIndividual p_ind = myOntologyService.getNamedIndividual(p_indName, dataFactory, pm);
		Preconditions.checkNotNull(p_ind);
		
		Set<OWLNamedIndividual> rejectedInSet = dlQueryEngine.getObjectPropertyValues(p_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_IN);

		if ( isAudienceRejected(shortFormProvider, rejectedInSet) ) {	//TODO Use ontological domain information
			return prepareAudienceReason(rejectingAgentSet,dataFactory, pm, key, shortFormProvider, dlQueryEngine, violatedRule,meg);
		} else if ( isMediumRejected(shortFormProvider, rejectedInSet) ) {
			return prepareMediumReason(rejectingAgentSet,dataFactory, pm, key, agentUid, shortFormProvider, dlQueryEngine,violatedRule,meg);
		} else {
			return null; // the reason is not specified, so return null
		}
		

	}



	/**
	 * Given the most important rule, swl, sends the rejection reason
	 * containing the audience affected by this most important rule.
	 * 
	 * */
	private RejectionReason prepareAudienceReason(
			Set<OWLNamedIndividual> rejectingAgentSet,
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine,
			SWRLRule swl,
			MyExplanationGenerator meg) {


		Iterator <OWLNamedIndividual> iter = rejectingAgentSet.iterator();
		OWLNamedIndividual owner = iter.next();
		
		System.out.println("prepare audience reason");
		RejectionReason reason = new RejectionReason();
		reason.setField(RejectedField.AUDIENCE);

		String audience_indName = indNameCreator.getAudienceIndName(key);
		OWLNamedIndividual audience_ind = myOntologyService.getNamedIndividual(audience_indName, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind);

		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(audience_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);

		Iterator<OWLNamedIndividual> it = becauseOfSet.iterator();

		while(it.hasNext()){

			boolean containsMostImportantRule = false;
			OWLNamedIndividual becauseOfAgent = (OWLNamedIndividual) it.next();
			Set<SWRLRule> firedRules = meg.findTheFiredRules(owner, becauseOfAgent);
			Iterator<SWRLRule> it2 = firedRules.iterator();

			
			while(it2.hasNext()){
				SWRLRule cur = it2.next();
				if(cur.equals(swl)){
					containsMostImportantRule = true;
				}
			}

			if(!containsMostImportantRule){
				it.remove();
			}

		}

		Set<String> rejectedPeople = findRejectedPeople(shortFormProvider, becauseOfSet);
		reason.setIncludedPeople(rejectedPeople);

		return reason;
	}

	/**
	 * For default strategy. All rejected audience.
	 * 
	 * */
	private RejectionReason prepareAudienceReason(
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine) {

		RejectionReason reason = new RejectionReason();
		reason.setField(RejectedField.AUDIENCE);

		String audience_indName = indNameCreator.getAudienceIndName(key);
		OWLNamedIndividual audience_ind = myOntologyService.getNamedIndividual(audience_indName, dataFactory, pm);
		Preconditions.checkNotNull(audience_ind);

		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(audience_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);


		Set<String> rejectedPeople = findRejectedPeople(shortFormProvider, becauseOfSet);
		reason.setIncludedPeople(rejectedPeople);

		return reason;
	}


	//not used, still adjusted for most important rule
	private RejectionReason prepareMediumReason(
			Set<OWLNamedIndividual> rejectingAgentSet,
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			String agentUid,
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine,
			SWRLRule swl,
			MyExplanationGenerator meg) {

		RejectionReason reason = new RejectionReason();
		reason.setField(RejectedField.MEDIUM);

		Iterator <OWLNamedIndividual> iter = rejectingAgentSet.iterator();
		OWLNamedIndividual owner = iter.next();
		
		String picture_indName = indNameCreator.getPictureIndName(key); // TODO: if needed code for video as well. For now only pictures are covered.
		OWLNamedIndividual picture_ind = myOntologyService.getNamedIndividual(picture_indName, dataFactory, pm);
		Preconditions.checkNotNull(picture_ind);

		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(picture_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);

		if (isItSelfDislike(agentUid, shortFormProvider, becauseOfSet)) {	//TODO Use ontological domain information
			reason.setSelfDislike(true);
		} else if (isItPeople(shortFormProvider, becauseOfSet)) {
			Iterator it = becauseOfSet.iterator();
			System.out.println(becauseOfSet.toString());
			while(it.hasNext()){

				boolean containsMostImportantRule = false;
				OWLNamedIndividual becauseOfAgent = (OWLNamedIndividual) it.next();
				Set<SWRLRule> firedRules = meg.findTheFiredRules(owner, becauseOfAgent);
				Iterator<SWRLRule> it2 = firedRules.iterator();
				
				while(it2.hasNext()){
					SWRLRule cur = it2.next();
					if(cur.equals(swl)){
			
						containsMostImportantRule = true;
						System.out.println("most important rule is here");

					}
				}

				if(!containsMostImportantRule){
					it.remove();
				}

			}

			System.out.println(becauseOfSet.toString());

			Set<String> rejectedPeople = findRejectedPeople(shortFormProvider, becauseOfSet);

			reason.setIncludedPeople(rejectedPeople);
		} else if (isItDateTaken(dlQueryEngine, picture_ind)) {
			reason.setDateTakenDislike(true);
		} else if (isItLocations(shortFormProvider, becauseOfSet)) {

			Iterator it = becauseOfSet.iterator();
			System.out.println(becauseOfSet.toString());
			while(it.hasNext()){

				boolean containsMostImportantRule = false;
				OWLNamedIndividual becauseOfAgent = (OWLNamedIndividual) it.next();
				Set<SWRLRule> firedRules = meg.findTheFiredRules(owner, becauseOfAgent);
				Iterator<SWRLRule> it2 = firedRules.iterator();
				
				while(it2.hasNext()){
					SWRLRule cur = it2.next();
					if(cur.equals(swl)){
						containsMostImportantRule = true;
						System.out.println("most important rule is here");

					}
				}

				if(!containsMostImportantRule){
					it.remove();
				}

			}

			Set<String> rejectedLocations = findRejectedLocations(shortFormProvider, becauseOfSet);
			reason.setIncludedLocations(rejectedLocations);
		} else {
			String rejectedContext = getRejectedContext(shortFormProvider, becauseOfSet);
			if (StringUtils.isNotEmpty(rejectedContext)) {
				reason.setContextDislike(rejectedContext);
			}
		}

		return reason;
	}

	//still not used
	private RejectionReason prepareMediumReason(
			OWLDataFactory dataFactory,
			PrefixOWLOntologyFormat pm,
			String key,
			String agentUid,
			ShortFormProvider shortFormProvider,
			DLQueryEngine dlQueryEngine) {

		RejectionReason reason = new RejectionReason();
		reason.setField(RejectedField.MEDIUM);

		String picture_indName = indNameCreator.getPictureIndName(key); // TODO: if needed code for video as well. For now only pictures are covered.
		OWLNamedIndividual picture_ind = myOntologyService.getNamedIndividual(picture_indName, dataFactory, pm);
		Preconditions.checkNotNull(picture_ind);

		Set<OWLNamedIndividual> becauseOfSet = dlQueryEngine.getObjectPropertyValues(picture_ind, AppGlobals.OBJ_PROP_NAME_REJECTED_BECAUSE_OF);
		Preconditions.checkNotNull(becauseOfSet);

		if (isItSelfDislike(agentUid, shortFormProvider, becauseOfSet)) {	//TODO Use ontological domain information
			reason.setSelfDislike(true);
		} else if (isItPeople(shortFormProvider, becauseOfSet)) {
			Set<String> rejectedPeople = findRejectedPeople(shortFormProvider, becauseOfSet);
			reason.setIncludedPeople(rejectedPeople);
		} else if (isItDateTaken(dlQueryEngine, picture_ind)) {
			reason.setDateTakenDislike(true);
		} else if (isItLocations(shortFormProvider, becauseOfSet)) {
			Set<String> rejectedLocations = findRejectedLocations(shortFormProvider, becauseOfSet);
			reason.setIncludedLocations(rejectedLocations);
		} else {
			String rejectedContext = getRejectedContext(shortFormProvider, becauseOfSet);
			if (StringUtils.isNotEmpty(rejectedContext)) {
				reason.setContextDislike(rejectedContext);
			}
		}

		return reason;
	}

	private Set<String> findRejectedPeople(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> becauseOfSet) {
		Set<String> rejectedPeople = new HashSet();
		for (OWLNamedIndividual becauseOfInd : becauseOfSet) {
			String becauseOfIndName = shortFormProvider.getShortForm(becauseOfInd);
			Class becauseOfInd_clazz = indNameResolver.findClassOf(becauseOfIndName);
			if (Agent.class.equals(becauseOfInd_clazz)) {
				rejectedPeople.add(indNameResolver.getAgentUid(becauseOfIndName));
			}
		}
		return rejectedPeople;
	}

	private Set<String> findRejectedLocations(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> becauseOfSet) {
		Set<String> rejectedLocations = new HashSet();
		for (OWLNamedIndividual becauseOfInd : becauseOfSet) {
			String becauseOfIndName = shortFormProvider.getShortForm(becauseOfInd);
			Class becauseOfInd_clazz = indNameResolver.findClassOf(becauseOfIndName);
			if (Location.class.equals(becauseOfInd_clazz)) {
				rejectedLocations.add(indNameResolver.getLocation(becauseOfIndName));
			}
		}
		return rejectedLocations;
	}

	private boolean isItSelfDislike(String agentUid, ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> becauseOfSet) {
		boolean isItSelfDislike = false;
		String agentIndName = indNameCreator.getAgentIndName(agentUid);
		for (OWLNamedIndividual becauseOfInd : becauseOfSet) {
			String becauseOfIndName = shortFormProvider.getShortForm(becauseOfInd);
			if (agentIndName.equals(becauseOfIndName)) {
				isItSelfDislike = true;
				break;
			}
		}
		return isItSelfDislike;
	}

	private boolean isItPeople(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> becauseOfSet) {
		boolean isItPeople = false;
		for (OWLNamedIndividual becauseOfInd : becauseOfSet) {
			String becauseOfIndName = shortFormProvider.getShortForm(becauseOfInd);
			if (becauseOfIndName.startsWith("Agent_")) {
				isItPeople = true;
				break;
			}
		}
		return isItPeople;
	}

	private boolean isItLocations(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> becauseOfSet) {
		boolean isItLocations = false;
		for (OWLNamedIndividual becauseOfInd : becauseOfSet) {
			String becauseOfIndName = shortFormProvider.getShortForm(becauseOfInd);
			if (becauseOfIndName.startsWith("Location_")) {
				isItLocations = true;
				break;
			}
		}
		return isItLocations;
	}

	private boolean isItDateTaken(DLQueryEngine dlQueryEngine, OWLNamedIndividual picture_ind) {
		boolean isItDateTaken = false;
		Set<OWLLiteral> becauseOfDateSet = dlQueryEngine.getDataPropertyValues(picture_ind, AppGlobals.DATA_PROP_NAME_REJECTED_BECAUSE_OF_DATE);
		Preconditions.checkNotNull(becauseOfDateSet);

		if ( CollectionUtils.isNotEmpty(becauseOfDateSet) ) {
			isItDateTaken = true;
		}
		return isItDateTaken;
	}

	private String getRejectedContext(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> becauseOfSet) {
		String rejectedContext = null;
		for (OWLNamedIndividual becauseOfInd : becauseOfSet) {
			String becauseOfIndName = shortFormProvider.getShortForm(becauseOfInd);
			if (becauseOfIndName.startsWith("Context_")) {  
				rejectedContext = indNameResolver.getContext(becauseOfIndName);
				break;
			}
		}
		return rejectedContext;
	}

	private boolean isAudienceRejected(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> rejectedInSet) {
		return isFieldRejected(shortFormProvider, rejectedInSet, Audience.class);
	}


	private boolean isMediumRejected(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> rejectedInSet) {
		return isFieldRejected(shortFormProvider, rejectedInSet, Picture.class)
				|| isFieldRejected(shortFormProvider, rejectedInSet, Video.class);
	}

	private boolean isFieldRejected(ShortFormProvider shortFormProvider, Set<OWLNamedIndividual> rejectedInSet, Class fieldClazz) {
		Preconditions.checkNotNull(rejectedInSet);

		Iterator<OWLNamedIndividual> iter = rejectedInSet.iterator();
		while (iter.hasNext()) {
			OWLNamedIndividual rejectedFieldInd = iter.next();
			Class rejectedField_clazz = indNameResolver.findClassOf(shortFormProvider.getShortForm(rejectedFieldInd));

			if (fieldClazz.equals(rejectedField_clazz)) {
				return true;
			}
		}
		return false;
	}

} 