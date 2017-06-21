package com.prinego.ontology.object2ontology.api;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * Created by mester on 16/08/14.
 */
public interface Object2OntologyService {

    /**
     * @param ontManager
     * @param dataFactory
     * @param myOntology
     * @param pm
     * @param obj
     *
     * @return key
     */
    String upsertMyObject(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            Object obj);

    void upsertMyObjectFields(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            Object obj,
            OWLNamedIndividual ind_obj,
            String key);


}
