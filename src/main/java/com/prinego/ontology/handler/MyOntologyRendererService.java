package com.prinego.ontology.handler;

import org.semanticweb.owlapi.model.OWLAxiom;
import uk.ac.manchester.cs.bhig.util.Tree;

/**
 * Created by mester on 25/08/14.
 */
public interface MyOntologyRendererService {

    void printIndented(Tree<OWLAxiom> node, String indent);
}
