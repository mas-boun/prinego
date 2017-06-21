package com.prinego.domain.entity.ontology.medium;

import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.ontology.context.Context;
import com.prinego.domain.entity.ontology.event.Event;
import com.prinego.domain.entity.ontology.location.Location;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mester on 18/08/14.
 */
public @Data class Picture extends Medium implements Serializable {

    public Picture() {
        super.initialize();
    }

    // copy constructor
    public Picture(Picture another) {

        setUrl(new String(another.getUrl()));
        if ( another.getDateTaken() == null ) {
            setDateTaken(null);
        } else {
            setDateTaken((Date) another.getDateTaken().clone());
        }
        if ( another.getIsDisliked() == null ) {
            setIsDisliked(null);
        } else {
            setIsDisliked(new Boolean(another.getIsDisliked()));
        }
        if ( another.getHasMatureContent() == null ) {
            setHasMatureContent(null);
        } else {
        	setHasMatureContent(new Boolean(another.getHasMatureContent()));
        }
        
        if ( another.getMood() == null ) {
            setMood(null);
        } else {
        	setMood(new String(another.getMood()));
        }
        

        Set<Agent> people = new HashSet();
        for ( Agent a : another.getIncludedPeople() ) {
            people.add(new Agent(a));
        }
        setIncludedPeople(people);

        Set<Location> locations = new HashSet();
        for ( Location l : another.getIncludedLocations() ) {
            locations.add(l);
        }
        setIncludedLocations(locations);

        Set<Context> contexts = new HashSet();
        for ( Context c : another.getIsInContexts() ) {
            contexts.add(c);
        }
        setIsInContexts(contexts);
        
        if ( another.getIsTakenIn() == null ) {
            setIsTakenIn(null);
        } else {
        	setIsTakenIn( new Event( another.getIsTakenIn())); // for now only pictures are covered. TODO if necessary
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Medium medium = (Medium) other;

        if (!getUrl().equals(medium.getUrl())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
