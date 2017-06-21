package com.prinego.domain.entity.ontology.postrequest;

import com.prinego.domain.annotation.MyOntoClass;
import com.prinego.domain.annotation.MyOntoField;
import com.prinego.domain.entity.negotiation.NegotiationType;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.audience.Audience;
import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.domain.entity.ontology.medium.Picture;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by mester on 05/08/14.
 */
@MyOntoClass
//@XmlRootElement(name="postRequest")
//@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
//@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data class PostRequest implements Serializable {

    @MyOntoField(propertyName = "hasOwner")
    private Agent owner; // not-null

    @MyOntoField(propertyName = "hasMedium")
    private Medium medium;

    @MyOntoField(propertyName = "hasAudience")
    private Audience audience;

    private String exampleName; // added for test purposes
    
    private NegotiationType negotiationMethod = NegotiationType.DEFAULT;  //added for integrating different negotiation methods
    
    
    public PostRequest() { 
    	owner = new Agent();
    
    		
    }

    // copy constructor
    public PostRequest(PostRequest another) {
        this.owner = new Agent(another.getOwner());

        if ( another.getMedium() == null ) {
            this.medium = null;
        } else {
            this.medium = new Picture((Picture) another.getMedium()); // for now only pictures are covered. TODO if necessary
        }

        this.audience = new Audience(another.getAudience());

        this.exampleName = another.getExampleName();
        
        this.negotiationMethod = another.getNegotiationMethod();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        PostRequest otherP = (PostRequest) other;

        if (getOwner().equals(otherP.getOwner()) == false) return false;
        if (getMedium().equals(otherP.getMedium()) == false) return false;
        if (getAudience().equals(otherP.getAudience()) == false) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getOwner().getUid().hashCode();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[PostRequest:" + "\r\n");
        sb.append("\t" + "owner:" + owner + "\r\n");
        if ( medium != null ) {
            sb.append("\t" + "medium:" + "\r\n");
            sb.append("\t\t" + medium + "\r\n");
        }
        sb.append("\t" + "audience:" + "\r\n");
        sb.append("\t\t" + audience + "\r\n");
        sb.append("]");

        return sb.toString();
    }

	public Agent getOwner() {
		return owner;
	}

	public void setOwner(Agent owner) {
		this.owner = owner;
	}

	public Medium getMedium() {
		return medium;
	}

	public void setMedium(Medium medium) {
		this.medium = medium;
	}

	public Audience getAudience() {
		return audience;
	}

	public void setAudience(Audience audience) {
		this.audience = audience;
	}

	public String getExampleName() {
		return exampleName;
	}

	public void setExampleName(String exampleName) {
		this.exampleName = exampleName;
	}

	public NegotiationType getNegotiationMethod() {
		return negotiationMethod;
	}

	public void setNegotiationMethod(NegotiationType negotiationMethod) {
		this.negotiationMethod = negotiationMethod;
	}


}
