package com.prinego.ontology.handler;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.springframework.stereotype.Component;
import uk.ac.manchester.cs.bhig.util.Tree;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

/**
 * Created by mester on 25/08/14.
 */
@Component
public class MyOntologyRendererServiceImpl implements MyOntologyRendererService {

    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    @Override
    public void printIndented(Tree<OWLAxiom> node, String indent) {
        OWLAxiom axiom = node.getUserObject();
        System.out.println(indent + renderer.render(axiom));
        if (!node.isLeaf()) {
            for (Tree<OWLAxiom> child : node.getChildren()) {
                printIndented(child, indent + "    ");
            }
        }
    }

}
