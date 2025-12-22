package com.interpreter.lox;

import java.util.List;
import java.util.Map;

public class LoxClass  implements LoxCallable{
    final String name;
    final Map<String,LoxFunction> methods ;
    final Map<String,LoxFunction> staticMethods;
    final LoxClass superClass;
    LoxClass(String name,LoxClass superClass,Map<String,LoxFunction> methods,Map<String, LoxFunction> staticMethods) {
        this.name = name;
        this.methods = methods;
        this.staticMethods = staticMethods;
        this.superClass = superClass;
    }

    public LoxFunction findMethod(Token name){
        if (methods.containsKey(name.lexeme)){
            return methods.get(name.lexeme);
        }
        if (this.superClass != null) {
            return this.superClass.findMethod(name);
        }
        return  null;
    }

    public Object get(Token name) {
        // if (this.fields.containsKey(name.lexeme)) {
        //     return this.fields.get(name.lexeme);
        // }
        LoxFunction method =null;
        if(this.staticMethods.containsKey(name.lexeme)){
             method = this.staticMethods.get(name.lexeme);
        }
        if(method!=null){
            return method;
        }
        if(this.superClass.staticMethods.containsKey(name.lexeme)){
            return this.superClass.staticMethods.get(name.lexeme);
        }
        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = methods.get("init");
        if(initializer!=null){
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity(){
      LoxFunction initializer= methods.get("init");
        if(initializer == null){
            return 0;
        }
        return initializer.arity();
    }

    @Override
    public String toString() {
        return name;
    }
}
