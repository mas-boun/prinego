package com.prinego.ontology.object2ontology.domain.reflection;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * Created by mester on 17/08/14.
 */
public @Data class MyField {

    private Field field;
    //private Class clazz;
    private MyFieldType type;

    public MyField(Field field) {
        this.field = field;
    }

    public boolean isDataProperty() {
        return MyFieldType.PLURAL_ORDINARY.equals(type)
            || MyFieldType.SINGLE_ORDINARY.equals(type);
    }

    public boolean isObjectProperty() {
        return MyFieldType.PLURAL_MY_ONTO_OBJECT.equals(type)
                || MyFieldType.SINGLE_MY_ONTO_OBJECT.equals(type);
    }

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public MyFieldType getType() {
		return type;
	}

	public void setType(MyFieldType type) {
		this.type = type;
	}

}
