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
public class Agent_ALICE extends OntologicalAgentBase {

	@Inject
	private MyDatabaseService myDatabaseService;
	
	Map<String,Integer> points = new HashMap<String,Integer>();

	@Override
	public String getUid() {
		return "ALICE";
	}

	@Override
	public String getOwlFilePath() {

		//return "/Users/mester/Desktop/PriNego_SVN/aamas-14-negotiation/examples/example1-2-3/PRINEGO_ALICE.owl";

		URL url = getClass().getClassLoader().getResource("PriNego_ALICE.owl");
		return url.getPath();
	}
	@Override
	public String getOwlFilePath(String folder) {

		if(folder.contains("Example")){
			URL url = getClass().getClassLoader().getResource(folder+"/"+"PriNego_ALICE.owl");
			System.out.println("The folder name "+url);
			return url.getPath();
		}

		URL url = getClass().getClassLoader().getResource("PriNego_ALICE.owl");
		return url.getPath();
	}
	public double getUtilityThreshold(){
		return 0.8;
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
