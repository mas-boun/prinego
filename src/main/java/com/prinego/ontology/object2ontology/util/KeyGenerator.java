package com.prinego.ontology.object2ontology.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mester on 21/08/14.
 */
public class KeyGenerator {

    public static String generateKey() {

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.S");
        String key = dateFormat.format(now);

        return key;
    }

}
