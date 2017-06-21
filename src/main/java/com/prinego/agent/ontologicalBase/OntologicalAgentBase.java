package com.prinego.agent.ontologicalBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import com.google.common.base.Preconditions;
import com.prinego.agent.ontologicalBase.contracts.OntologyAware;
import com.prinego.agent.skeleton.AgentSkeleton;
import com.prinego.database.handler.MyDatabaseService;
import com.prinego.domain.entity.ontology.postrequest.PostRequest;
import com.prinego.domain.entity.response.Response;
import com.prinego.ontology.handler.MyOntologyService;
import com.prinego.ontology.object2ontology.api.Object2OntologyService;
import com.prinego.ontology.reasoner.MyOntologyReasoner;
/**
 * Created by mester on 13/10/14.
 * 
 * updated by dilara
 * This is the base of every ontological agent.
 * This class handles the evaluation done by the negotiator agent.
 */
public abstract class OntologicalAgentBase
extends AgentSkeleton implements OntologyAware
{
    @Inject
    private MyOntologyService myOntologyService;

    @Inject
    private Object2OntologyService object2OntologyService;


    @Inject
    private MyOntologyReasoner myOntologyReasoner;
    
    @Inject
	private MyDatabaseService myDatabaseService;
    

    /**
     *
     * @param p: the post request to be evaluated
     *
     * @return : the response after the evaluation
     */
    @Override
    public Response evaluate(
            PostRequest p
    ) {

        Preconditions.checkNotNull(p);
        Preconditions.checkNotNull(p.getAudience());
        Preconditions.checkNotNull(p.getOwner());
        Preconditions.checkNotNull(p.getOwner().getUid());
        Preconditions.checkNotNull(p.getNegotiationMethod());
        
        OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = ontManager.getOWLDataFactory();

        RefreshFile(getOwlFilePath(p.getExampleName()));
        OWLOntology myOntology = myOntologyService.readMyOntology(ontManager, getOwlFilePath(p.getExampleName()));
        IRI ontologyIRI = myOntology.getOntologyID().getOntologyIRI();

        PrefixOWLOntologyFormat pm = (PrefixOWLOntologyFormat) ontManager.getOntologyFormat(myOntology);
        pm.setDefaultPrefix(ontologyIRI + "#");

        // Insert the post request to the ontology
        String key = null;
        try {
            key = object2OntologyService.upsertMyObject(
                            ontManager,
                            dataFactory,
                            myOntology,
                            pm,
                            p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(key);
       
        myDatabaseService.writeDemoLog(getUid()+" got the request and evaluates the offer.", "negotiator",false);
        //get the response of the negotiator agent.
        Response response = myOntologyReasoner.extractResponse(myOntology, dataFactory, pm, key, getUid(),
        		p.getNegotiationMethod(), getUtilityThreshold());
		

       	
        return response;
    }
    
    /**
    * This method gets a post request and the previous responses
    * of the negotiator agent, return the current response of the 
    * negotiator agent.
    * @param p the post request to be evaluated
    * @param list of previous responses of the negotiator agent
    *
    * @return response the response after the evaluation of the post request
    */
   @Override
   public Response evaluate(
           PostRequest p , List<Response> prevResponses
   ) {

     
       Preconditions.checkNotNull(p);
       Preconditions.checkNotNull(p.getAudience());
       Preconditions.checkNotNull(p.getOwner());
       Preconditions.checkNotNull(p.getOwner().getUid());
       Preconditions.checkNotNull(p.getNegotiationMethod());
       Preconditions.checkNotNull(prevResponses);
       
       OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
       OWLDataFactory dataFactory = ontManager.getOWLDataFactory();

       RefreshFile(getOwlFilePath(p.getExampleName()));
       
       OWLOntology myOntology = myOntologyService.readMyOntology(ontManager, getOwlFilePath(p.getExampleName()));
       IRI ontologyIRI = myOntology.getOntologyID().getOntologyIRI();

       PrefixOWLOntologyFormat pm = (PrefixOWLOntologyFormat) ontManager.getOntologyFormat(myOntology);
       pm.setDefaultPrefix(ontologyIRI + "#");

      
       // Insert the post request to the ontology
       String key = null;
       try {
           key = object2OntologyService.upsertMyObject(
                           ontManager,
                           dataFactory,
                           myOntology,
                           pm,
                           p);
       } catch (Exception e) {
           e.printStackTrace();
       }
       Preconditions.checkNotNull(key);
       
       myDatabaseService.writeDemoLog(getUid()+" got the request and evaluates the offer.", "negotiator",false);
     	
       Response response = myOntologyReasoner.extractResponse(myOntology, dataFactory, pm, key, getUid(),
       		p.getNegotiationMethod(), getUtilityThreshold(),prevResponses);

       return response;
   }
   
   

   /**
    * This method gets a post request, the previous responses
    * of the negotiator agent and the current point offer, return the current response of the 
    * negotiator agent.
    * This method is only used for point-based negotiations.
    * @param p the post request to be evaluated
    * @param list of previous responses of the negotiator agent
    * @param pointOffer the point offer of the initiator agent for the p.
    *
    * @return response the response after the evaluation of the post request
    */
  @Override
  public Response evaluate(
          PostRequest p , List<Response> prevResponses,int pointOffer
  ) {

     
      Preconditions.checkNotNull(p);
      Preconditions.checkNotNull(p.getAudience());
      Preconditions.checkNotNull(p.getOwner());
      Preconditions.checkNotNull(p.getOwner().getUid());
      Preconditions.checkNotNull(p.getNegotiationMethod());
      Preconditions.checkNotNull(prevResponses);
      
      OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
      OWLDataFactory dataFactory = ontManager.getOWLDataFactory();
      System.out.println("Inserted  post request "+p);
      RefreshFile(getOwlFilePath(p.getExampleName()));
      OWLOntology myOntology = myOntologyService.readMyOntology(ontManager, getOwlFilePath(p.getExampleName()));
      IRI ontologyIRI = myOntology.getOntologyID().getOntologyIRI();

      PrefixOWLOntologyFormat pm = (PrefixOWLOntologyFormat) ontManager.getOntologyFormat(myOntology);
      pm.setDefaultPrefix(ontologyIRI + "#");
      
      // Insert the post request to the ontology
      String key = null;
      try {
          key = object2OntologyService.upsertMyObject(
                          ontManager,
                          dataFactory,
                          myOntology,
                          pm,
                          p);
      } catch (Exception e) {
          e.printStackTrace();
      }
      Preconditions.checkNotNull(key);
      myDatabaseService.writeDemoLog(getUid()+" got the request and evaluates the offer.", "negotiator",false);
      Response response = myOntologyReasoner.extractResponse(myOntology, dataFactory, pm, key, getUid(),
      		p.getNegotiationMethod(), getUtilityThreshold(),prevResponses,pointOffer,getPointWish(),getPoints(getUid(),p.getOwner().getUid()));
      return response;
  }
  
  /**
   * This method gets an initial post request and a final post request
   * and returns the utility of the negotiator agent.
   * This method is only for negotiator now since initiator agent's
   * utility calculation is straightforward and only depends on the changed audience amount. Can
   * alter later to support initiator agent if the utility calculation of it changes.
   * @param initial the initial post request
   * @param finalized the final post request that is agreed on
   * @param role Role of the agent.
   *
   * @return utility the final utility of the agreed post request.
   */
  @Override
	public double getUtility(PostRequest initial,PostRequest finalized,String role){
		Preconditions.checkNotNull(initial);
      Preconditions.checkNotNull(initial.getAudience());
      Preconditions.checkNotNull(initial.getOwner());
      Preconditions.checkNotNull(initial.getOwner().getUid());
      Preconditions.checkNotNull(initial.getNegotiationMethod());
      
      if(finalized==null)
      	return 1;
      OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
      OWLDataFactory dataFactory = ontManager.getOWLDataFactory();

      RefreshFile(getOwlFilePath(initial.getExampleName()));
      OWLOntology myOntology = myOntologyService.readMyOntology(ontManager, getOwlFilePath(initial.getExampleName()));
      IRI ontologyIRI = myOntology.getOntologyID().getOntologyIRI();

      System.out.println("Inserted initial post request "+initial);
      PrefixOWLOntologyFormat pm = (PrefixOWLOntologyFormat) ontManager.getOntologyFormat(myOntology);
      pm.setDefaultPrefix(ontologyIRI + "#");
      
   // Insert the post request to the ontology
      String key = null;
      String key2 = null;
      try {
          key = object2OntologyService.upsertMyObject(
                          ontManager,
                          dataFactory,
                          myOntology,
                          pm,
                          initial);
          
          key2 = object2OntologyService.upsertMyObject(
                  ontManager,
                  dataFactory,
                  myOntology,
                  pm,
                  finalized);
      } catch (Exception e) {
          e.printStackTrace();
      }
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(key2);
      
      double utility = myOntologyReasoner.extractUtility(myOntology, dataFactory, pm, key,key2, getUid());
      System.out.println("got the utility "+utility);
      
		return utility;
	}
  
  /**
   * This method gets a file path of an owl file
   * and resets it to the initial condition from the *_Copy.owl
   * file. 
   * 
   * Every iteration adds postrequest to the .owl file and as the
   * negotiation continues the .owl file gets larger and larger.
   * This affects the reasoning time so we delete the previous
   * post requests and its related variable from the current .owl
   * file since they are not useful anymore.
   *  
   * @param owlFilePath the file path of the currently used .owl file
   *
   */
  private void RefreshFile(String owlFilePath) {
		
	    File f = new File(owlFilePath);
		String filename = f.getName();
		
		String[] fname = filename.split(Pattern.quote("."));
		
		f.delete();
		File f1 = new File(f.getAbsolutePath());
		String filePath = f.getAbsolutePath().
			    substring(0,f.getAbsolutePath().lastIndexOf(File.separator));
		File f2 = new File(filePath+File.separator+fname[0]+"_Copy."+fname[1]);
		
		InputStream inStream = null;
		OutputStream outStream = null;
			
	    	try{
	    		
	    	   
	    	    inStream = new FileInputStream(f2);
	    	    outStream = new FileOutputStream(f1);
	        	
	    	    byte[] buffer = new byte[1024];
	    		
	    	    int length;
	    	    //copy the file content in bytes 
	    	    while ((length = inStream.read(buffer)) > 0){
	    	  
	    	    	outStream.write(buffer, 0, length);
	    	 
	    	    }
	    	 
	    	    inStream.close();
	    	    outStream.close();
	    	      
	    	    
	    	}catch(IOException e){
	    		e.printStackTrace();
	    	}
	}

}