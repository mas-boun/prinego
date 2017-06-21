package com.prinego.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Preconditions;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.prinego.domain.entity.negotiation.NegotiationType;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.audience.Audience;
import com.prinego.domain.entity.ontology.context.Beach;
import com.prinego.domain.entity.ontology.context.Context;
import com.prinego.domain.entity.ontology.context.EatAndDrink;
import com.prinego.domain.entity.ontology.context.FriendMeeting;
import com.prinego.domain.entity.ontology.context.Leisure;
import com.prinego.domain.entity.ontology.context.Party;
import com.prinego.domain.entity.ontology.context.ProtestMeeting;
import com.prinego.domain.entity.ontology.context.Work;
import com.prinego.domain.entity.ontology.location.Bar;
import com.prinego.domain.entity.ontology.location.Museum;
import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.domain.entity.ontology.medium.Picture;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.util.globals.AppGlobals;
/**
 * Created by mester on 05/09/14.
 */
public class ExampleCreator {

	
	public static List<PostRequest> createSimulation(){
		List<PostRequest> toReturn = new ArrayList<PostRequest>();
		List<PostRequest> allPRs = new ArrayList<PostRequest>();
		MongoClient mongoClient = new MongoClient(AppGlobals.MONGODB_HOST , AppGlobals.MONGODB_PORT );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DATABASE_NAME);		 
		MongoCollection<Document> collection = database.getCollection(AppGlobals.POSTREQUESTPAIRWISE_COLLECTION);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		ObjectReader or = mapper.reader(PostRequest.class);
		MongoCursor<Document> cursor = collection.find().iterator();
		try {
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				Iterator<String> iter = doc.keySet().iterator();
				while(iter.hasNext()){
					String key =  iter.next();
					if(key.equals("_id") || key.equals("count"))
						continue;

					try{
						System.out.println(doc.get(key).toString());
						PostRequest pr = or.readValue(doc.get(key).toString());
						pr.setExampleName("simulation");
						allPRs.add(pr);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		} finally {
			cursor.close();
		}

		for(int i = 0; i<allPRs.size();i+=2){		
			PostRequest p = allPRs.get(i);			
			p.setNegotiationMethod(NegotiationType.RPG);
			PostRequest p1 = allPRs.get((i+1));
			p1.setNegotiationMethod(NegotiationType.RPG);
			toReturn.add(p);
			toReturn.add(p1);
			toReturn.add(p);
			toReturn.add(p1);
			toReturn.add(p);
			toReturn.add(p1);
			toReturn.add(p);
			toReturn.add(p1);
			toReturn.add(p);
			toReturn.add(p1);
		}
		mongoClient.close();
	
//		PostRequest p = allPRs.get(2);			
//		p.setNegotiationMethod(NegotiationType.RPG);
//		System.out.println(p.getMedium().getIsTakenIn());
//		toReturn.add(p);
		return toReturn;

	}
	

	public static List<PostRequest> executePointBased(){
		List<PostRequest> postRequests = new ArrayList<PostRequest>();

		PostRequest p9 = new PostRequest(createP_point(3));  //bob to alice
		p9.setNegotiationMethod(NegotiationType.HybridG);
		p9.setExampleName("Example1");
		
		postRequests.add(p9);
		
		PostRequest p8 = new PostRequest(createP_point(2)); //alice to bob
		p8.setNegotiationMethod(NegotiationType.MP);
		p8.setExampleName("Example1");
	
		PostRequest p10 = new PostRequest(createP_point(4)); //alice to bob
		p10.setNegotiationMethod(NegotiationType.RPG);
		p10.setExampleName("Example1");
	
		PostRequest p5 = new PostRequest(createP_point(5)); //bob to alice


		PostRequest p0 = new PostRequest(createP_point(3)); //bob to alice
		p0.setNegotiationMethod(NegotiationType.RPG);
		p0.setExampleName("Example1");
			
		return postRequests;
	}

	public static PostRequest createP_point(int mode) {
		PostRequest p = new PostRequest(); 
		if(mode ==1){
			Agent owner = new Agent("ALICE"); 
			p.setOwner(owner); 
			Picture picture = new Picture(); 
			picture.setUrl("Picture"); 
			picture.getIncludedPeople().add(new Agent("BOB")); 
			picture.getIsInContexts().add(new EatAndDrink()); 
			p.setMedium(picture); 
			p.setAudience(new Audience()); 
			p.getAudience().getAudienceMembers().add(new Agent("HARRY")); 
			p.getAudience().getAudienceMembers().add(new Agent("JILL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ERROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("DAVID")); 
			p.getAudience().getAudienceMembers().add(new Agent("IRENE")); 
			p.getAudience().getAudienceMembers().add(new Agent("FILIPO")); 
			p.getAudience().getAudienceMembers().add(new Agent("BOB")); 
			p.getAudience().getAudienceMembers().add(new Agent("GEORGE")); 
			p.getAudience().getAudienceMembers().add(new Agent("CAROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ALICE")); 
			p.setNegotiationMethod(NegotiationType.RPG); 
			p.setExampleName("Example1");
			return p;
		}else if(mode==2){
			Agent owner = new Agent("ALICE"); 
			p.setOwner(owner); 
			Picture picture = new Picture(); 
			picture.setUrl("Picture"); 
			picture.getIncludedPeople().add(new Agent("BOB")); 
			picture.getIsInContexts().add(new Party()); 
			p.setMedium(picture); 
			p.setAudience(new Audience()); 
			p.getAudience().getAudienceMembers().add(new Agent("HARRY")); 
			p.getAudience().getAudienceMembers().add(new Agent("JILL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ERROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("DAVID")); 
			p.getAudience().getAudienceMembers().add(new Agent("IRENE")); 
			p.getAudience().getAudienceMembers().add(new Agent("FILIPO")); 
			p.getAudience().getAudienceMembers().add(new Agent("BOB")); 
			p.getAudience().getAudienceMembers().add(new Agent("GEORGE")); 
			p.getAudience().getAudienceMembers().add(new Agent("CAROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ALICE")); 
			p.setNegotiationMethod(NegotiationType.RPG); 
			p.setExampleName("Example1");
			return p;
		}else if(mode==3){
			Agent owner = new Agent("BOB"); 
			p.setOwner(owner); 
			Picture picture = new Picture(); 
			picture.setUrl("Picture"); 
			picture.getIncludedPeople().add(new Agent("ALICE")); 
			picture.getIsInContexts().add(new Party()); 
			p.setMedium(picture); 
			p.setAudience(new Audience()); 
			p.getAudience().getAudienceMembers().add(new Agent("HARRY")); 
			p.getAudience().getAudienceMembers().add(new Agent("JILL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ERROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("DAVID")); 
			p.getAudience().getAudienceMembers().add(new Agent("IRENE")); 
			p.getAudience().getAudienceMembers().add(new Agent("FILIPO")); 
			p.getAudience().getAudienceMembers().add(new Agent("BOB")); 
			p.getAudience().getAudienceMembers().add(new Agent("GEORGE")); 
			p.getAudience().getAudienceMembers().add(new Agent("CAROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ALICE")); 
			p.setNegotiationMethod(NegotiationType.RPG); 
			p.setExampleName("Example1");
			return p;
		}else if(mode == 4){
			Agent owner = new Agent("ALICE"); 
			p.setOwner(owner); 
			Picture picture = new Picture(); 
			picture.setUrl("Picture"); 
			picture.getIncludedPeople().add(new Agent("BOB")); 
			picture.getIsInContexts().add(new Work()); 
			p.setMedium(picture); 
			p.setAudience(new Audience()); 
			p.getAudience().getAudienceMembers().add(new Agent("HARRY")); 
			p.getAudience().getAudienceMembers().add(new Agent("JILL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ERROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("DAVID")); 
			p.getAudience().getAudienceMembers().add(new Agent("IRENE")); 
			p.getAudience().getAudienceMembers().add(new Agent("FILIPO")); 
			p.getAudience().getAudienceMembers().add(new Agent("BOB")); 
			p.getAudience().getAudienceMembers().add(new Agent("GEORGE")); 
			p.getAudience().getAudienceMembers().add(new Agent("CAROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ALICE")); 
			p.setNegotiationMethod(NegotiationType.RPG); 
			p.setExampleName("Example1");
			return p;
		}else if(mode == 5){
			Agent owner = new Agent("BOB"); 
			p.setOwner(owner); 
			Picture picture = new Picture(); 
			picture.setUrl("Picture"); 
			picture.getIncludedPeople().add(new Agent("ALICE")); 
			picture.getIsInContexts().add(new EatAndDrink()); 
			p.setMedium(picture); 
			p.setAudience(new Audience()); 
			p.getAudience().getAudienceMembers().add(new Agent("HARRY")); 
			p.getAudience().getAudienceMembers().add(new Agent("JILL")); 
			p.getAudience().getAudienceMembers().add(new Agent("ERROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("DAVID")); 
			p.getAudience().getAudienceMembers().add(new Agent("IRENE")); 
			p.getAudience().getAudienceMembers().add(new Agent("FILIPO")); 
			p.getAudience().getAudienceMembers().add(new Agent("ALICE")); 
			p.getAudience().getAudienceMembers().add(new Agent("GEORGE")); 
			p.getAudience().getAudienceMembers().add(new Agent("CAROL")); 
			p.getAudience().getAudienceMembers().add(new Agent("BOB")); 
			p.setNegotiationMethod(NegotiationType.RPG); 
			p.setExampleName("Example1");
			return p;
		}
		return null;
	}
}