package com.prinego.ontology.reasoner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;

public class MyExplanationGenerator {


	private OWLOntology ontology;
	

	public MyExplanationGenerator(OWLOntology ontology){
		this.ontology = ontology;
		
	}

	public Set<SWRLRule> findTheFiredRules(OWLNamedIndividual owner,OWLNamedIndividual included){
		Set<SWRLRule> toReturn = new HashSet<SWRLRule>();


		Set<OWLAxiom> axioms = ontology.getAxioms();
		Set<SWRLRule> ownerRules = new HashSet<SWRLRule>();
		Iterator<OWLAxiom> it = axioms.iterator();
		while(it.hasNext()){
			OWLAxiom ax = (OWLAxiom) it.next();

			if(ax.getAxiomType().equals(AxiomType.SWRL_RULE)){
				SWRLRule swl = (SWRLRule) ax;
				Iterator<SWRLAtom> it2 = swl.getHead().iterator();
				while(it2.hasNext()){
					SWRLAtom satom = it2.next();
					if(satom.toString().contains(owner.getIRI().getShortForm()) && satom.toString().contains("rejects"))  {
						ownerRules.add(swl);

					}

				}

			}

		}


		//:TODO look if you can use reasoner to get object properties and such 
		//couldnt do it on the swrltry but reasoner seems to work on this code.
		//get all axioms on swrl and look if it exists in the ontology / inferred or not
		for(SWRLRule swl:ownerRules){
			if(appliesTo(swl,owner,included)){
				toReturn.add(swl);
				System.out.println("applies "+swl.getSimplified().toString());
			}				
		}



		return toReturn;
	}

	private boolean appliesTo(SWRLRule swl, OWLNamedIndividual owner,
			OWLNamedIndividual included) {
//		if(swl.toString().contains(included.toString())){
//			return true;
//		}else
		if(hasRequiredRelation(owner,included,swl) &&
				 hasRequiredLocation(swl) &&
				 hasRequiredContext(swl) &&
				 hasRequiredMood(swl) &&
				 hasRequiredMaturity(swl) &&
				 hasRequiredCity(included,swl) &&
				 hasRequiredEvent(included,swl) &&
				 hasRequiredPerson(included,swl)
				){
			return true;

		}
		return false;
	}

	private boolean hasRequiredPerson(OWLNamedIndividual included, SWRLRule swl) {
		
		Iterator<SWRLAtom> it = swl.getHead().iterator();
		while(it.hasNext()){
			SWRLAtom sa = it.next();
			if(sa.toString().contains("rejectedBecauseOf")){
				if(sa.toString().contains(included.toString()))
					return true;
				if(!sa.toString().contains("user") && !sa.toString().contains("Agent") )
					return true;
			}
		}
		return false;
	}

	private boolean hasRequiredEvent(OWLNamedIndividual included, SWRLRule swl) {

		if(!swl.toString().contains("didNotInvite"))
			return true;
		
		Set<OWLAxiom> axioms = ontology.getAxioms();
		
		for(OWLAxiom ax:axioms){
			if(ax.getAxiomType().equals(AxiomType.OBJECT_PROPERTY_ASSERTION) && 
					ax.toString().contains(included.getIRI().getShortForm()) &&
					ax.toString().contains("didNotInvite")&&
					ax.toString().contains(included.getIRI().getShortForm()))
				return true;
		}
		
	
		
		
		return false;
	}

	private boolean hasRequiredCity(OWLNamedIndividual included, SWRLRule swl) {
		
		if(!swl.toString().contains("inCity"))
			return true;
		
		Set<OWLAxiom> axioms = ontology.getAxioms();
		String city ="";
		
		for(OWLAxiom ax:axioms){
			if(ax.getAxiomType().equals(AxiomType.DATA_PROPERTY_ASSERTION) && 
					ax.toString().contains(included.getIRI().getShortForm()) &&
					ax.toString().contains("livesIn"))
				 city = ax.toString().split(" ")[2].split("\"")[1];
		}
		
		for(OWLAxiom ax:axioms){
			if(ax.getAxiomType().equals(AxiomType.DATA_PROPERTY_ASSERTION) && 
					ax.toString().contains("Vacation") &&
					ax.toString().contains("inCity")&&
					ax.toString().contains(city))
				 return true;
		}
		
		
		
				
		return false;
		
		
	}

