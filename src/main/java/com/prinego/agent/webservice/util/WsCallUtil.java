package com.prinego.agent.webservice.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Preconditions;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.prinego.database.handler.MyDatabaseService;
import com.prinego.database.handler.MyDatabaseServiceImpl;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.UploadRequest;
import com.prinego.domain.entity.request.UtilityRequest;
import com.prinego.util.globals.AppGlobals;
import com.prinego.util.json.JsonReader;
import com.prinego.util.json.JsonWriter;

/**
 * Created by mester on 19/10/14.
 */
public class WsCallUtil {
	
	
	private static MyDatabaseService myDatabaseService = new MyDatabaseServiceImpl();

	public static PostRequest callUploadWs(PostRequest p, Set<Medium> altMediums) {	

		display(p, altMediums);
		RestTemplate restTemplate = new RestTemplate();

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Content-Type", "application/json;charset=utf-8");
		headers.add("accept", "application/json");

		String uploadUrl = AppGlobals.PRINEGO_REST_BASE_URL + p.getOwner().getUid() + "/upload";
	
		UploadRequest uploadRequest = new UploadRequest();
		uploadRequest.setP(p);
		uploadRequest.setAltMediums(altMediums);

		String uploadRequestJson = null;
		try {
			uploadRequestJson = JsonWriter.objectToJson(uploadRequest);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			//set your entity to send
			HttpEntity<String> requestEntity = new HttpEntity<String>(uploadRequestJson, headers);

			ResponseEntity<String> responseEntity =
					restTemplate.exchange(
							uploadUrl,
							HttpMethod.POST,
							requestEntity,
							String.class,
							new HashMap<>()
							);

			PostRequest finalizedP = (PostRequest)  JsonReader.jsonToJava(responseEntity.getBody());
			displayFinalP(finalizedP);

			return finalizedP;
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}

	}

