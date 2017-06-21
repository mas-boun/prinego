package com.prinego.ontology.object2ontology.util;

import java.lang.reflect.Field;

import com.google.common.base.Preconditions;
import com.prinego.domain.annotation.MyOntoField;
import com.prinego.ontology.object2ontology.domain.MyOntologyPropertyMeta;
import com.prinego.ontology.object2ontology.domain.MyOntologyPropertyType;
import com.prinego.ontology.object2ontology.domain.reflection.MyField;

/**
 * Created by mester on 17/08/14.
 */
public class Object2OntologyUtil {

    private static String getMyPropName(Field field) {
        MyOntoField ann = field.getAnnotation(MyOntoField.class);
        Preconditions.checkNotNull(ann);

        return ann.propertyName();
    }

    public static MyOntologyPropertyMeta getMyOntologyPropertyMeta(MyField myField) {

        Preconditions.checkNotNull(myField);
        Preconditions.checkNotNull(myField.getType());

        Field field = myField.getField();

        MyOntologyPropertyMeta myMeta = new MyOntologyPropertyMeta();

        if ( myField.isDataProperty() ) {
            myMeta.setPropertyType(MyOntologyPropertyType.DATA_PROPERTY);
        } else if ( myField.isObjectProperty() ) {
            myMeta.setPropertyType(MyOntologyPropertyType.OBJECT_PROPERTY);
        }
        myMeta.setPropertyName(getMyPropName(field));

        return myMeta;
    }



}
