package com.prinego.util;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mester on 13/10/14.
 */
public class MySetUtils {

    public static <T> void clearNulls(Set<T> aSet) {
        Preconditions.checkNotNull(aSet);

        Iterator<T> iter = aSet.iterator();
        while (iter.hasNext()) {
            if ( iter.next() == null ) {
                iter.remove();
            }
        }
    }

    public static <T> Set<T> clearNullsInImmutable(Set<T> aSet) {
        Preconditions.checkNotNull(aSet);

        Set<T> newSet = new HashSet();
        Iterator<T> iter = aSet.iterator();
        while (iter.hasNext()) {
            T elem = iter.next();
            if ( elem != null ) {
                newSet.add(elem);
            }
        }
        return newSet;
    }

    public static <T> Set<T> flatten(Collection<Set<T>> coll) {
        if ( coll == null ) {
            return null;
        }

        Set<T> flattenedSet = new HashSet();
        for ( Set<T> aSet : coll ) {
            flattenedSet.addAll(aSet);
        }

        return flattenedSet;
    }

    public static <T> boolean isIntersectionEmpty(Set<T> a1, Set<T> a2) {

        Preconditions.checkNotNull(a1);
        Preconditions.checkNotNull(a2);

        for ( T elem : a1 ) {
            if ( a2.contains(elem) ) {
                return false;
            }
        }

        return true;
    }

}
