package com.slang.parser;

import com.slang.Type;
import com.slang.ast.*;
import com.slang.lexer.Lexer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sarath on 16/3/17.
 */
public class Parser {

    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Map<String, Function> parseFunctions() {
        Token token = null;
        Map<String, Function> functions = new LinkedHashMap<>();
        do {
            Function function = parseFunction();
            functions.put(function.getName(), function);
            token = lexer.getCurrentToken();
        } while (Token.UNKNOWN != token);
        return functions;
    }

    public Function parseFunction() {
        //First call to parseFunction require eat and once parsing has started we should not call eat() as it will
        //skip a token
        if (null == lexer.getPreviousToken()) {
            lexer.eat();
        }
        lexer.expect(Token.FUNCTION);
        lexer.eat();
        Type returnType = null;
        switch (lexer.getCurrentToken()) {
            case VOID:
                returnType = Type.VOID;
                break;
            case INT:
                returnType = Type.INTEGER;
                break;
            case LONG:
                returnType = Type.LONG;
                break;
            case FLOAT:
                returnType = Type.FLOAT;
                break;
            case DOUBLE:
                returnType = Type.DOUBLE;
                break;
            case BOOL:
                returnType = Type.BOOL;
                break;
            default:
                throw new RuntimeException("Return type cannot be " + lexer.getCurrentToken());
        }

        lexer.eat();
        if (lexer.getCurrentToken() != Token.VAR_NAME) {
            throw new RuntimeException("Function name expected");
        }
        String name = lexer.getVariableName();

        lexer.eat();
        lexer.expect(Token.OPAR);

        LinkedHashMap<String, Type> formalArguments = new LinkedHashMap<>();

        lexer.eat();
        while (lexer.getCurrentToken() != Token.CPAR) {
            Type varType = getType(lexer.getCurrentToken());
            lexer.eat();
            if (lexer.getCurrentToken() != Token.VAR_NAME) {
                throw new RuntimeException("Formal parameter name expected");
            }
            String varName = lexer.getVariableName();
            lexer.eat();

            formalArguments.put(varName, varType);

            if (lexer.getCurrentToken() != Token.COMMA) {
                break;
            }
            lexer.eat();
        }

        if (lexer.getPreviousToken() == Token.COMMA) {
            throw new RuntimeException("Expecting a formal parameter after comma");
        }

        lexer.expect(Token.CPAR);
        lexer.eat();
        List<Statement> functionBody = new ArrayList<>();

        do {
            Statement statement = parseStatement();
            functionBody.add(statement);
        } while (lexer.getCurrentToken() != Token.END);

        lexer.expect(Token.END);

        Function function = new Function(name, returnType, formalArguments, functionBody);
        lexer.eat();
        System.out.println(function);
        return function;

    }

    public List<Statement> parseStatements() {
        Token token = null;
        List<Statement> statements = new ArrayList<>();
        do {
            statements.add(parseStatement());
            token = lexer.getCurrentToken();
        } while (Token.UNKNOWN != token);
        return statements;
    }

    public Statement parseStatement() {
        //First call to parseStatement require eat and once parsing has started we should not call eat() as it will
        //skip a token
        if (null == lexer.getPreviousToken()) {
            lexer.eat();
        }
        Token token = lexer.getCurrentToken();
        if(Token.PRINT == token) {
            Expression expression = parseExpression();
            lexer.expect(Token.SEMICLN);
            lexer.eat();
            return new PrintStatement(expression);
        }

        if(Token.PRINTLN == token) {
            Expression expression = parseExpression();
            lexer.expect(Token.SEMICLN);
            lexer.eat();
            return new PrintlnStatement(expression);
        }

        if(Token.VAR == token) {
            Expression expression = parseExpression();
            VariableExpression variableExpression = (VariableExpression) expression;

            if(Token.SEMICLN == lexer.getCurrentToken()) {
                lexer.eat();
                return new VariableDeclarationStatement(variableExpression);
            } else if(Token.EQ == lexer.getCurrentToken()) {
                Expression rhsExp = parseExpression();
                lexer.expect(Token.SEMICLN);
                lexer.eat();
                return new VariableDeclAndAssignStatement(new VariableDeclarationStatement(variableExpression),
                        new VariableAssignmentStatement(variableExpression.getVariableName(), rhsExp));
            }

        }

        if(Token.VAR_NAME == token) {
            lexer.eat();
            lexer.expect(Token.EQ);
            String varName = lexer.getVariableName();
            Expression expression = parseExpression();
            VariableAssignmentStatement variableAssignmentStatement = new VariableAssignmentStatement(varName, expression);
            lexer.expect(Token.SEMICLN);
            lexer.eat();
            return variableAssignmentStatement;

        }

        if(Token.IF == token) {
            lexer.eat();

            Expression expression = parseExpression();

            lexer.eat();

            lexer.expect(Token.THEN);

            lexer.eat();

            //If no body
            if(lexer.getCurrentToken() == Token.ENDIF) {
                throw new RuntimeException("Empty If condition is not allowed");
            }

            List<Statement> trueBody = new ArrayList<>();

            do {
                Statement statement = parseStatement();
                trueBody.add(statement);
            } while (lexer.getCurrentToken() != Token.ENDIF && lexer.getCurrentToken() != Token.ELSE);

            List<Statement> falseBody = new ArrayList<>();

            //IF false part exists
            if(lexer.getCurrentToken() == Token.ELSE) {
                lexer.eat();
                do {
                    Statement statement = parseStatement();
                    falseBody.add(statement);
                } while (lexer.getCurrentToken() != Token.ENDIF);
            }
            lexer.expect(Token.ENDIF);
            lexer.eat();
            return new IfStatement(expression, trueBody, falseBody);
        }

        if(Token.WHILE == token) {
            lexer.eat();

            Expression expression = parseExpression();

            lexer.eat();

            if(lexer.getCurrentToken() == Token.WEND ) {
                throw new RuntimeException("Empty loop is not allowed");
            }

            List<Statement> body = new ArrayList<>();

            do {
                Statement statement = parseStatement();
                body.add(statement);
            } while (lexer.getCurrentToken() != Token.WEND);

            lexer.expect(Token.WEND);

            lexer.eat();
            return new WhileStatement(expression, body);
        }

        if(Token.BREAK == token) {
            lexer.eat();
            lexer.expect(Token.SEMICLN);
            lexer.eat();
            return new BreakStatement();
        }

        System.out.println(lexer);
        throw new RuntimeException("Expected PRINT or PRINTLN");

    }

