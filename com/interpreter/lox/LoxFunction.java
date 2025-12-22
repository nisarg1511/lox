package com.interpreter.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{
    
    private final Stmt.Function declaration;
    private final Environment clouser;
    private final boolean  isInitializer;
    private final boolean isStatic;

    LoxFunction(Stmt.Function declaration,Environment clouser,boolean  isInitializer,boolean isStatic){
        this.declaration = declaration;
        this.clouser = clouser;
        this.isInitializer = isInitializer;
        this.isStatic = isStatic;
    }

    LoxFunction bind(LoxInstance instance){
            Environment environment = new Environment(clouser);
            environment.define("this", instance);
            return new LoxFunction(declaration, environment, isInitializer, isStatic);
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        try {
            Environment environment = new Environment(clouser);
            for (int i = 0; i < declaration.parameters.size(); i++) {
                environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
            }

            interpreter.executeBlock(declaration.body, environment);
        } catch (Return e) {
            if (isInitializer)
                return this.clouser.getAt(0, "this");
            return e.value;
        }
        if(isInitializer){
            return this.clouser.getAt(0, "this");
        }
        return null;
    }

    @Override
    public int arity(){
        return declaration.parameters.size();
    }

    @Override
    public String toString(){
        return "<fn " + declaration.name.lexeme + ">";
    }
}
