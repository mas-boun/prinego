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

	//The negotiation type you want to use with the simulation.
	private static NegotiationType NEGOTIATION_TYPE = NegotiationType.RPG;
	
	public static List<PostRequest> createSimulation(){
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

		mongoClient.close();
	
		return createPostRequestListForNegotiationType(allPRs,NEGOTIATION_TYPE);

	}
	
	public static List<PostRequest> createPostRequestListForNegotiationType(List<PostRequest> allPostRequests, NegotiationType negotiationType){
		
		List<PostRequest> toReturn = new ArrayList<PostRequest>();
		switch(negotiationType){
			case GEP:
				for(int i = 0; i<allPostRequests.size();i++){		
					PostRequest p = allPostRequests.get(i);			
					p.setNegotiationMethod(NegotiationType.GEP);
					toReturn.add(p);
				}
				break;
			case MP:
				for(int i = 0; i<allPostRequests.size();i++){		
					PostRequest p = allPostRequests.get(i);			
					p.setNegotiationMethod(NegotiationType.MP);
					toReturn.add(p);
				}
				break;
			case RPG:  //RGEP
				for(int i = 0; i<allPostRequests.size();i+=2){		
					PostRequest p = allPostRequests.get(i);			
					p.setNegotiationMethod(NegotiationType.RPG);
					PostRequest p1 = allPostRequests.get((i+1));
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
				break;
			case RPM:  //RMP
				for(int i = 0; i<allPostRequests.size();i+=2){		
					PostRequest p = allPostRequests.get(i);			
					p.setNegotiationMethod(NegotiationType.RPM);
					PostRequest p1 = allPostRequests.get((i+1));
					p1.setNegotiationMethod(NegotiationType.RPM);
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
				break;
			case HybridG:
				for(int i = 0; i<allPostRequests.size();i+=2){		
					PostRequest p = allPostRequests.get(i);			
					p.setNegotiationMethod(NegotiationType.HybridG);
					PostRequest p1 = allPostRequests.get((i+1));
					p1.setNegotiationMethod(NegotiationType.HybridG);
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
				break;
			case HybridM:
				for(int i = 0; i<allPostRequests.size();i+=2){		
					PostRequest p = allPostRequests.get(i);			
					p.setNegotiationMethod(NegotiationType.HybridM);
					PostRequest p1 = allPostRequests.get((i+1));
					p1.setNegotiationMethod(NegotiationType.HybridM);
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
				break;
			case DEFAULT:
				break;
		}
		
		return toReturn;
	}

	public static List<PostRequest> executePointBased(){
		List<PostRequest> postRequests = new ArrayList<PostRequest>();

		PostRequest p1 = new PostRequest(createPostRequestAliceAndBob(1));//alice - initiator   :  bob - negotiator
		p1.setNegotiationMethod(NEGOTIATION_TYPE);
		p1.setExampleName("Example1");
		
		postRequests.add(p1);
		
		PostRequest p2 = new PostRequest(createPostRequestAliceAndBob(2)); //alice - initiator   :  bob - negotiator
		p2.setNegotiationMethod(NEGOTIATION_TYPE);
		p2.setExampleName("Example1");
	
		PostRequest p3 = new PostRequest(createPostRequestAliceAndBob(3)); //bob - initiator   :  alice - negotiator
		p3.setNegotiationMethod(NEGOTIATION_TYPE);
		p3.setExampleName("Example1");
	
		PostRequest p4 = new PostRequest(createPostRequestAliceAndBob(4)); //alice - initiator   :  bob - negotiator
		p4.setNegotiationMethod(NEGOTIATION_TYPE);
		p4.setExampleName("Example1");
	

		PostRequest p5 = new PostRequest(createPostRequestAliceAndBob(3)); //bob - initiator   :  alice - negotiator
		p5.setNegotiationMethod(NEGOTIATION_TYPE);
		p5.setExampleName("Example1");
			
		return postRequests;
	}

	public static PostRequest createPostRequestAliceAndBob(int mode) {  //post request creator for Alice and Bob.
		PostRequest p = new PostRequest(); 
		if(mode ==1){     // Alice tags Bob in a picture with Eat & Drink context.
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
			p.setExampleName("Example1");
			return p;
		}else if(mode==2){ //Alice tags Bob in a picture with party context.
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
			p.setExampleName("Example1");
			return p;
		}else if(mode==3){  //Bob tags Alice in a picture with party context.
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
			p.setExampleName("Example1");
			return p;
		}else if(mode == 4){  //Alice tags Bob in a picture with work context.
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
			p.setExampleName("Example1");
			return p;
		}else if(mode == 5){ //Bob tags Alice in a picture with Eat & Drink context.
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
			p.setExampleName("Example1");
			return p;
		}
		return null;
	}
}
