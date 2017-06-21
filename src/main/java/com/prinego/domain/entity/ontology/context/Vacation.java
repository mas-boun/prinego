package com.prinego.domain.entity.ontology.context;

import com.prinego.domain.annotation.MyOntoClass;
import com.prinego.domain.annotation.MyOntoField;

import java.io.Serializable;

/**
 * Created by dilara on 17/10/16.
 */
@MyOntoClass
public class Vacation extends Context implements Serializable {
	@MyOntoField(propertyName = "inCity")
    private String city; // not-null
	
	public Vacation() {}

    public Vacation(String city) {
        this.city = city;
    }
    
    public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
}
