package com.prinego.agent.skeleton.contracts;

/**
 * Created by mester on 05/09/14.
 */
public interface IdentityAware {

    String getUid();
    double getUtilityThreshold();
    double getPointWish();
	int getPoints(String owner,String opponent);
	void setPoints(String owner, String opponent, String mode, int lastPointOffer);
	
}
