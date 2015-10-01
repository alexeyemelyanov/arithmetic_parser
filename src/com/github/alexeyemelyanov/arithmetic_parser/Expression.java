package com.github.alexeyemelyanov.arithmetic_parser;

import java.util.LinkedList;
import java.util.HashMap;

public class Expression {

    private String expressionString;

    private Number result;

    public static void main(String[] args) {

        Expression expression = parse("((-1+7)+(-1.9 *2)+((5.5 * 6.1)/+1 + (3 / 4 - 1)) * ( 1 / 1 ) / 1)");
        System.out.println(expression.getResult());

        Expression expression2 = parse("-1+(+2)");
        System.out.println(expression2.getResult());

        Expression expression3 = parse("-1/2");
        System.out.println(expression3.getResult());

        Expression expression4 = parse("+1+-1");
        System.out.println(expression4.getResult());

    }

    public static Expression parse(String expressionString) {
        if (expressionString == null) throw new IllegalArgumentException("must be a string, null given");
        expressionString = expressionString.replace(" ", "");
        if (expressionString.length() == 0) throw new IllegalArgumentException("empty string");
        if (!checkParentheses(expressionString)) throw new IllegalArgumentException("invalid string");
        return createExpression(expressionString);
    }

    private static Expression createExpression(String expressionString) {

        char[] expressionChars = expressionString.toCharArray();
        LinkedList<Integer> expressionsBarriers = new LinkedList<>();
        int parentheses = 0;

        for (int i = 0; i < expressionChars.length; i++) {

            if (expressionChars[i] == '(') {
                parentheses++;
                if (parentheses == 1) expressionsBarriers.add(i);
            }

            if (expressionChars[i] == ')') {
                parentheses--;
                if (parentheses == 0) expressionsBarriers.add(i);
            }

        }

        if (expressionsBarriers.size() == 2 && expressionsBarriers.get(0) == 0 && expressionsBarriers.get(1) == expressionChars.length - 1) {

            expressionString = expressionString.substring(1, expressionChars.length - 1);
            return createExpression(expressionString);

        } else {

            LinkedList<String> operands = new LinkedList<>();
            LinkedList<String> operations = new LinkedList<>();
            int checkIndexInSubExpressionRangeResult;

            for (int i = 0; i < expressionChars.length; i++) {

                if (Operation.getBySymbol(expressionChars[i]) != null) {

                    if (expressionChars[i] == '-' || expressionChars[i] == '+') {
                        if (i == 0) {
                            if (Operation.getBySymbol(expressionChars[i + 1]) == null) {
                                operands.add(Character.toString(expressionChars[i]));
                            }
                        } else {
                            if (
                                    Operation.getBySymbol(expressionChars[i - 1]) != null
                                            &&
                                            Operation.getBySymbol(expressionChars[i + 1]) == null
                                    ) {
                                operands.set(operands.size() - 1, Character.toString(expressionChars[i]));
                            } else {
                                operations.add(Character.toString(expressionChars[i]));
                                operands.add("");
                            }
                        }
                        continue;
                    }

                    try {

                        if (i == 0)
                            throw new IllegalArgumentException("incorrect first expression symbol: " + Character.toString(expressionChars[i]));

                        if (Operation.getBySymbol(expressionChars[i - 1]) != null)
                            throw new IllegalArgumentException("invalid operation: " + new String(new char[]{expressionChars[i - 1], expressionChars[i]}));

                        if (i == expressionChars.length - 1)
                            throw new IllegalArgumentException("incorrect last expression symbol: " + Character.toString(expressionChars[i]));

                    } catch (IllegalArgumentException e) {
                        System.err.println(operations);
                        System.err.println(operands);
                        throw e;
                    }

                    operations.add(Character.toString(expressionChars[i]));
                    operands.add("");
                    continue;
                }

                if (operands.size() == 0) {
                    operands.add("");
                }

                checkIndexInSubExpressionRangeResult = checkIndexInSubExpressionRange(expressionsBarriers, i);

                if (checkIndexInSubExpressionRangeResult != 0) {
                    char[] subExpressionChars = new char[checkIndexInSubExpressionRangeResult - i + 1];
                    System.arraycopy(expressionChars, i, subExpressionChars, 0, subExpressionChars.length);
                    operands.set(operands.size() - 1, operands.get(operands.size() - 1) + new String(subExpressionChars));
                    i = checkIndexInSubExpressionRangeResult;
                    continue;
                }

                operands.set(operands.size() - 1, operands.get(operands.size() - 1) + Character.toString(expressionChars[i]));

            }

            try {
                if (operands.size() - 1 != operations.size())
                    throw new IllegalArgumentException("invalid expression string");
            } catch (IllegalArgumentException e) {
                System.err.println(operations);
                System.err.println(operands);
                throw e;
            }

            LinkedList<Operation> operationsEnumObjects = new LinkedList<Operation>() {
                {
                    operations.forEach(operation -> add(Operation.getBySymbol(operation.charAt(0))));
                }
            };

            LinkedList operandsNumberAndExpressionObjects = new LinkedList() {
                {
                    operands.forEach(operand -> {
                        try {
                            if (operand.startsWith("(") && operand.endsWith(")")) {
                                add(Double.valueOf(operand.substring(1, operand.length() - 1)));
                            } else {
                                add(Double.valueOf(operand));
                            }
                        } catch (NumberFormatException e) {
                            add(createExpression(operand));
                        }
                    });
                }
            };

            return new Expression(operationsEnumObjects, operandsNumberAndExpressionObjects);
        }
    }

