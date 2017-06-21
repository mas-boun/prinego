package com.prinego.domain.entity.ontology.medium;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.prinego.domain.annotation.MyOntoClass;
import com.prinego.domain.annotation.MyOntoField;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.context.Context;
import com.prinego.domain.entity.ontology.event.Event;
import com.prinego.domain.entity.ontology.location.Location;

/**
 * Created by mester on 12/08/14.
 */
@MyOntoClass
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Picture.class),
        @JsonSubTypes.Type(value = Video.class)
})
public @Data abstract class Medium implements Serializable {

    @MyOntoField(propertyName = "hasUrl")
    private String url; // should be unique

    @MyOntoField(propertyName = "hasDateTaken")
    private Date dateTaken;

    @MyOntoField(propertyName = "isDisliked")
    private Boolean isDisliked;
    
    @MyOntoField(propertyName = "hasMatureContent")
    private Boolean hasMatureContent;
    
    @MyOntoField(propertyName = "hasMood")
    private String mood; // should be unique

    /**
     * assumed to be available attributes:
     */
    @MyOntoField(propertyName = "includesPerson")
    private Set<Agent> includedPeople;

    @MyOntoField(propertyName = "includesLocation")
    private Set<Location> includedLocations;

    @MyOntoField(propertyName = "isInContext")
    private Set<Context> isInContexts;
    
    @MyOntoField(propertyName = "isTakenIn")
    private Event isTakenIn;

    public void initialize() {
        url = "";
        includedPeople = new HashSet();
        includedLocations = new HashSet();
        isInContexts = new HashSet();
        isTakenIn = null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Medium otherMedium = (Medium) other;

        if (!url.equals(otherMedium.getUrl())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Medium:" + "\r\n");
        sb.append("\t\t\t" + "url:" + url + "\r\n");
        if( getDateTaken() != null ) {
            sb.append("\t\t\t" + "dateTaken:" + dateTaken + "\r\n");
        }
        if (CollectionUtils.isNotEmpty(includedPeople)) {
            sb.append("\t\t\t" + "includedPeople:" + includedPeople + "\r\n");
        }
        if (CollectionUtils.isNotEmpty(includedLocations)) {
            sb.append("\t\t\t" + "includedLocations:" + includedLocations + "\r\n");
        }
        if (CollectionUtils.isNotEmpty(isInContexts)) {
            sb.append("\t\t\t" + "isInContexts:" + isInContexts + "\r\n");
        }
        if (isTakenIn != null){
        	sb.append("\t\t\t" + "isTakenIn:" + isTakenIn + "\r\n");
        }
        sb.append("]");

        return sb.toString();
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

	public Date getDateTaken() {
		return dateTaken;
	}

	public void setDateTaken(Date dateTaken) {
		this.dateTaken = dateTaken;
	}

	public Boolean getIsDisliked() {
		return isDisliked;
	}

	public void setIsDisliked(Boolean isDisliked) {
		this.isDisliked = isDisliked;
	}
	
	public Boolean getHasMatureContent() {
		return hasMatureContent;
	}

	public void setHasMatureContent(Boolean hasMatureContent) {
		this.hasMatureContent = hasMatureContent;
	}
	
	public Event getIsTakenIn() {
		return isTakenIn;
	}

	public void setIsTakenIn(Event isTakenIn) {
		this.isTakenIn = isTakenIn;
	}

	public Set<Agent> getIncludedPeople() {
		return includedPeople;
	}

	public void setIncludedPeople(Set<Agent> includedPeople) {
		this.includedPeople = includedPeople;
	}

	public Set<Location> getIncludedLocations() {
		return includedLocations;
	}

	public void setIncludedLocations(Set<Location> includedLocations) {
		this.includedLocations = includedLocations;
	}

	public Set<Context> getIsInContexts() {
		return isInContexts;
	}

	public void setIsInContexts(Set<Context> isInContexts) {
		this.isInContexts = isInContexts;
	}

}
