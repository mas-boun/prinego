package com.prinego.domain.entity.ontology.context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.prinego.domain.annotation.MyOntoClass;

import java.io.Serializable;

/**
 * Created by mester on 05/09/14.
 */
@MyOntoClass
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Beach.class),
        @JsonSubTypes.Type(value = ColleagueMeeting.class),
        @JsonSubTypes.Type(value = EatAndDrink.class),
        @JsonSubTypes.Type(value = FriendMeeting.class),
        @JsonSubTypes.Type(value = Leisure.class),
        @JsonSubTypes.Type(value = Meeting.class),
        @JsonSubTypes.Type(value = Party.class),
        @JsonSubTypes.Type(value = ProtestMeeting.class),
        @JsonSubTypes.Type(value = ResearchMeeting.class),
        @JsonSubTypes.Type(value = Sightseeing.class),
        @JsonSubTypes.Type(value = Work.class),
        @JsonSubTypes.Type(value = Vacation.class)
})
public abstract class Context implements Serializable {

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
