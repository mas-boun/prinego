package com.prinego.ontology.object2ontology.util;

import com.google.common.base.Preconditions;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.audience.Audience;
import com.prinego.domain.entity.ontology.context.Context;
import com.prinego.domain.entity.ontology.event.Event;
import com.prinego.domain.entity.ontology.location.Location;
import com.prinego.domain.entity.ontology.medium.Picture;
import com.prinego.domain.entity.ontology.medium.Video;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import org.springframework.stereotype.Component;

/**
 * Created by mester on 19/10/14.
 */
@Component
public class IndividualNameResolverImpl implements IndividualNameResolver {

    @Override
    public Class findClassOf(String ind) {

        Preconditions.checkNotNull(ind);

        if(ind.contains("user"))
        	return Agent.class;
        
        int index_of_underscore = ind.indexOf('_');        
        Preconditions.checkArgument(index_of_underscore > 0);

        String firstWord = ind.substring(0, index_of_underscore);
        if ( "Agent".equals(firstWord) ) {
            return Agent.class;
        } else if ( "Picture".equals(firstWord) ) {
            return Picture.class;
        } else if ( "Video".equals(firstWord) ) {
            return Video.class;
        } else if ( "Audience".equals(firstWord) ) {
            return Audience.class;
        } else if ( "Event".equals(firstWord) ) {
            return Event.class;
        } else if ( "PostRequest".equals(firstWord) ) {
            return PostRequest.class;
        } else if ( "Location".equals(firstWord) ) {
            return Location.class;
        } else if ( "Context".equals(firstWord) ) {
            return Context.class;
        } else {
            return null;
        }
    }

    @Override
    public String getAgentUid(String indName) {

        Preconditions.checkNotNull(indName);

        if(indName.contains("user"))
        	return indName;
        
        int index_of_underscore = indName.indexOf('_');
        Preconditions.checkArgument(index_of_underscore > 0);

        String agentUid = indName.substring(index_of_underscore + 1);

        return agentUid;
    }

    @Override
    public String getLocation(String indName) {

        Preconditions.checkNotNull(indName);

        int index_of_underscore = indName.indexOf('_');
        Preconditions.checkArgument(index_of_underscore > 0);

        String location = indName.substring(index_of_underscore + 1);

        return location;
    }

    @Override
    public String getContext(String indName) {

        Preconditions.checkNotNull(indName);

        int index_of_underscore = indName.indexOf('_');
        Preconditions.checkArgument(index_of_underscore > 0);

        String context = indName.substring(index_of_underscore + 1);

        return context;
    }

}