    private Type getType(Token currentToken) {
        switch (lexer.getCurrentToken()) {
            case VOID:
                return Type.VOID;
            case INT:
                return Type.INTEGER;
            case LONG:
                return Type.LONG;
            case FLOAT:
                return Type.FLOAT;
            case DOUBLE:
                return Type.DOUBLE;
            case BOOL:
                return Type.BOOL;
            default:
                throw new RuntimeException("Formal param type cannot be " + lexer.getCurrentToken());
        }
    }

    public Expression parseExpression() {
        Expression expression = parseRelationalExpression();
        Token token = lexer.getCurrentToken();
        while (Token.ANDAND == token || Token.OR == token) {
            Expression rightExp = parseRelationalExpression();
            expression = new LogicalExpression(expression, rightExp, token);
            token = lexer.getCurrentToken();
        }
        return expression;
    }

    public Expression parseRelationalExpression() {
        Expression expression = parseArithmeticExpression();
        Token token = lexer.getCurrentToken();
        while (Token.DEQ == token || Token.LT == token
                || Token.LTE == token || Token.GT == token
                || Token.GTE == token) {
            Expression rightExp = parseArithmeticExpression();
            expression = new RelationalExpression(expression, rightExp, token);
            token = lexer.getCurrentToken();
        }
        return expression;
    }

    public Expression parseArithmeticExpression() {
        Expression expression = parseTerm();
        Token token = lexer.getCurrentToken();
        while (Token.ADD == token || Token.SUB == token) {
            Expression rightExp = parseTerm();
            expression = new ArithmeticExpressionExpression(expression, rightExp, token);
            token = lexer.getCurrentToken();
        }
        return expression;
    }

    public Expression parseTerm() {
        Expression expression = parseFactor();
        lexer.eat();
        Token token = lexer.getCurrentToken();
        while (Token.MUL == token || Token.DIV == token) {
            Expression rightExp = parseFactor();
            expression = new ArithmeticExpressionExpression(expression, rightExp, token);
            lexer.eat();
            token = lexer.getCurrentToken();
        }
        return expression;
    }

    public Expression parseFactor() {
        lexer.eat();
        Token token = lexer.getCurrentToken();

        switch (token) {
            case NUM:
                switch (lexer.getNumType()) {
                    case DOUBLE:
                        return new NumericExpression(lexer.getDoubleNum());
                    case FLOAT:
                        return new NumericExpression(lexer.getFloatNum());
                    case LONG:
                        return new NumericExpression(lexer.getLongNum());
                    case INTEGER:
                        return new NumericExpression(lexer.getIntegerNum());
                    default:
                        throw new RuntimeException("Unsupported Data type");
                }
            case ADD:
                return parseFactor();
            case SUB:
                Expression leftExp = parseFactor();
                return new UnaryExpression(leftExp,
                        lexer.getPreviousToken());
            case OPAR:
                Expression expression = parseExpression();
                lexer.expect(Token.CPAR);
                return expression;
            case STRLTRL:
                return new StringLiteral(lexer.getStringLiteral());
            case VAR_NAME:
                return new VariableExpression(lexer.getVariableName());
            case TRUE:
                return new BooleanExpression(true);
            case FALSE:
                return new BooleanExpression(false);
            case NOT:
                Expression notExp = parseFactor();
                return new NotExpression(notExp);

            default:
                throw new RuntimeException("Un expected token at leaf : " + token);
        }
    }
}
