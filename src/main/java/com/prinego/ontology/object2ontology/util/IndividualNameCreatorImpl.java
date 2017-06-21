package com.prinego.ontology.object2ontology.util;

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
 * Created by mester on 22/08/14.
 */
@Component
public class IndividualNameCreatorImpl implements IndividualNameCreator {

    /**
     * INDIVIDUAL NAMING CONVENTIONS:
     * Agent                    --> Agent_UID
     * Medium                   --> individuals not needed
     * Picture                  --> Picture_KEY
     * Video                    --> Video_KEY
     * Audience                 --> Audience_KEY
     * PostText                 --> PostText_KEY
     * PostRequest              --> PostRequest_from_OwnerName_no_KEY
     * Event					--> Event_KEY
     *
     * Ontology Only:
     * Context and subclasses   --> Context_Classname
     * Location and subclasses  --> Location_Classname
     *
     */
	 @Override
    public String createIndividualName(Object obj, String key) {

        if ( obj instanceof Agent) {
            Agent agent = (Agent) obj;
            return getAgentIndName(agent.getUid());
        } else if ( obj instanceof Picture) {
            return getPictureIndName(key);
        } else if ( obj instanceof Video) {
            return getVideoIndName(key);
        } else if ( obj instanceof Audience) {
            return getAudienceIndName(key);
        } else if ( obj instanceof Event) {
            return getEventIndName(key);
        } else if ( obj instanceof PostRequest) {
            PostRequest postRequest = (PostRequest) obj;
            return getPostRequestIndName(key);
        } else if ( obj instanceof Location) {
            return getLocationIndName(obj.getClass().getSimpleName());
        } else if ( obj instanceof Context) {
            return getContextIndName(obj.getClass().getSimpleName());
        } else {
            return null;
        }
    }

    @Override
    public String getAgentIndName(String uid) {
    	if(uid.contains("user"))
    		return uid;
        return "Agent_" + uid;
    }

    @Override
    public String getPictureIndName(String key) {
        return "Picture_" + key;
    }

    @Override
    public String getVideoIndName(String key) {
        return "Video_" + key;
    }

    @Override
    public String getAudienceIndName(String key) {
        return "Audience_" + key;
    }
    
    @Override
    public String getEventIndName(String key) {
        return "Event_" + key;
    }

    @Override
    public String getPostTextIndName(String key) {
        return "PostText_" + key;
    }

    @Override
    public String getPostRequestIndName(String key) {
        return "PostRequest_" + key;
    }

    @Override
    public String getLocationIndName(String clazzName) {
        return "Location_" + clazzName;
    }

    @Override
    public String getContextIndName(String clazzName) {
        return "Context_" + clazzName;
    }

}
