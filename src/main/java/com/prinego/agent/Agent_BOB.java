package com.prinego.agent;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.bson.Document;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.prinego.agent.ontologicalBase.OntologicalAgentBase;
import com.prinego.database.handler.MyDatabaseService;

/**
 * Created by mester on 14/10/14.
 */
@Component
public class Agent_BOB extends OntologicalAgentBase {

	@Inject
	private MyDatabaseService myDatabaseService;
	
	Map<String,Integer> points = new HashMap<String,Integer>();
	
    @Override
    public String getUid() {
        return "BOB";
    }

    @Override
    public String getOwlFilePath() {

        URL url = getClass().getClassLoader().getResource("PriNego_BOB.owl");
        return url.getPath();
    }
    
    @Override
    public String getOwlFilePath(String folder) {

    	if(folder.contains("Example")){
        	URL url = getClass().getClassLoader().getResource(folder+"/"+"PriNego_BOB.owl");
        	return url.getPath();
        }
    	
    	URL url = getClass().getClassLoader().getResource("PriNego_BOB.owl");
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
