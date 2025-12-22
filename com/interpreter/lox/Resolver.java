package com.interpreter.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class Resolver implements  Expr.Visitor<Void>,Stmt.Visitor<Void>{

    private final Interpreter interpreter;
    private final Stack<Map<String,Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    private enum  FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER,
        STATIC
    }

    private enum ClassType{
        NONE,
        CLASS
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt){
        return null;
    }

    @Override 
    public Void visitBreakStmt(Stmt.Break stmt){
        return null;
    }


    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if(stmt.initializer!=null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }
        
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword,
                        "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callie);
        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr){
        resolve(expr.condition);
        resolve(expr.then);
        resolve(expr.otherwise);
        return  null;
    }

    @Override 
    public Void visitCommaExpr(Expr.Comma expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }
    @Override 
    public Void visitVariableExpr(Expr.Variable expr){
        if(!scopes.isEmpty() && Objects.equals(scopes.peek().get(expr.name.lexeme), Boolean.FALSE)){
            Lox.error(expr.name,"Can't read a variable in it's own initializer");
        }
        resolveLocal(expr,expr.name);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt,FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt){
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);
        
        for(Stmt.Function method:stmt.methods){
            FunctionType declaration ;
            if (method.isStatic) {
                declaration = FunctionType.STATIC;
                resolveFunction(method, declaration);
            }else {
                beginScope();
                scopes.peek().put("this", true);
                declaration = FunctionType.METHOD;
                if(method.name.lexeme.equals("init")){
                    declaration = FunctionType.INITIALIZER;
                }
                resolveFunction(method, declaration);
                endScope();
            }
        }
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr){
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword,
                    "Can't use 'this' outside of a class.");
            return null;
        }
        if(currentFunction==FunctionType.STATIC){
            Lox.error(expr.keyword,"Can't use 'this' on static method.");
        }
        resolveLocal(expr,expr.keyword);
        return null;
    }
    
    @Override
    public Void visitGetExpr(Expr.Get expr){
        resolve(expr.object);
        return null;
    }

    private void resolveFunction(Stmt.Function stmt,FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for(Token param:stmt.parameters){
            declare(param);
            define(param);
        }
        resolve(stmt.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void endScope(){
        scopes.pop();
    }
    private void resolveLocal(Expr expr,Token name){
        for(int i = scopes.size()-1;i>=0;i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr,scopes.size()-1-i);
                return;
            }
        }
    }

    private void declare(Token name){
        if(scopes.isEmpty()) return;
        Map<String,Boolean> scope = scopes.peek();
        
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name,
                    "Already variable with this name in this scope.");
        }
        scope.put(name.lexeme,false);
    }
    
    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        Map<String, Boolean> scope = scopes.peek();
        scope.put(name.lexeme, true);
    }

    public void resolve(List<Stmt> statements){
        for(Stmt stmt:statements){
            resolve(stmt);
        }
    }

    private void resolve(Stmt stmt){
        stmt.accept(this);
    }

    private void resolve(Expr expr){
        expr.accept(this);
    }

    private void beginScope(){
        scopes.push(new HashMap<>());
    }

}
 