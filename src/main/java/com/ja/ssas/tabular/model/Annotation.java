package com.ja.ssas.tabular.model;



/**
* Annotation object of Tabular Object Model (TOM)
* 
*/
public class Annotation {

private String name;
private Object value;

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public Object getValue() {
return value;
}

public void setValue(Object value) {
this.value = value;
}

}