package com.prinego.domain.entity.negotiation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * created by dilara on 30/10/15
 * */
public enum NegotiationType {
		DEFAULT,
		GEP,  //GEP
		MP,  //MP
		SUCH_BASED,
		@JsonProperty("POINT_BASED")
		RPG,   //RGEP
		RPM,   //RMP
		HybridG,  //hybridG
		HybridM;   //hybridM
}
