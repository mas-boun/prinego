package com.prinego.util.globals;

/**
 * Created by mester on 16/10/14.
 */
public class AppGlobals {

	//public static String PRINEGO_REST_BASE_URL = "http://mas.cmpe.boun.edu.tr/prinegoservice/rest/";
    public static String PRINEGO_REST_BASE_URL = "http://localhost:8080/core/rest/";
    public static String OWL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static String OBJ_PROP_NAME_REJECTS = "rejects";
    public static String OBJ_PROP_NAME_REJECTED_IN = "rejectedIn";
    public static String OBJ_PROP_NAME_REJECTED_BECAUSE_OF = "rejectedBecauseOf";
    public static String DATA_PROP_NAME_REJECTED_BECAUSE_OF_DATE = "rejectedBecauseOfDate";

    public static String DATABASE_NAME =	"prinego";
    public static String MONGODB_HOST =	"localhost";
    public static int MONGODB_PORT = 27017;
    public static String POSTREQUEST_COLLECTION = "postRequests";
    public static String POSTREQUESTPAIRWISE_COLLECTION = "postRequestsPairWise";
    public static String POINT_COLLECTION = "points";
    public static String DEMO_DATABASE = "prinego-demo";
    public static String DEMO_COLLECTION = "demo";
    
}
