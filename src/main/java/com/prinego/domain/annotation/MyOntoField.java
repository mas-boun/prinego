package com.prinego.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mester on 18/08/14.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface MyOntoField {

    String propertyName();

}
