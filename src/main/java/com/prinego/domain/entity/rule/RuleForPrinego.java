package com.prinego.domain.entity.rule;

import com.prinego.domain.entity.ontology.agent.Agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//import android.util.Log;


/**
 * @author Dilara Kekulluoglu
 *         <p/>
 *         This class is for rules. Rules are used to determine user's choices.
 *         This is our code's counterpart for the SWRL Rule in OWL API.
 */
public class RuleForPrinego implements Serializable {


    private static final long serialVersionUID = 1L;

    //The owner of the rule for creating swrl rules
    private Agent ownerUser;


    //Group name of the Audience if any. e.g. family, colleague
    private String audiGroupName = null;

    //Custom audience. Can only pick one member even though it s a list.
    private List<Agent> audience;

    //Custom included people. Can only pick one member even though it s a list.
    private List<Agent> includedPeople;

    //Locations of the photo. Can only pick one location even though it s a list.
    private List<String> includedLocations;

    //Context of the photo for the rule
    private String context = null;

    //Date restriction for the rule if any
    private String date = null;

   //Weight of the rule *new addition
    private int weight = 5;
    
    //Title to show the rules by.
    private String title;

    private boolean useCalendar = false;


    // Reason List consisting of Triple s and Pairs. Counterpart of the SWRL Rule Body.
    private List<Object> reasonList;

    //Reject List consisting of Triple s. Counterpart of the SWRL Rule Head.
    private List<Triple> rejectList;


    public List<Object> getReasonList() {
        return reasonList;
    }

    public void addReasonListMember(Object item) {
        this.reasonList.add(item);
    }

    public void addRejectListMember(Triple item) {
        this.rejectList.add(item);
    }

    public List<Triple> getRejectList() {
        return rejectList;
    }

    public Agent getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(Agent ownerUser) {
        this.ownerUser = ownerUser;
    }


    public RuleForPrinego(String title) {
        this.title = title;
        reasonList = new ArrayList<Object>();
        rejectList = new ArrayList<Triple>();
        includedLocations = new ArrayList<String>();
        audience = new ArrayList<Agent>();
        includedPeople = new ArrayList<Agent>();

    }

    public RuleForPrinego() {
        reasonList = new ArrayList<Object>();
        rejectList = new ArrayList<Triple>();
        includedLocations = new ArrayList<String>();
        audience = new ArrayList<Agent>();
        includedPeople = new ArrayList<Agent>();

    }

    public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isUseCalendar() {
        return useCalendar;
    }

    public void setUseCalendar(boolean useCalendar) {
        this.useCalendar = useCalendar;
    }

    public String getAudiGroupName() {
        return audiGroupName;
    }

    public List<Agent> getAudience() {
        return audience;
    }

    public List<Agent> getIncludedPeople() {
        return includedPeople;
    }

    public List<String> getIncludedLocations() {
        return includedLocations;
    }

    public String getContext() {
        return context;
    }

    public String getDate() {
        return date;
    }


    public void setAudiGroupName(String audiGroupName) {
        this.audiGroupName = audiGroupName;
    }

    public void setAudience(List<Agent> audience) {
        this.audience = audience;
    }

    public void setIncludedPeople(List<Agent> includedPeople) {
        this.includedPeople = includedPeople;
    }

    public void setIncludedLocations(List<String> includedLocations) {
        this.includedLocations = includedLocations;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
