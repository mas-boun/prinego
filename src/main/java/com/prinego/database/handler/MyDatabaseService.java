package com.prinego.database.handler;

import org.bson.Document;

import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.request.HistoryRequest;
import com.prinego.domain.entity.response.Response;

public interface MyDatabaseService {

	void writeDemoLog(Object obj, String sender, boolean revised);
	
	Document getPostRequestDoc(PostRequest pr, boolean revised);

	Document getResponseDoc(Response response);

	Document getHistoryRequestDoc(HistoryRequest hr);

	int getPoints(String owner, String opponent);

	void setPoints(String owner, String opponent, String mode, int lastPointOffer);

	void dropPoints();

	void copyDemoLogs();

	void copyPoints();

	
	
}
