package com.interpreter.lox;

import java.util.List;

public abstract class Stmt {

    public interface Visitor<R> {

        R visitExpressionStmt(Expression stmt);

        R visitPrintStmt(Print stmt);

        R visitVarStmt(Var stmt);

        R visitBlockStmt(Block stmt);

        R visitIfStmt(If stmt);

        R visitWhileStmt(While stmt);

        R visitBreakStmt(Break stmt);

        R visitContinueStmt(Continue stmt);

        R visitFunctionStmt(Function stmt);

        R visitReturnStmt(Return stmt);
    }

    public static class Return extends Stmt{
        
        Return(Token keyword,Expr value){
            this.keyword = keyword;
            this.value = value;
        }
        
        final Expr value;
        final Token keyword;
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Function extends Stmt{
        

        public Function(Token name,List<Token> parameters,List<Stmt> body) {
            this.name =name;
            this.parameters =parameters;
            this.body =body;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitFunctionStmt(this);
        }

        final Token name; 
        final List<Token> parameters;
        final List<Stmt> body;
    }
    public static class Continue extends Stmt{
        @Override 
        <R> R accept(Visitor<R> visitor){
            return visitor.visitContinueStmt(this);
        }
    }
    public static class If extends Stmt{
        
        If(Expr condition,Stmt thenBranch,Stmt elseBranch){
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;
        @Override 
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class Break extends Stmt{

        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitBreakStmt(this);
        }
    }
    public static class Expression extends Stmt {

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
        final Expr expression;
    }

    public static class While extends Stmt{

        While(Expr condition,Stmt body,Stmt increment){
            this.body = body;
            this.condition = condition;
            this.increment = increment;
        }

        While(Expr condition, Stmt body) {
            this.body = body;
            this.condition = condition;
            this.increment = null;
        }
        final Expr condition;
        final Stmt body;
        final Stmt increment;

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitWhileStmt(this);
        }
    }

    public static class Var extends Stmt {

        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
        final Token name;
        final Expr initializer;
    }

    public static class Block extends Stmt {

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
        final List<Stmt> statements;
    }

    public static class Print extends Stmt {

        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
        final Expr expression;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
