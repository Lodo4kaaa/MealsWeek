package org.example.mealsweek.exception;

import lombok.Getter;
import java.util.Map;


@Getter
public class ResourceNotFoundException extends RuntimeException{
    private final String resource;
    private final String field;
    private final Object value;

    public ResourceNotFoundException(String resource, String field, Object value){
        super(resource+" with "+field+" = "+value+" not found");
        this.resource = resource;
        this.field = field;
        this.value = value;
    }

    public Map<String,Object> getProperties(){
        return Map.of("resource", this.resource, "field", this.field, "value", this.value);
    }
}
