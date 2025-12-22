package com.interpreter.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean hadError = false;
    static boolean hadRuntimeError = true;
    static Interpreter interpreter = new Interpreter();
    static boolean isRepl = false;
    static String inputFileName;
    static List<String> sourceLines;
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage:JLOX [Script]");
            System.exit(64);
        } else if (args.length == 1) {
            inputFileName = args[0];
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        isRepl = true;
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print(">");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }

    }

    private static void run(String source) {
        sourceLines = List.of(source.split("\n", -1));
        Scanner sc = new Scanner(source);
        List<Token> tokens = sc.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        // Stop if there was a syntax error.
        if (hadError) {
            return;
        }
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        // Stop if there was error in resolution.
        if (hadError) {
            return;
        }
        interpreter.interpret(statements);
    }

    static void error(int line, int column, String message) {
        report(line, column, message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, token.column, message + " at end.");
        } else {
            report(token.line, token.column, message);
        }
    }


    // static void report(int line, String where, String message) {
    //     System.out.println("[line " + line + "] Error " + where + ": " + message);
    //     hadError = true;
    // }

    static void report(int line, int column, String message) {
        hadError = true;

        System.err.println("Error: " + message);

        if (inputFileName != null) {
            System.err.println("  --> " + inputFileName + ":" + line + ":" + column);
        }

        if (sourceLines != null && line - 1 < sourceLines.size()) {
            String srcLine = sourceLines.get(line - 1);

            System.err.println("   |");
            System.err.printf("%2d | %s%n", line, srcLine);
            System.err.print("   | ");

            // spaces before caret
            for (int i = 1; i < column; i++) {
                System.err.print(" ");
            }
            System.err.println("^");
        }
    }

    
    static void runtimeError(RuntimeError error) {
        hadRuntimeError = true;

        Token token = error.token;
        System.err.println("Runtime Error: " + error.getMessage());

        if (inputFileName != null) {
            System.err.println("  --> " + inputFileName + ":" + token.line + ":" + token.column);
        }

        if (sourceLines != null && token.line - 1 < sourceLines.size()) {
            String srcLine = sourceLines.get(token.line - 1);

            System.err.println("   |");
            System.err.printf("%2d | %s%n", token.line, srcLine);
            System.err.print("   | ");

            for (int i = 1; i < token.column; i++) {
                System.err.print(" ");
            }
            System.err.println("^");
        }
    }

}
