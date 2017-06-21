package com.prinego.domain.entity.rule;

/**
 * @author Dilara Kekulluoglu
 * This class is to create our own Rules. This is counterpart to SWRL Rule ObjectPropertyAtom .
 * */
public class Triple {

    //Name of the atom
    private String name;
    //First argument of the atom
    private String firstItem;
    //Second argument of the atom
    private String secondItem;


    public Triple(){

    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFirstItem() {
        return firstItem;
    }
    public void setFirstItem(String firstItem) {
        this.firstItem = firstItem;
    }
    public String getSecondItem() {
        return secondItem;
    }
    public void setSecondItem(String secondItem) {
        this.secondItem = secondItem;
    }
    public String toString(){

        return new String(name+"("+firstItem+","+secondItem+")");

    }

}