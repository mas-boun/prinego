package com.prinego.domain.entity.ontology.audience;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.prinego.domain.annotation.MyOntoClass;
import com.prinego.domain.annotation.MyOntoField;
import com.prinego.domain.entity.ontology.agent.Agent;

import lombok.Data;

/**
 * Created by mester on 16/08/14.
 */
@MyOntoClass
public @Data class Audience implements Serializable {

    @MyOntoField(propertyName = "hasAudienceMember")
    private Set<Agent> audienceMembers;

    public Audience() {
        audienceMembers = new HashSet();
    }

    // copy constructor
    public Audience(Audience another) {
        this.audienceMembers = new HashSet();
        for ( Agent a : another.getAudienceMembers() ) {
            this.audienceMembers.add(new Agent(a));
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Audience otherAudience = (Audience) other;

        if (getAudienceMembers().equals(otherAudience.getAudienceMembers()) == false) return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Audience:" + "\r\n");
        sb.append("\t\t\t" + "audienceMembers:" + audienceMembers + "\r\n");
        sb.append("]");

        return sb.toString();
    }

	public Set<Agent> getAudienceMembers() {
		return audienceMembers;
	}

	public void setAudienceMembers(Set<Agent> audienceMembers) {
		this.audienceMembers = audienceMembers;
	}

}
