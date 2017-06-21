package com.prinego.domain.entity.ontology.event;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

import com.prinego.domain.annotation.MyOntoClass;
import com.prinego.domain.annotation.MyOntoField;
import com.prinego.domain.entity.ontology.agent.Agent;

@MyOntoClass
public @Data class Event implements Serializable {

	 @MyOntoField(propertyName = "didNotInvite")
	    private Set<Agent> nonInvitedMembers;
	 
	 @MyOntoField(propertyName = "isOrganizedBy")
	    private Agent organizer;

	    public Event() {
	    	nonInvitedMembers = new HashSet();
	    }

	    // copy constructor
	    public Event(Event another) {
	        this.nonInvitedMembers = new HashSet();
	        this.organizer = new Agent(another.getOrganizer());
	        for ( Agent a : another.getNonInvitedMembers() ) {
	            this.nonInvitedMembers.add(new Agent(a));
	        }
	    }

	    public Agent getOrganizer() {
			// TODO Auto-generated method stub
	    	return this.organizer;
		}
	    
	    public void setOrganizer(Agent agent) {
			this.organizer = new Agent(agent);
		}

		@Override
	    public boolean equals(Object other) {
	        if (this == other) return true;
	        if (other == null || getClass() != other.getClass()) return false;

	        Event otherEvent = (Event) other;

	        if(getOrganizer().equals(otherEvent.getOrganizer()) == false ) return false;
	        
	        if (getNonInvitedMembers().equals(otherEvent.getNonInvitedMembers()) == false) return false;

	        return true;
	    }

	    @Override
	    public String toString() {
	        StringBuilder sb = new StringBuilder();
	        sb.append("[Event:" + "\r\n");
	        sb.append("\t\t\t" + "organized by:" + organizer + "\r\n");
	        sb.append("\t\t\t" + "nonInvitedMembers:" + nonInvitedMembers + "\r\n");
	        sb.append("]");

	        return sb.toString();
	    }

		public Set<Agent> getNonInvitedMembers() {
			return nonInvitedMembers;
		}

		public void setNonInvitedMembers(Set<Agent> nonInvitedMembers) {
			this.nonInvitedMembers = nonInvitedMembers;
		}


}
