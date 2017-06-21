package com.prinego.domain.entity.response.reason;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Created by mester on 12/10/14.
 */
public @Data class RejectionReason implements Serializable {

    private RejectedField field;

    // valid for AUDIENCE, MEDIUM
    private Set<String> includedPeople;

    private List<String> rejectedPeoplebyImportance;
    private int originalViolators = 0;
    

    // valid for POST_TEXT
    private Set<String> mentionedPeople;

    // valid for POST_TEXT
    private Set<String> mentionedLocations;

    // valid for MEDIUM
    private Set<String> includedLocations;

    // valid for MEDIUM:
    private boolean dateTakenDislike;
    private String contextDislike;
    private boolean selfDislike;

    public RejectionReason() {
        setIncludedPeople(new HashSet());
        setMentionedPeople(new HashSet());
        setMentionedLocations(new HashSet());
        setIncludedLocations(new HashSet());
        setRejectedPeoplebyImportance(new ArrayList());
        dateTakenDislike = false;
        selfDislike = false;
        contextDislike = null;
    }

    /**
     *
     * field: Audience
     *      includedPeople: [Ali, Veli]
     *
     * field: PostText
     *      mentionedPeople: [Ali, Veli]
     *                      or
     *      includedLocations [aLocation, anotherLocation]
     *
     * field: Medium
     *      includedPeople [Ali, Veli]
     *                      or
     *      includedLocations [aLocation, anotherLocation]
     *                      or
     *      selfDislike
     *                      or
     *      contextDislike
     *                      or
     *      dateTakenDislike 22.09.14
     *
     */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[RejectionReason:" + "\r\n");
        sb.append("\t" + "field:" + getField() + "\r\n");
        if (CollectionUtils.isNotEmpty(getIncludedPeople())) {
            sb.append("\t" + "includedPeople:" + "\r\n");
            sb.append("\t\t" + getIncludedPeople() + "\r\n");
        }
        if (CollectionUtils.isNotEmpty(getMentionedPeople())) {
            sb.append("\t" + "mentionedPeople:" + "\r\n");
            sb.append("\t\t" + getMentionedPeople() + "\r\n");
        }
        if (CollectionUtils.isNotEmpty(getMentionedLocations())) {
            sb.append("\t" + "mentionedLocations:" + "\r\n");
            sb.append("\t\t" + getMentionedLocations() + "\r\n");
        }
        if (CollectionUtils.isNotEmpty(getIncludedLocations())) {
            sb.append("\t" + "includedLocations:" + "\r\n");
            sb.append("\t\t" + getIncludedLocations() + "\r\n");
        }
        if (isDateTakenDislike()) {
            sb.append("\t" + "dateTakenDislike:" + "\r\n");
            sb.append("\t\t" + true + "\r\n");
        }
        if (getContextDislike() != null) {
            sb.append("\t" + "contextDislike:" + "\r\n");
            sb.append("\t\t" + getContextDislike() + "\r\n");
        }
        if (isSelfDislike()) {
            sb.append("\t" + "selfDislike:" + "\r\n");
            sb.append("\t\t" + true + "\r\n");
        }
        sb.append("]");

        return sb.toString();
    }

	public RejectedField getField() {
		return field;
	}

	public void setField(RejectedField field) {
		this.field = field;
	}

	public Set<String> getIncludedPeople() {
		return includedPeople;
	}

	public void setIncludedPeople(Set<String> includedPeople) {
		this.includedPeople = includedPeople;
	}
	
	public List<String> getRejectedPeoplebyImportance() {
		return rejectedPeoplebyImportance;
	}

	public void setRejectedPeoplebyImportance(List<String> rejectedPeoplebyImportance) {
		this.rejectedPeoplebyImportance = rejectedPeoplebyImportance;
	}

	public int getOriginalViolators() {
		return originalViolators;
	}

	public void setOriginalViolators(int originalViolators) {
		this.originalViolators = originalViolators;
	}
	
	public Set<String> getMentionedPeople() {
		return mentionedPeople;
	}

	public void setMentionedPeople(Set<String> mentionedPeople) {
		this.mentionedPeople = mentionedPeople;
	}

	public Set<String> getMentionedLocations() {
		return mentionedLocations;
	}

	public void setMentionedLocations(Set<String> mentionedLocations) {
		this.mentionedLocations = mentionedLocations;
	}

	public Set<String> getIncludedLocations() {
		return includedLocations;
	}

	public void setIncludedLocations(Set<String> includedLocations) {
		this.includedLocations = includedLocations;
	}

	public boolean isDateTakenDislike() {
		return dateTakenDislike;
	}

	public void setDateTakenDislike(boolean dateTakenDislike) {
		this.dateTakenDislike = dateTakenDislike;
	}

	public String getContextDislike() {
		return contextDislike;
	}

	public void setContextDislike(String contextDislike) {
		this.contextDislike = contextDislike;
	}

	public boolean isSelfDislike() {
		return selfDislike;
	}

	public void setSelfDislike(boolean selfDislike) {
		this.selfDislike = selfDislike;
	}

}
