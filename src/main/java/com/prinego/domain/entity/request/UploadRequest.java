package com.prinego.domain.entity.request;

import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by mester on 20/10/14.
 */
public @Data class UploadRequest implements Serializable {

    private PostRequest p;
    private Set<Medium> altMediums;

    public UploadRequest() { }

	public PostRequest getP() {
		return p;
	}

	public void setP(PostRequest p) {
		this.p = p;
	}

	public Set<Medium> getAltMediums() {
		return altMediums;
	}

	public void setAltMediums(Set<Medium> altMediums) {
		this.altMediums = altMediums;
	}

}
