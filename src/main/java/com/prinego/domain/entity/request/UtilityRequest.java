package com.prinego.domain.entity.request;

import com.prinego.domain.entity.ontology.postrequest.PostRequest;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by dilara on 10/06/16.
 */
public @Data class UtilityRequest implements Serializable {

    private PostRequest initialP;
    private PostRequest finalizedP;
    private int postIndex;
    private String role;
    
    public UtilityRequest() { }

    public PostRequest getinitialP() {
		return initialP;
	}

	public void setInitialP(PostRequest p) {
		this.initialP = p;
	}
	
	public PostRequest getFinalizedP() {
		return finalizedP;
	}

	public void setFinalizedP(PostRequest p) {
		this.finalizedP = p;
	}

	public int getPostIndex() {
		return postIndex;
	}

	public void setPostIndex(int index) {
		this.postIndex = index;
	}
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
}
