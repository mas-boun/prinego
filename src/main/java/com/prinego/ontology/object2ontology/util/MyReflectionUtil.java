package com.prinego.ontology.object2ontology.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import com.google.common.base.Preconditions;
import com.prinego.domain.annotation.MyOntoClass;
import com.prinego.ontology.object2ontology.domain.reflection.MyField;
import com.prinego.ontology.object2ontology.domain.reflection.MyFieldType;

/**
 * Created by mester on 17/08/14.
 */
public class MyReflectionUtil {

    public static Object convertFieldToObject(Object obj, Field fieldOfObj) {
        fieldOfObj.setAccessible(true);

        Object fieldObj = null;
        try {
            fieldObj = fieldOfObj.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return fieldObj;
    }

    public static Collection convertFieldToCollection(Object obj, Field fieldOfObj) {
        fieldOfObj.setAccessible(true);

        Collection fieldObj = null;
        try {
            fieldObj = (Collection) fieldOfObj.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return fieldObj;
    }

    public static Object getObjectFromField(Object obj, Field fieldOfObj) {
        fieldOfObj.setAccessible(true);

        Object fieldObj = null;
        try {
            fieldObj = fieldOfObj.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return fieldObj;
    }

    public static MyField convertFieldToMyField(Field field) {

        Preconditions.checkNotNull(field);
        Preconditions.checkNotNull(field.getType());
        Preconditions.checkNotNull(field.getName());

        MyField myField = new MyField(field);

        Class clazz = null;
        MyFieldType myFieldType = null;
        if ( isFieldAConventionalCollection(field) ) {
            clazz = getClassOfParametrizedField(field);
            if ( isMyOntoClass(clazz) ) {
                myFieldType = MyFieldType.PLURAL_MY_ONTO_OBJECT;
            } else {
                myFieldType = MyFieldType.PLURAL_ORDINARY;
            }
        } else {
            clazz = field.getType();
            if ( isMyOntoClass(clazz) ) {
                myFieldType = MyFieldType.SINGLE_MY_ONTO_OBJECT;
            } else {
                myFieldType = MyFieldType.SINGLE_ORDINARY;
            }
        }
        //myField.setClazz(clazz);
        myField.setType(myFieldType);

        return myField;
    }

    public static boolean isFieldAConventionalCollection(Field field) {

        Preconditions.checkNotNull(field);
        Preconditions.checkNotNull(field.getType());
        Preconditions.checkNotNull(field.getName());

        for ( Class c : field.getType().getInterfaces() ) {
            if ( Collection.class.equals(c) ) {
                // Apply MyOntologyUpdaterConventions
                Preconditions.checkArgument(isFieldParametrized(field));

                return true;
            }
        }

        return false;
    }

    private static boolean isFieldParametrized(Field field) {
        Preconditions.checkNotNull(field);
        Preconditions.checkNotNull(field.getType());
        if (field.getGenericType() instanceof ParameterizedType) {
            return true;
        } else {
            return false;
        }
    }

    public static <T> Class<T> getClassOfParametrizedField(Field field) {
        if (isFieldParametrized(field)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            //System.out.println("parameterizedType:" + parameterizedType);
            Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
            return clazz;
        } else {
            return null;
        }
    }

    public static boolean isMyOntoClass(Class clazz) {
        return clazz.isAnnotationPresent(MyOntoClass.class);
    }

}
