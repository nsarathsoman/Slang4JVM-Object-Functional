package com.slang.lexer;

import com.slang.ast.Token;

/**
 * Created by sarath on 16/3/17.
 */
public class Lexer {

    private Token previousToken;
    private Token currentToken;
    private final String module;
    private final int moduleLen;
    private int index;
    private double num;
    private String stringLiteral;
    private String variableName;

    public Lexer(String module) {
        this.module = module;
        moduleLen = module.length();
    }

    public void eat() {

        if(isEndOfModule()) {
            previousToken = currentToken;
            currentToken = Token.UNKNOWN;
        }

        moduleStream:
        while (isNotEndOfModule()) {
            switch (module.charAt(index)) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    num = readNum();
                    previousToken = currentToken;
                    currentToken = Token.NUM;
                    break moduleStream;
                case ' ':
                case '\n':
                    index ++;
                    break ;
                case '+':
                    previousToken = currentToken;
                    currentToken = Token.ADD;
                    index++;
                    break moduleStream;
                case '-':
                    previousToken = currentToken;
                    currentToken = Token.SUB;
                    index++;
                    break moduleStream;
                case '/':
                    previousToken = currentToken;
                    currentToken = Token.DIV;
                    index++;
                    break moduleStream;
                case '*':
                    previousToken = currentToken;
                    currentToken = Token.MUL;
                    index++;
                    break moduleStream;
                case '(':
                    previousToken = currentToken;
                    currentToken = Token.OPAR;
                    index++;
                    break moduleStream;
                case ')':
                    previousToken = currentToken;
                    currentToken = Token.CPAR;
                    index++;
                    break moduleStream;
                case ';':
                    previousToken = currentToken;
                    currentToken = Token.SEMICLN;
                    index++;
                    break moduleStream;
                case '=':
                    previousToken = currentToken;
                    currentToken = Token.EQ;
                    index++;
                    break moduleStream;
                case '"':
                    stringLiteral  = readString();
                    previousToken = currentToken;
                    currentToken = Token.STRLTRL;
                    break moduleStream;
                default:
                    String keyword = readKeyWord();
                    previousToken = currentToken;
                    Token tempToken = TokenLookup.getToken(keyword);
                    if(Token.UNKNOWN == tempToken) {
                        variableName = keyword;
                        currentToken = Token.VAR_NAME;
                    } else if(Token.VAR == tempToken) {
                        //Handling reserved type on variable name
                        variableName = keyword;
                        currentToken = Token.VAR;
                    } else {
                        currentToken = tempToken;
                    }
                    break moduleStream;
            }

        }

    }

    private String readKeyWord() {
        StringBuilder keyWordBuilder = new StringBuilder();
        //Iterating till end of module
        while (isNotEndOfModule()) {
            char c = module.charAt(index);

            if(c == ' ' || c == '\n' || c == ';') {
                break;
            }

            keyWordBuilder.append(c);
            index++;
        }
        return keyWordBuilder.toString();
    }

    private String readString() {
        StringBuilder keyWordBuilder = new StringBuilder();
        //Iterating till end of module
        index++;
        boolean closeQuotesFound = false;
        while (isNotEndOfModule()) {
            char c = module.charAt(index);

            if(c == '"') {
                closeQuotesFound = true;
                index++;
                break;
            }

            keyWordBuilder.append(c);
            index++;
        }

        if(!closeQuotesFound) {
            throw new RuntimeException("Closing quotes not found for the string literal");
        }

        return keyWordBuilder.toString();
    }

    private double readNum() {
        boolean foundDot = false;
        StringBuilder numberBuilder = new StringBuilder();
        //Iterating till end of module
        while (isNotEndOfModule()) {
            char c = module.charAt(index);

            if(isNotNumeric(c)) {
                break;
            }

            if (foundDot) {
                throw new RuntimeException("Found '.' more than two times in the number");
            }

            if ('.' == num) {
                foundDot = true;
            }

            numberBuilder.append(c);
            index++;

        }
        return Double.valueOf(numberBuilder.toString());
    }

    private boolean isNumeric(char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '.':
                return true;

            default:
                return false;
        }
    }

    private boolean isNotNumeric(char c) {
        return !isNumeric(c);
    }

    public boolean isEndOfModule() {
        if(index < moduleLen) {
            return false;
        }

        return true;
    }

    public boolean isNotEndOfModule() {
        return !isEndOfModule();
    }

    public void expect(Token num) {
        if(num != currentToken) {
            throw new RuntimeException("Error Token found is "+ currentToken+", Expected token :" + num);
        }
    }

    public Token getPreviousToken() {
        return previousToken;
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public String getModule() {
        return module;
    }


    public int getModuleLen() {
        return moduleLen;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getNum() {
        return num;
    }

    public String getStringLiteral() {
        return stringLiteral;
    }

    public String getVariableName() {
        return variableName;
    }

}
