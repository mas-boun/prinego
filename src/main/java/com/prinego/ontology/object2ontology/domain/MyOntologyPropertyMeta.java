package com.prinego.ontology.object2ontology.domain;

import lombok.Data;

/**
 * Created by mester on 17/08/14.
 */
public @Data class MyOntologyPropertyMeta {

    private String propertyName;
    private MyOntologyPropertyType propertyType;
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public MyOntologyPropertyType getPropertyType() {
		return propertyType;
	}
	public void setPropertyType(MyOntologyPropertyType propertyType) {
		this.propertyType = propertyType;
	}

}
