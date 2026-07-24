package com.vishal.jarvis;

import java.text.DecimalFormat;
import java.util.Locale;

public class CalculatorEngine {
    private String expression;
    private int index;

    public String calculate(String spokenExpression) {
        if (spokenExpression == null || spokenExpression.trim().isEmpty()) {
            return null;
        }

        expression = normalize(spokenExpression);
        index = 0;
        try {
            double value = parseExpression();
            skipSpaces();
            if (index != expression.length()) {
                return null;
            }
            return new DecimalFormat("0.########").format(value);
        } catch (ArithmeticException exception) {
            return null;
        }
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.US)
                .replace("plus", "+")
                .replace("add", "+")
                .replace("minus", "-")
                .replace("subtract", "-")
                .replace("into", "*")
                .replace("times", "*")
                .replace("multiply by", "*")
                .replace("multiplied by", "*")
                .replace("divided by", "/")
                .replace("divide by", "/")
                .replace("over", "/")
                .replace("x", "*")
                .replaceAll("[^0-9+\\-*/(). ]", "");
    }

    private double parseExpression() {
        double value = parseTerm();
        while (true) {
            skipSpaces();
            if (match('+')) {
                value += parseTerm();
            } else if (match('-')) {
                value -= parseTerm();
            } else {
                return value;
            }
        }
    }

    private double parseTerm() {
        double value = parseFactor();
        while (true) {
            skipSpaces();
            if (match('*')) {
                value *= parseFactor();
            } else if (match('/')) {
                double divisor = parseFactor();
                if (divisor == 0) {
                    throw new ArithmeticException("divide by zero");
                }
                value /= divisor;
            } else {
                return value;
            }
        }
    }

    private double parseFactor() {
        skipSpaces();
        if (match('+')) {
            return parseFactor();
        }
        if (match('-')) {
            return -parseFactor();
        }
        if (match('(')) {
            double value = parseExpression();
            if (!match(')')) {
                throw new ArithmeticException("missing parenthesis");
            }
            return value;
        }
        return parseNumber();
    }

    private double parseNumber() {
        skipSpaces();
        int start = index;
        while (index < expression.length()) {
            char character = expression.charAt(index);
            if (!Character.isDigit(character) && character != '.') {
                break;
            }
            index++;
        }
        if (start == index) {
            throw new ArithmeticException("number expected");
        }
        return Double.parseDouble(expression.substring(start, index));
    }

    private boolean match(char expected) {
        skipSpaces();
        if (index < expression.length() && expression.charAt(index) == expected) {
            index++;
            return true;
        }
        return false;
    }

    private void skipSpaces() {
        while (index < expression.length() && expression.charAt(index) == ' ') {
            index++;
        }
    }
}