	private boolean hasRequiredMaturity(SWRLRule swl) {
		
		if(!swl.toString().contains("hasMatureContent"))
			return true;
		
		Set<OWLAxiom> axioms = ontology.getAxioms();
		String mature ="";
		
		for(OWLAxiom ax:axioms){
			if(ax.getAxiomType().equals(AxiomType.DATA_PROPERTY_ASSERTION) && ax.toString().contains("hasMood"))
				 mature = ax.toString().split(" ")[2].split("\"")[1];
		}
		
		if(swl.toString().contains(mature))
			return true;
		
		
		return false;
	}

	private boolean hasRequiredMood(SWRLRule swl) {
		
		if(!swl.toString().contains("hasMood"))
			return true;
		
		Set<OWLAxiom> axioms = ontology.getAxioms();
		String mood ="";
		
		for(OWLAxiom ax:axioms){
			if(ax.getAxiomType().equals(AxiomType.DATA_PROPERTY_ASSERTION) && ax.toString().contains("hasMood"))
				 mood = ax.toString().split(" ")[2].split("\"")[1];
		}
		
		if(swl.toString().contains(mood))
			return true;
		
		
		return false;
	}

	private boolean hasRequiredContext(SWRLRule swl) {
				
		if(!swl.toString().contains("isInContext"))
			return true;
		
		Set<OWLNamedIndividual> instances = ontology.getIndividualsInSignature();

		for(OWLNamedIndividual i:instances){
			if(i.toString().contains("Context")){
				String contextName = i.getIRI().getShortForm().split("_")[1];
				if(swl.toString().contains(contextName))
					return true;
				if(swl.toString().contains("Leisure")&&(contextName.equals("Party")||contextName.equals("Beach")||
						contextName.equals("EatAndDrink")||contextName.equals("Sightseeing")))
					return true;
			}
		}		
		return false;
	}

	private boolean hasRequiredLocation(SWRLRule swl) {
		
		if(!swl.toString().contains("includesLocation"))
			return true;
		
		Set<OWLNamedIndividual> instances = ontology.getIndividualsInSignature();
		
		for(OWLNamedIndividual i:instances){
			if(i.toString().contains("Location")){
				String locationName = i.getIRI().getShortForm().split("_")[1];
				if(swl.toString().contains(locationName))
					return true;
			}

		}
		
		return false;
	}

	
	private boolean hasRequiredRelation(OWLNamedIndividual owner,
			OWLNamedIndividual included,SWRLRule swl) {

		if(noSpecificRelation(swl))
			return true;
		
		Set<OWLObjectPropertyAssertionAxiom> axs = ontology.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION);		

		for(OWLAxiom a:axs){

			if(a.toString().contains(owner.getIRI().getShortForm()) &&
					a.toString().contains(included.getIRI().getShortForm())){
				Set<OWLObjectProperty> prop = a.getObjectPropertiesInSignature();
				for(OWLObjectProperty p:prop){
					if(swl.toString().contains(p.getIRI().getShortForm())){
						return true;
					}

				}
			}
		}
		return false;
	}

	private boolean noSpecificRelation(SWRLRule swl) {
		
		if(swl.toString().contains("isConnectedTo"))
			return false;
		
		if(swl.toString().contains("isPartOfFamilyOf"))
			return false;
		
		if(swl.toString().contains("isWorkRelatedOf"))
			return false;
		
		if(swl.toString().contains("isFriendOf"))
			return false;
		
		if(swl.toString().contains("isColleagueOf"))
			return false;
		
		
		
		return true;
	}

}
