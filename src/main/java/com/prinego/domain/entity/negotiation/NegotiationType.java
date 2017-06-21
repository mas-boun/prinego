package com.prinego.domain.entity.negotiation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * created by dilara on 30/10/15
 * */
public enum NegotiationType {
		DEFAULT,
		GEP,  //gep
		MP,  //mp
		SUCH_BASED,
		@JsonProperty("POINT_BASED")
		RPG,   //rpg
		RPM,   //rpm
		HybridG,  //hybridG
		HybridM;   //hybridM
}