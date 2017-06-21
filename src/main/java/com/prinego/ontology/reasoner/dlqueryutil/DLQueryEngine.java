package com.prinego.ontology.reasoner.dlqueryutil;

import com.prinego.util.globals.AppGlobals;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.util.Collections;
import java.util.Set;

public class DLQueryEngine {
    private final OWLReasoner reasoner;
    private final DLQueryParser parser;
  
    public DLQueryEngine(OWLReasoner reasoner, ShortFormProvider shortFormProvider) {
        this.reasoner = reasoner;
        parser = new DLQueryParser(reasoner.getRootOntology(), shortFormProvider);
    }

    public Set<OWLClass> getSuperClasses(String classExpressionString, boolean direct) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLClass> superClasses = reasoner
                .getSuperClasses(classExpression, direct);
        return superClasses.getFlattened();
    }

    public Set<OWLClass> getEquivalentClasses(String classExpressionString) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(classExpression);
        Set<OWLClass> result = null;
        if (classExpression.isAnonymous()) {
            result = equivalentClasses.getEntities();
        } else {
            result = equivalentClasses.getEntitiesMinus(classExpression.asOWLClass());
        }
        return result;
    }

    public Set<OWLClass> getSubClasses(String classExpressionString, boolean direct) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(classExpression, direct);
        return subClasses.getFlattened();
    }

    public Set<OWLNamedIndividual> getInstances(String classExpressionString,
                                                boolean direct) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(classExpression,
                direct);
        return individuals.getFlattened();
    }

    public Set<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual subject, String objectPropName) {

        OWLObjectProperty objectProperty = parser.getEntityChecker().getOWLObjectProperty(objectPropName);

        OWLObjectPropertyExpression objectPropertyExpression = null;
        if (AppGlobals.OBJ_PROP_NAME_REJECTS.equals(objectPropName)
                || AppGlobals.OBJ_PROP_NAME_REJECTED_IN.equals(objectPropName)) {
            objectPropertyExpression = objectProperty.getInverseProperty();
        } else {
            objectPropertyExpression = objectProperty;
        }

        NodeSet<OWLNamedIndividual> individuals = reasoner.getObjectPropertyValues(subject, objectPropertyExpression);
        return individuals.getFlattened();
    }

    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual subject, String dataPropName) {

        OWLDataProperty dataProperty = parser.getEntityChecker().getOWLDataProperty(dataPropName);
        Set<OWLLiteral> literals = reasoner.getDataPropertyValues(subject, dataProperty);
        return literals;
    }
    
    public OWLObjectProperty getObjectProperty(String objectProperty){
    	return parser.getEntityChecker().getOWLObjectProperty(objectProperty);
    }
   
}