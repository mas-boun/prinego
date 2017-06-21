package com.prinego.agent;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import com.prinego.agent.ontologicalBase.OntologicalAgentBase;
import com.prinego.database.handler.MyDatabaseService;

@Component
public class Agent_SIMULATION extends OntologicalAgentBase {
	@Inject
	private MyDatabaseService myDatabaseService;
	

	Map<String,Integer> points = new HashMap<String,Integer>();
	
	String Uid;
    @Override
    public String getUid() {
        return Uid;
    }
    
    public void setUid(String Uid){
    	this.Uid = Uid;
    }
    @Override
    public String getOwlFilePath() {

    	URL url = getClass().getClassLoader().getResource("small_network.owl");
        return url.getPath();
    }
    @Override
    public String getOwlFilePath(String folder) {

        URL url = getClass().getClassLoader().getResource("small_network.owl");
        return url.getPath();
    }
    
    
    public double getUtilityThreshold(){
    	return 0.7;
    }
    
    public double getPointWish(){
    	return 0.5;
    }
    
    @Override
    public int getPoints(String owner,String opponent){
    	return myDatabaseService.getPoints(owner,opponent);    	
    }
    
    @Override
    public void setPoints(String owner,String opponent,String mode,int lastPointOffer){
    	
    	myDatabaseService.setPoints(owner, opponent, mode, lastPointOffer);
    }

}
