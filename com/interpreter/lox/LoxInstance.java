package com.interpreter.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    final LoxClass klass;
    Map<String,Object> fields = new HashMap<>();
    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    public Object get(Token name){
        if(this.fields.containsKey(name.lexeme)){
            return this.fields.get(name.lexeme);
        }
        LoxFunction method  = this.klass.findMethod(name);
        if(method != null)  return method.bind(this);
        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    public void set(Token name,Object value){
        this.fields.put(name.lexeme, value);
    }
    @Override
    public String toString(){
        return klass.name + " instance";
    }
    
}
