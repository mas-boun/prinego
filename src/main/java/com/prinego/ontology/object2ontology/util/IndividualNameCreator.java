package com.prinego.ontology.object2ontology.util;

/**
 * Created by mester on 22/08/14.
 */
public interface IndividualNameCreator {

    String createIndividualName(Object obj, String key);

    String getAgentIndName(String uid);

    String getPictureIndName(String key);

    String getVideoIndName(String key);

    String getAudienceIndName(String key);

    String getPostTextIndName(String key);

    String getPostRequestIndName(String key);

    String getLocationIndName(String key);

    String getContextIndName(String clazzName);

	String getEventIndName(String key);
}
