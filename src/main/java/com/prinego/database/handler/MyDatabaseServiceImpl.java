package com.prinego.database.handler;

import java.util.Iterator;

import org.bson.Document;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.context.Context;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.response.Response;
import com.prinego.util.globals.AppGlobals;

/**
 * Created by dilara on 23/06/16.
 */
@Component
public class MyDatabaseServiceImpl implements MyDatabaseService{

	@Override
	public void writeDemoLog(Object obj,String sender,boolean revised){
		if(obj == null)
			return;
		MongoClient mongoClient = new MongoClient(AppGlobals.MONGODB_HOST , AppGlobals.MONGODB_PORT );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DEMO_DATABASE);		 
		MongoCollection<Document> collection = database.getCollection(AppGlobals.DEMO_COLLECTION);
		Document doc = new Document();
		if(obj.getClass().equals(String.class)){
			doc.append("message", (String)obj);
		}else if(obj.getClass().equals(PostRequest.class)){
			doc = getPostRequestDoc((PostRequest)obj,revised);
		}else if(obj.getClass().equals(Response.class)){
			doc = getResponseDoc((Response)obj);
		}else if(obj.getClass().equals(HistoryRequest.class)){
			doc = getHistoryRequestDoc((HistoryRequest)obj);
		}
		
		
		doc.append("sender", sender);
		
