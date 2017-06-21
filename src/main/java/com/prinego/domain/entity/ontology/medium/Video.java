package com.prinego.domain.entity.ontology.medium;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by mester on 18/08/14.
 */
public @Data class Video extends Medium implements Serializable {

    public Video() {
        super.initialize();
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