	public static List<PostRequest> callUploadWs(List<PostRequest> postList, Set<Medium> altMediums) {

				
		List<PostRequest> finalizedPostList = new ArrayList<PostRequest>();

		Iterator<PostRequest> iter = postList.iterator();
		while(iter.hasNext()){
			
			copyDemoLogs();
			
			PostRequest p = iter.next();

			display(p, altMediums);

			RestTemplate restTemplate = new RestTemplate();

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
			headers.add("Content-Type", "application/json;charset=utf-8");
			headers.add("accept", "application/json");

			String uploadUrl = AppGlobals.PRINEGO_REST_BASE_URL + p.getOwner().getUid() + "/upload";
			if(p.getOwner().getUid().contains("user"))
				uploadUrl = AppGlobals.PRINEGO_REST_BASE_URL +"SIMULATION/upload";
			
			UploadRequest uploadRequest = new UploadRequest();
			uploadRequest.setP(p);
			uploadRequest.setAltMediums(altMediums);

			String uploadRequestJson = null;
			try {
				uploadRequestJson = JsonWriter.objectToJson(uploadRequest);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {

				//set your entity to send
				HttpEntity<String> requestEntity = new HttpEntity<String>(uploadRequestJson, headers);

				ResponseEntity<String> responseEntity =
						restTemplate.exchange(
								uploadUrl,
								HttpMethod.POST,
								requestEntity,
								String.class,
								new HashMap<>()
								);
				
				PostRequest finalizedP = (PostRequest) JsonReader.jsonToJava(responseEntity.getBody());
				displayFinalP(finalizedP);
				List<PostRequest> firstPost = new ArrayList<PostRequest>();
				List<PostRequest> finalizedPost = new ArrayList<PostRequest>();
				firstPost.add(p);
				finalizedPost.add(finalizedP); 
				finalizedPostList.add(finalizedP); 
				displayUtilities(firstPost,finalizedPost);
		
			} catch ( Exception ex ) {
				ex.printStackTrace();

			}

		}
		System.out.println("initial post list size "+postList.size());
		System.out.println("finalized post list size "+finalizedPostList.size());

		copyPoints();
		return finalizedPostList;

	}
	
	public static void bestPairwiseWs(List<PostRequest> postList){
		
		for(int i=0; i<postList.size(); i+=2){
			
			PostRequest p1 = postList.get(i);
			PostRequest p2 = postList.get((i+1));
			
			RestTemplate restTemplate = new RestTemplate();

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
			headers.add("Content-Type", "application/json;charset=utf-8");
			headers.add("accept", "application/json");

					String uploadUrl = AppGlobals.PRINEGO_REST_BASE_URL + p1.getOwner().getUid() + "/pairwise";
			if(p1.getOwner().getUid().contains("user"))
				uploadUrl = AppGlobals.PRINEGO_REST_BASE_URL +"SIMULATION/pairwise";
			
			List<PostRequest> uploadRequest = new ArrayList<PostRequest>();
			uploadRequest.add(p1); uploadRequest.add(p2);
			String uploadRequestJson = null;
			try {
				uploadRequestJson = JsonWriter.objectToJson(uploadRequest);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				//set your entity to send
				HttpEntity<String> requestEntity = new HttpEntity<String>(uploadRequestJson, headers);

				ResponseEntity responseEntity =
						restTemplate.exchange(
								uploadUrl,
								HttpMethod.POST,
								requestEntity,
								String.class,
								new HashMap<>()
								);
				
										
			} catch ( Exception ex ) {
				ex.printStackTrace();

			}

		}
	}
	private static void dropPoints() {
		myDatabaseService.dropPoints();
	}

	private static void copyDemoLogs() {
		myDatabaseService.copyDemoLogs() ;
	}

	private static void copyPoints() {
		myDatabaseService.copyPoints();		
	}

	public static void displayUtilities(List<PostRequest> initialPostList,List<PostRequest> finalizedPostList) {
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		// Now connect to your databases
		MongoDatabase database = mongoClient.getDatabase(AppGlobals.DATABASE_NAME);		 
		MongoCollection<Document> collection = database.getCollection("newcomp_RP");
		System.out.println("Post Index\tOwner\tNegotiator\tO.Utility\tN.Utility");
		for(int i = 0 ; i<initialPostList.size();i++){
			PostRequest initialP = initialPostList.get(i);
			PostRequest finalizedP = finalizedPostList.get(i);
			double ownerUtility = getUtility(initialP, finalizedP, initialP.getOwner().getUid(), "owner", i);
			Set<Agent> negotiators = initialP.getMedium().getIncludedPeople();
			Agent negotiator = null;
			for(Agent a:negotiators)
				negotiator = a;
			Preconditions.checkNotNull(negotiator);
			double negotiatorUtility = getUtility(initialP, finalizedP, negotiator.getUid(), "negotiator", i);
			MongoCursor<Document> cursor = collection.find(new Document("owner",initialP.getOwner().getUid()).append("negotiation_type", initialP.getNegotiationMethod().toString())).iterator();
			int count = 1;
			while(cursor.hasNext()){
				cursor.next();
				count++;
			}
			System.out.println("run number "+count);
			
			double scaledProduct = (ownerUtility*negotiatorUtility*(1-Math.abs(ownerUtility-negotiatorUtility)));
			collection.insertOne(new Document("run_number",count).append("negotiation_type", initialP.getNegotiationMethod().toString())
					.append("owner", initialP.getOwner().getUid())
					.append("negotiator", negotiator.getUid()).append("u1", ownerUtility)
					.append("u2", negotiatorUtility).append("u1xu2",ownerUtility*negotiatorUtility)
					.append("scaledProduct", scaledProduct));

			
			System.out.println(""+i+"\t"+initialP.getOwner().getUid()+"\t"+negotiator.getUid()+"\t"+ownerUtility+"\t"+negotiatorUtility);

		}
		
		System.out.println("Average Utilities");
		System.out.println("User\tAverage Utility\t# of Negotiations Joined");

		mongoClient.close();

	}
	
	public static double getUtility(PostRequest initialP,PostRequest finalizedP,String agentUid,String role,int index){
		
		RestTemplate restTemplate = new RestTemplate();

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Content-Type", "application/json;charset=utf-8");
		headers.add("accept", "application/json");

		String uploadUrl = AppGlobals.PRINEGO_REST_BASE_URL + agentUid + "/getUtility";
		if(agentUid.contains("user"))
			uploadUrl = AppGlobals.PRINEGO_REST_BASE_URL +"SIMULATION/getUtility";
		
		UtilityRequest utilityRequest = new UtilityRequest();
		utilityRequest.setInitialP(initialP);
		utilityRequest.setFinalizedP(finalizedP);
		utilityRequest.setRole(role);
		utilityRequest.setPostIndex(index);
		String utilityRequestJson = null;
		
		try {
			utilityRequestJson = JsonWriter.objectToJson(utilityRequest);
			UtilityRequest util = (UtilityRequest) JsonReader.jsonToJava(utilityRequestJson);
			System.out.println(util.getRole());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(utilityRequestJson);
		try {
			//set your entity to send
			HttpEntity<String> requestEntity = new HttpEntity<String>(utilityRequestJson, headers);

			ResponseEntity<Double> responseEntity =
					restTemplate.exchange(
							uploadUrl,
							HttpMethod.POST,
							requestEntity,
							Double.class,
							new HashMap<>()
							);
		
				return responseEntity.getBody();
		} catch ( Exception ex ) {
			ex.printStackTrace();

		}
		
		
		
		return -1;
	}

	private static void display(PostRequest p, Set<Medium> altMediums) {
		System.out.println(p.getOwner().getUid() + " creates a post request and calls upload.");
		System.out.println("The post request is:");
		System.out.println(p);
		if ( CollectionUtils.isNotEmpty(altMediums) ) {
			System.out.println("The alternative mediums are:");
			for ( Medium aMedium : altMediums ) {
				System.out.println(aMedium.toString());
			}
		}

	}

	private static void displayFinalP(PostRequest finalizedP) {
		if(finalizedP==null){
			System.out.println("The agreement could not be reached");
		}else{
			System.out.println("After the negotiation, the finalized post request is:");
			System.out.println(finalizedP);
		}


	}
}
