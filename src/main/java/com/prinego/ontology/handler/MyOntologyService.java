package com.prinego.ontology.handler;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.prinego.domain.entity.rule.RuleForPrinego;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.util.List;

/**
 * Created by mester on 24/08/14.
 */
public interface MyOntologyService {

    OWLOntology readMyOntology(OWLOntologyManager ontManager, String owlFilePath);

    OWLNamedIndividual applyClassAssertionFromObject(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            Object obj,
            String indName);

    void applyObjectPropertyAssertion(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_obj,
            String objectPropertyName,
            OWLNamedIndividual ind_field_obj);

    OWLObjectPropertyAssertionAxiom getObjectPropertyAssertionAxiom(
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            OWLNamedIndividual ind_object);

    void applyDataPropertyAssertion(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_obj,
            String dataPropertyName,
            Object data);

    OWLDataPropertyAssertionAxiom getDataPropertyAssertionAxiom(
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            Object data);

    OWLObjectProperty getObjectProperty(
            String propName,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm);

    OWLDataProperty getDataProperty(
            String propName,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm);

    OWLNamedIndividual getNamedIndividual(
            String indName,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm);

    boolean isObjectPropEntailed(
            PelletReasoner reasoner,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            OWLNamedIndividual ind_object);

    boolean isDataPropEntailed(
            PelletReasoner reasoner,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            Object data);

//    void createRuleObjectForTest(
//            OWLOntologyManager ontManager,
//            OWLDataFactory factory,
//            OWLOntology myOntology,
//            PrefixOWLOntologyFormat pm
//    );

    // Dilara's method
    //This method is to use OWL API and create rule from our own Rule object.
    SWRLRule getOWLRepresentation(RuleForPrinego rule);

    RuleForPrinego SWRLtoRule(SWRLRule swl);

    List<SWRLRule> listRules(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm
    );

    boolean createRule(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            SWRLRule rule
    );

    boolean deleteRule(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            SWRLRule rule
    );

}
