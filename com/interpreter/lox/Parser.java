package com.interpreter.lox;

import static com.interpreter.lox.TokenType.EOF;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class Parser {

    private static class ParseError extends RuntimeException {
    }
    private final List<Token> tokens;
    private int current = 0;
    private int loopDepth = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr expr = assignment();
        while (match(TokenType.COMMA)) {
            Expr right = assignment();
            expr = new Expr.Comma(expr, right);
        }
        return expr;
    }

    private Expr assignment() {
        Expr expr = ternary();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable variable) {
                Token name = variable.name;
                return new Expr.Assign(name, value);

            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }
    

    private Expr ternary() {
        Expr expr = logicOr();
        if (match(TokenType.QUESTION)) {
            Token question = previous();
            Expr then = ternary();
            Token colon = consume(TokenType.COLON, "Expect ':' after expression");
            Expr otherwise = ternary();
            expr = new Expr.Ternary(expr, then, otherwise, question, colon);
        }
        return expr;
    }
    private Expr logicOr() {
        Expr expr = logicAnd();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicAnd();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Stmt whileStatement(){
        consume(TokenType.LEFT_PAREN,"Expected '(' after while");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN,"Expected ')' after while condition");
        loopDepth++;
        Stmt statement = statement();
        loopDepth--;
        return new Stmt.While(condition,statement);
    }
    private Expr logicAnd() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }
    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        if (check(TokenType.MINUS) || check(TokenType.PLUS)) {
            error(peek(), "Got a " + peek().lexeme + "without any left operand.");
            Token token = advance();
            Expr right = factor();
            return right;
        }
        Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }
        if (match(TokenType.STRING, TokenType.NUMBER)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        throw error(peek(), "Expect Expression");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private Token advance() {
        if (!isAtEnd()) {
            this.current++;
        }
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private void synchronize() {
        advance();
        if (previous().type == TokenType.SEMICOLON) {
            return;
        }

        while (!isAtEnd()) {
            switch (peek().type) {
                case TokenType.CLASS, TokenType.FUN, TokenType.VAR, TokenType.FOR, TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> {
                    return;
                }
                default -> {
                    advance();
                }
            }

        }
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expected ; at end of statement");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expected ; at end of statement");
        return new Stmt.Expression(value);
    }

    private Stmt statement() {
        if(match(TokenType.CONTINUE)){
            return continueStatement();
        }
        if(match(TokenType.FOR)){
            return forStatement();
        }
        if(match(TokenType.WHILE)){
            return whileStatement();
        }
        if(match(TokenType.IF)){
            return ifStatement();
        }
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(block());
        }
        if(match(TokenType.BREAK)){
            return breakStatement();
        }
        return expressionStatement();
    }

    private Stmt continueStatement(){
        if(loopDepth==0){
            error(previous(),"Cannot use 'continue' outside of a loop.");
        }
        consume(TokenType.SEMICOLON,"Expect ';' after continue.");
        return new Stmt.Continue();
    }

    private Stmt breakStatement(){
        if(loopDepth==0){
            error(previous(),"Cannot use 'break' outside of a loop.");
        }
        consume(TokenType.SEMICOLON, "Expect ';' after 'break'.");
        return new Stmt.Break();
    }

    private Stmt forStatement(){
        consume(TokenType.LEFT_PAREN,"Expect'(' after 'for'.");
        Stmt initializer;
        if(match(TokenType.SEMICOLON)){
            initializer = null;
        }else if(check(TokenType.VAR)){
            // System.err.println("true");
            initializer = declaration();
        }else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(TokenType.SEMICOLON))
        {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");
        Expr increment = null;
        if(!check(TokenType.RIGHT_PAREN)){
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN,"Expect ')' after for clause.");
        loopDepth++;
        Stmt body = statement();
        loopDepth--;
        
        if(condition == null){
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body,new Stmt.Expression(increment));
        if(initializer!=null){
            body= new Stmt.Block(Arrays.asList(initializer,body));
        }
        return body;
    }

    private Stmt ifStatement(){
        consume(TokenType.LEFT_PAREN,"Expected '(' after if");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN,"Expected ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(TokenType.ELSE)){
            elseBranch = statement();
        }
        return new Stmt.If(condition,thenBranch,elseBranch);
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE,"Expect '}' after block." );
        return statements;
    }
    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        // System.out.println(name);
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    public List<Stmt> parse() {
        try {
            List<Stmt> statements = new ArrayList<>();
            while (!isAtEnd()) {
                statements.add(declaration());
            }
            return statements;
        } catch (ParseError e) {
            return null;
        }
    }
}
