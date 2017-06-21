package com.prinego.domain.entity.ontology.location;

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
        @JsonSubTypes.Type(value = Bar.class),
        @JsonSubTypes.Type(value = Cafe.class),
        @JsonSubTypes.Type(value = College.class),
        @JsonSubTypes.Type(value = Museum.class),
        @JsonSubTypes.Type(value = University.class)
})
public abstract class Location implements Serializable {

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
