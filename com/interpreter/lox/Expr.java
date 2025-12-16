package com.interpreter.lox;

import java.util.List;

@SuppressWarnings("unused")
abstract class Expr {

    interface Visitor<R> {

        R visitCommaExpr(Comma expr);

        R visitBinaryExpr(Binary expr);

        R visitUnaryExpr(Unary expr);

        R visitLiteralExpr(Literal expr);

        R visitGroupingExpr(Grouping expr);

        R visitTernaryExpr(Ternary expr);

        R visitVariableExpr(Variable expr);

        R visitAssignExpr(Assign expr);

        R visitLogicalExpr(Logical expr);

        R visitCallExpr(Call expr);
    }

    public static class Comma extends Expr {

        final Expr left;
        final Expr right;

        Comma(Expr left, Expr right) {
            this.left = left;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCommaExpr(this);
        }
    }

    public static class Ternary extends Expr {

        final Expr condition;
        final Expr then;
        final Expr otherwise;
        final Token question;

        final Token colon;

        Ternary(Expr condition, Expr then, Expr otherwise, Token question, Token colon) {
            this.condition = condition;
            this.then = then;
            this.otherwise = otherwise;
            this.colon = colon;
            this.question = question;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }
    }

    public static class Call extends  Expr{

        public Call(Expr callie,Token paren,List<Expr> arguments) {
            this.callie = callie;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitCallExpr(this);
        }

        final Expr callie;
        final Token paren;
        final List<Expr> arguments;
    }
    public static class Logical extends Expr{
        
        Logical(Expr left, Token operator, Expr right){
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        final Expr left;
        final Token operator;
        final Expr right;
        
        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Binary extends Expr {

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
        final Expr left;
        final Token operator;
        final Expr right;
    }

    public static class Variable extends Expr {

        Variable(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        public Variable(Token name) {
            this.name = name;
            this.initializer = null;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }

        final Token name;
        final Expr initializer;
    }

    public static class Unary extends Expr {

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
        final Token operator;
        final Expr right;
    }

    public static class Assign extends Expr {

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
        public final Token name;
        public final Expr value;
    }

    public static class Literal extends Expr {

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
        final Object value;
    }

    public static class Grouping extends Expr {

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
        final Expr expression;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
