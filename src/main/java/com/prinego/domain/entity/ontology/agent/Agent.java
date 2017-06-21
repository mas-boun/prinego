package com.prinego.domain.entity.ontology.agent;


import java.io.Serializable;

import com.prinego.domain.annotation.MyOntoClass;
import com.prinego.domain.annotation.MyOntoField;

import lombok.Data;

/**
 * Created by mester on 05/08/14.
 */
@MyOntoClass
public @Data class Agent implements Serializable {

    @MyOntoField(propertyName = "hasUid")
    private String uid; // not-null

    private double utilityThreshold = 0.5;
    
	public Agent() {}

    public Agent(String uid) {
        this.uid = uid;
    }

    // copy constructor
    public Agent(Agent another) {
        this.uid = another.getUid();
        this.utilityThreshold = another.getUtilityThreshold();
    }

    private double getUtilityThreshold() {		
		return this.utilityThreshold;
	}

    public void setUtilityThreshold(double utilityThreshold) {
		this.utilityThreshold = utilityThreshold;
	}

	@Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Agent agent = (Agent) other;

        if (!uid.equals(agent.getUid())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "Agent_" + getUid();
    }

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	
}
