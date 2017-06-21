package com.prinego.ontology.object2ontology.util;

/**
 * Created by mester on 22/08/14.
 */
public interface IndividualNameResolver {

    Class findClassOf(String ind);

    String getAgentUid(String indName);

    String getLocation(String indName);

    String getContext(String indName);
}
