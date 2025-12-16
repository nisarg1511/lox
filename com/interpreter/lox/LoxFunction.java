package com.interpreter.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{
    
    private final Stmt.Function declaration;
    private final Environment clouser;

    LoxFunction(Stmt.Function declaration,Environment clouser){
        this.declaration = declaration;
        this.clouser = clouser;
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
            return e.value;
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