		collection.insertOne(doc);
		mongoClient.close();
	}
	
	@Override
	public Document getHistoryRequestDoc(HistoryRequest hr) {
		Document toReturn = new Document();
		
		
		StringBuilder sb = new StringBuilder();
		String owner = hr.getP().getOwner().getUid();
		Iterator<Agent> iter = hr.getP().getMedium().getIncludedPeople().iterator();
		String negotiator = iter.hasNext()?iter.next().getUid():"";
		int pointOffer = hr.getPointOffer();
		
		sb.append(owner+" revised the post according to "+negotiator+"'s wishes.<br>"+
				"Points offered = "+pointOffer+".<br>");
		Iterator<Agent> iter3 = hr.getP().getAudience().getAudienceMembers().iterator();
		
		sb.append("People who can see this post are as follows; <br>");
		iter3 = hr.getP().getAudience().getAudienceMembers().iterator();
		while(iter3.hasNext()){
			Agent a = iter3.next();
			sb.append(a.getUid());
			break;
		}
		
		while(iter3.hasNext()){
			Agent a = iter3.next();
			sb.append(", "+a.getUid());
			
		}
		sb.append(".<br>");
		
		toReturn.append("message", sb.toString());
		
		return toReturn;
	}

	@Override
	public Document getPostRequestDoc(PostRequest pr,boolean revised) {
	
		Document toReturn = new Document();
		
		
		StringBuilder sb = new StringBuilder();
		String owner = pr.getOwner().getUid();
		Iterator<Agent> iter = pr.getMedium().getIncludedPeople().iterator();
		String negotiator = iter.hasNext()?iter.next().getUid():"";
		String context="";
		if(!pr.getMedium().getIsInContexts().isEmpty()){
			Iterator<Context> iter2 = pr.getMedium().getIsInContexts().iterator();
			context = iter2.hasNext()?iter2.next().toString():"";
			
		}
		
		if(!revised){
			if(context.equals("")){
				sb.append(owner+" wants to upload a post about "+negotiator+"." +
						" People who can see this post are as follows; <br>");
			}else{
				sb.append(owner+" wants to upload a post about "+negotiator+". The post is " +
						"in "+context+" context. People who can see this post are as follows; <br>");
			}
			
		}else{
			sb.append(owner+" revised the post according to "+negotiator+"'s wishes.<br>"+
					"People who can see this post are as follows; <br>");
		}
		
		
		Iterator<Agent> iter3 = pr.getAudience().getAudienceMembers().iterator();
		
		while(iter3.hasNext()){
			sb.append(iter3.next().getUid());
			break;
		}
		
		while(iter3.hasNext()){
			sb.append(", "+iter3.next().getUid());
		}
		sb.append(".<br>");
		
		toReturn.append("message", sb.toString());
		
		return toReturn;
	}

	@Override
	public Document getResponseDoc(Response response) {
		
		Document toReturn = new Document();
		
		StringBuilder sb = new StringBuilder();
		String owner = response.getOwner();
		
		if(response.getResponseCode().equals("Y")){
			sb.append(owner+" allows the post to be shared.<br>");
		}else{
			sb.append(owner+" does not want the post to be shared. <br>");

			if(response.getPointOffer() != -1){
				if(response.getReason().getIncludedPeople().size() != 0){
										
					if(response.getReason().getIncludedPeople().size()==1){
						Iterator<String> iter = response.getReason().getIncludedPeople().iterator();						
						sb.append((iter.hasNext()?iter.next():"")+" can see the post where "+owner+" does not want them to see. <br>");
					}else if(response.getReason().getIncludedPeople().size()>1){
						sb.append("There are people who can see the post where "+owner+" does not want them to see. <br>");
						sb.append("These people are as follows; <br>");
						Iterator<String> iter = response.getReason().getIncludedPeople().iterator();						
						while(iter.hasNext()){
							sb.append(iter.next());
							break;
						}						
						while(iter.hasNext()){
							sb.append(", "+iter.next());
						}
						sb.append(".<br>");
					}
					sb.append("It is possible that "+owner+" would allow the post for some points.<br>");
				}else{
					sb.append("However for "+response.getPointOffer()+" points,"+owner+" would allow it. <br>");
				}
				
			}else if(response.getReason().getIncludedPeople().size()==1){
				Iterator<String> iter = response.getReason().getIncludedPeople().iterator();	
				String agent = iter.hasNext()?iter.next():"";
				sb.append(agent+" can see the post where "+owner+" does not want them to see. <br>" +
						owner+" wants "+agent+".to be removed from the audience.<br>");
			}else if(response.getReason().getIncludedPeople().size()>1){
				sb.append("There are people who can see the post where "+owner+" does not want them to see. <br>");
				sb.append("These people are as follows; <br>");
				Iterator<String> iter = response.getReason().getIncludedPeople().iterator();						
				while(iter.hasNext()){
					sb.append(iter.next());
					break;
				}						
				while(iter.hasNext()){
					sb.append(", "+iter.next());
				}
				sb.append(".<br>");
				sb.append(owner+" wants these people to be removed from the audience.<br>");
			}
					
			
		}
		
		
		
		toReturn.append("message", sb.toString());
		
		return toReturn;
	}

	@Override
	public int getPoints(String owner, String opponent) {
		
		MongoClient mongoClient = new MongoClient(AppGlobals.MONGODB_HOST , AppGlobals.MONGODB_PORT );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DATABASE_NAME);	
		String pointsCollection = AppGlobals.POINT_COLLECTION;
		MongoCollection<Document> collection = database.getCollection(pointsCollection);
		Document opponentPoint = collection.find(new Document("owner",owner).append("opponent", opponent)).limit(1).first();
		if(opponentPoint == null){
			collection.insertOne(new Document("owner",owner).append("opponent", opponent).append("point", 5));
    		opponentPoint = collection.find(new Document("owner",owner).append("opponent", opponent)).limit(1).first();
		}
		int point = opponentPoint.getInteger("point",5);
		mongoClient.close();
		System.out.println(point);
    	return point;
	}

	@Override
	public void setPoints(String owner, String opponent, String mode, int lastPointOffer) {
		MongoClient mongoClient = new MongoClient(AppGlobals.MONGODB_HOST , AppGlobals.MONGODB_PORT );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DATABASE_NAME);	
		
		String pointsCollection = AppGlobals.POINT_COLLECTION;
		MongoCollection<Document> collection = database.getCollection(pointsCollection);
		System.out.println(pointsCollection);
		Document opponentPoint = collection.find(new Document("owner",owner).append("opponent", opponent)).limit(1).first();
    	if(opponentPoint == null){
    		collection.insertOne(new Document("owner",owner).append("opponent", opponent).append("point", 5));
    		opponentPoint = collection.find(new Document("owner",owner).append("opponent", opponent)).limit(1).first();
    	}
    	
    	if(mode.equals("INITIATOR")){
    		int prevPoint = opponentPoint.getInteger("point");
    		if(prevPoint<lastPointOffer){
    			System.out.println("Bidded higher than you can give");
    			mongoClient.close();
    			return;
    		}
    		
    		collection.updateOne(opponentPoint, new Document("$set",new Document("point",prevPoint-lastPointOffer)));
    		
    	}else if (mode.equals("INCLUDED")){
    		int prevPoint = opponentPoint.getInteger("point");
    		collection.updateOne(opponentPoint, new Document("$set",new Document("point",prevPoint+lastPointOffer)));
    		
    	}
    	
    	mongoClient.close();
		
	}

	@Override
	public void dropPoints() {
		MongoClient mongoClient = new MongoClient(AppGlobals.MONGODB_HOST , AppGlobals.MONGODB_PORT );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DATABASE_NAME);			 
		MongoCollection<Document> collection = database.getCollection(AppGlobals.POINT_COLLECTION);
		collection.drop();
		mongoClient.close();
		
	}

	@Override
	public void copyDemoLogs() {
		MongoClient mongoClient = new MongoClient(AppGlobals.MONGODB_HOST , AppGlobals.MONGODB_PORT );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DEMO_DATABASE);		 
		MongoCollection<Document> collection = database.getCollection(AppGlobals.DEMO_COLLECTION);
		collection.drop();
		mongoClient.close();		
	}

	@Override
	public void copyPoints() {
		MongoClient mongoClient = new MongoClient(AppGlobals.MONGODB_HOST , AppGlobals.MONGODB_PORT );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DATABASE_NAME);		 
		MongoCollection<Document> collection = database.getCollection(AppGlobals.POINT_COLLECTION);
		MongoCursor<String> cursor = database.listCollectionNames().iterator();
		int max = 0;
		while (cursor.hasNext()) {
			String name = cursor.next();
			if(!name.contains("points_"))
				continue;
			String[] split = name.split("_");
			int index = Integer.parseInt(split[1]);
			if(index>max)
				max = index;
		}
		max++;
		String pointsCollection= "points_"+max;
		MongoCollection<Document> copyCollection = database.getCollection(pointsCollection);
		MongoCursor<Document> iter = collection.find().iterator();

		while(iter.hasNext()){
			Document doc = iter.next();
			copyCollection.insertOne(doc);

		}
		mongoClient.close();	
	}

	
}