    private static int checkIndexInSubExpressionRange(LinkedList<Integer> expressionsBarriers, Integer index) {
        for (int i = 0; i < expressionsBarriers.size(); i += 2) {
            if (index >= expressionsBarriers.get(i) && index <= expressionsBarriers.get(i + 1)) {
                return expressionsBarriers.get(i + 1);
            }
        }
        return 0;
    }

    private static boolean checkParentheses(String expressionString) {
        int parentheses = 0;
        char[] expressionChars = expressionString.toCharArray();
        for (char expressionChar : expressionChars) {
            if (expressionChar == '(') parentheses++;
            if (expressionChar == ')') parentheses--;
            if (parentheses < 0) return false;
        }
        return parentheses == 0;
    }

    private Expression(LinkedList<Operation> operations, LinkedList operands) {
        validate(operations, operands);
        expressionString = createExpressionString(operations, operands);
        HashMap<Integer, Boolean> operationsPrioritets = new HashMap<>();
        operations.forEach((operation) -> operationsPrioritets.put(operation.getPrioritet(), true));
        result = calcResult(operationsPrioritets, operations, operands);
    }

    private Number calcResult(HashMap<Integer, Boolean> operationsPrioritets, LinkedList<Operation> operations, LinkedList operands) {
        operationsPrioritets.forEach((prioritet, flag) -> {
            // while operations by priopitets exists
            while (operationsPrioritets.get(prioritet)) {
                operationsPrioritets.put(prioritet, calcEpisode(prioritet, operations, operands));
            }
        });
        return (Number) operands.get(0);
    }

    private boolean calcEpisode(int prioritet, LinkedList<Operation> operations, LinkedList operands) {
        // execute all operations in expression by prioritet
        for (int i = 0; i < operations.size(); i++) {
            if (operations.get(i).getPrioritet() == prioritet) {
                simpleOperation(i, operations, operands);
                return true;
            }
        }
        return false;
    }

    private void simpleOperation(int operationIndex, LinkedList<Operation> operations, LinkedList operands) {

        Number operand1 = operands.get(operationIndex) instanceof Number
                ? (Number) operands.get(operationIndex)
                : ((Expression) operands.get(operationIndex)).getResult();

        Number operand2 = operands.get(operationIndex + 1) instanceof Number
                ? (Number) operands.get(operationIndex + 1)
                : ((Expression) operands.get(operationIndex + 1)).getResult();

        switch (operations.get(operationIndex)) {
            case plus:
                operands.set(operationIndex, operand1.doubleValue() + operand2.doubleValue());
                break;
            case minus:
                operands.set(operationIndex, operand1.doubleValue() - operand2.doubleValue());
                break;
            case multiplication:
                operands.set(operationIndex, operand1.doubleValue() * operand2.doubleValue());
                break;
            case division:
                operands.set(operationIndex, operand1.doubleValue() / operand2.doubleValue());
                break;
        }

        operands.remove(operationIndex + 1);
        operations.remove(operationIndex);
    }

    private void validate(LinkedList<Operation> operations, LinkedList operands) {

        if (operands == null || operations == null)
            throw new IllegalArgumentException("operands/operations is null");

        if (operations.size() < 1)
            throw new IllegalArgumentException("wrong operations count");

        if (operands.size() - 1 != operations.size())
            throw new IllegalArgumentException("wrong operands/operations count: " + (operands.size() - 1) + " " + operations.size());

        operands.forEach(operand -> {
            if (operand == null) throw new IllegalArgumentException("null operand");
            if (!(operand instanceof Integer) && !(operand instanceof Number) && !(operand instanceof Expression))
                throw new IllegalArgumentException("wrong type of operand: " + operand);
        });
    }

    private String createExpressionString(LinkedList<Operation> operations, LinkedList operands) {
        // create string by operations and operands lists merging
        StringBuilder expressionBuilder = new StringBuilder();
        for (int i = 0; i < operands.size() - 1; i++) {
            expressionBuilder.append(
                    (
                            operands.get(i) instanceof Number
                                    ? operands.get(i)
                                    : "( " + operands.get(i) + " )"
                    ) +
                            " " +
                            operations.get(i) +
                            " "
            );
        }
        return expressionBuilder.append(
                operands.get(operands.size() - 1) instanceof Number
                        ? operands.get(operands.size() - 1)
                        : "( " + operands.get(operands.size() - 1) + " )"
        ).toString();
    }

    public String toString() {
        return expressionString;
    }

    public Number getResult() {
        return result;
    }

    private enum Operation {

        plus('+', 2),
        minus('-', 2),
        multiplication('*', 1),
        division('/', 1);

        public static Operation getBySymbol(char chr) {
            Operation operation = null;
            switch (chr) {
                case '+':
                    operation = plus;
                    break;
                case '-':
                    operation = minus;
                    break;
                case '*':
                    operation = multiplication;
                    break;
                case '/':
                    operation = division;
                    break;
            }
            return operation;
        }

        private int prioritet;

        private char symbol;

        Operation(char symbol, int prioritet) {
            this.prioritet = prioritet;
            this.symbol = symbol;
        }

        public String toString() {
            return Character.toString(symbol);
        }

        public int getPrioritet() {
            return prioritet;
        }
    }

}