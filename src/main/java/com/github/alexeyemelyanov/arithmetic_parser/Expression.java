package com.github.alexeyemelyanov.arithmetic_parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Expression {

  private final String expressionString;

  private final Number result;

  private Expression(List<Operation> operations, List<Object> operands) {
    validate(operations, operands);
    expressionString = createExpressionString(operations, operands);
    HashMap<Integer, Boolean> operationsPriorities = new HashMap<>();
    operations.forEach((operation) -> operationsPriorities.put(operation.getPriority(), true));
    result = calcResult(operationsPriorities, operations, operands);
  }

  private static Expression createExpression(String expressionString) {

    char[] expressionChars = expressionString.toCharArray();
    List<Integer> expressionsBarriers = new ArrayList<>();
    int parentheses = 0;

    for (int i = 0; i < expressionChars.length; i++) {

      if (expressionChars[i] == '(') {
        parentheses++;
        if (parentheses == 1) {
          expressionsBarriers.add(i);
        }
      }

      if (expressionChars[i] == ')') {
        parentheses--;
        if (parentheses == 0) {
          expressionsBarriers.add(i);
        }
      }
    }

    if (expressionsBarriers.size() == 2
        && expressionsBarriers.get(0) == 0
        && expressionsBarriers.get(1) == expressionChars.length - 1) {

      expressionString = expressionString.substring(1, expressionChars.length - 1);
      return createExpression(expressionString);

    } else {

      List<String> operands = new ArrayList<>();
      List<String> operations = new ArrayList<>();
      int checkIndexInSubExpressionRangeResult;

      for (int i = 0; i < expressionChars.length; i++) {

        if (Operation.getBySymbol(expressionChars[i]) != null) {

          if (expressionChars[i] == '-' || expressionChars[i] == '+') {
            if (i == 0) {
              if (Operation.getBySymbol(expressionChars[i + 1]) == null) {
                operands.add(Character.toString(expressionChars[i]));
              }
            } else if (Operation.getBySymbol(expressionChars[i - 1]) != null
                && Operation.getBySymbol(expressionChars[i + 1]) == null) {
              operands.set(operands.size() - 1, Character.toString(expressionChars[i]));
            } else {
              operations.add(Character.toString(expressionChars[i]));
              operands.add("");
            }
            continue;
          }

          try {

            if (i == 0) {
              throw new IllegalArgumentException("incorrect first expression symbol: " + expressionChars[i]);
            }

            if (Operation.getBySymbol(expressionChars[i - 1]) != null) {
              throw new IllegalArgumentException(
                  "invalid operation: " + new String(new char[]{expressionChars[i - 1], expressionChars[i]})
              );
            }

            if (i == expressionChars.length - 1) {
              throw new IllegalArgumentException("incorrect last expression symbol: " + expressionChars[i]);
            }

          } catch (IllegalArgumentException e) {
            System.err.println(operations);
            System.err.println(operands);
            throw e;
          }

          operations.add(Character.toString(expressionChars[i]));
          operands.add("");
          continue;
        }

        if (operands.isEmpty()) {
          operands.add("");
        }

        checkIndexInSubExpressionRangeResult = checkIndexInSubExpressionRange(expressionsBarriers, i);

        if (checkIndexInSubExpressionRangeResult != 0) {
          char[] subExpressionChars = new char[checkIndexInSubExpressionRangeResult - i + 1];
          System.arraycopy(expressionChars, i, subExpressionChars, 0, subExpressionChars.length);
          operands.set(operands.size() - 1, operands.getLast() + new String(subExpressionChars));
          i = checkIndexInSubExpressionRangeResult;
          continue;
        }

        operands.set(operands.size() - 1, operands.getLast() + expressionChars[i]);
      }

      try {
        if (operands.size() - 1 != operations.size() || operations.isEmpty()) {
          throw new IllegalArgumentException("invalid expression string");
        }
      } catch (IllegalArgumentException e) {
        System.err.println(operations);
        System.err.println(operands);
        throw e;
      }

      List<Operation> operationsEnumObjects = new ArrayList<>();
      for (String operation : operations) {
        operationsEnumObjects.add(Operation.getBySymbol(operation.charAt(0)));
      }

      List<Object> operandsNumberAndExpressionObjects = new ArrayList<>();

      for (String operand : operands) {
        try {
          if (operand.startsWith("(") && operand.endsWith(")")) {
            operandsNumberAndExpressionObjects.add(Double.valueOf(operand.substring(1, operand.length() - 1)));
          } else {
            operandsNumberAndExpressionObjects.add(Double.valueOf(operand));
          }
        } catch (NumberFormatException e) {
          validateExpressionOperand(operand);
          operandsNumberAndExpressionObjects.add(createExpression(operand));
        }
      }

      return new Expression(operationsEnumObjects, operandsNumberAndExpressionObjects);
    }
  }

  private Number calcResult(
      HashMap<Integer, Boolean> operationsPriorities,
      List<Operation> operations,
      List<Object> operands
  ) {
    for (Map.Entry<Integer, Boolean> entry : operationsPriorities.entrySet()) {
      Integer priority = entry.getKey();
      // while operations by priorities exists
      while (operationsPriorities.get(priority)) {
        operationsPriorities.put(priority, calcEpisode(priority, operations, operands));
      }
    }
    return (Number) operands.getFirst();
  }

  private boolean calcEpisode(int priority, List<Operation> operations, List<Object> operands) {
    // execute all operations in expression by priority
    for (int i = 0; i < operations.size(); i++) {
      if (operations.get(i).getPriority() == priority) {
        simpleOperation(i, operations, operands);
        return true;
      }
    }
    return false;
  }

  private void simpleOperation(int operationIndex, List<Operation> operations, List<Object> operands) {

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

  private void validate(List<Operation> operations, List<?> operands) {

    if (operands == null || operations == null) {
      throw new IllegalArgumentException("operands/operations is null");
    }

    if (operations.isEmpty()) {
      throw new IllegalArgumentException("wrong operations count");
    }

    if (operands.size() - 1 != operations.size()) {
      throw new IllegalArgumentException(
          "wrong operands/operations count: " + (operands.size() - 1) + " " + operations.size()
      );
    }

    for (Object operand : operands) {
      if (operand == null) {
        throw new IllegalArgumentException("null operand");
      }
      if (!(operand instanceof Number) && !(operand instanceof Expression)) {
        throw new IllegalArgumentException("wrong type of operand: " + operand);
      }
      validateExpressionOperand(operand.toString());
    }
  }

  public String toString() {
    return expressionString;
  }

  public Number getResult() {
    return result;
  }

  public static Expression parse(String expressionString) {

    if (expressionString == null) {
      throw new IllegalArgumentException("must be a string, null given");
    }

    expressionString = expressionString.replace(" ", "");
    if (expressionString.isEmpty()) {
      throw new IllegalArgumentException("empty string");
    }

    if (!checkParentheses(expressionString)) {
      throw new IllegalArgumentException("invalid string");
    }

    return createExpression(expressionString);
  }

  private String createExpressionString(List<Operation> operations, List<?> operands) {
    // create string by operations and operands lists merging
    StringBuilder expressionBuilder = new StringBuilder();
    for (int i = 0; i < operands.size() - 1; i++) {
      expressionBuilder
          .append(
              operands.get(i) instanceof Number
                  ? operands.get(i)
                  : "( " + operands.get(i) + " )"
          )
          .append(" ")
          .append(operations.get(i))
          .append(" ");
    }

    return expressionBuilder
        .append(
            operands.getLast() instanceof Number
                ? operands.getLast()
                : "( " + operands.getLast() + " )"
        )
        .toString();
  }

  private static int checkIndexInSubExpressionRange(List<Integer> expressionsBarriers, Integer index) {
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
      if (expressionChar == '(') {
        parentheses++;
      }
      if (expressionChar == ')') {
        parentheses--;
      }
      if (parentheses < 0) {
        return false;
      }
    }
    return parentheses == 0;
  }

  private static void validateExpressionOperand(String operand) {
    char first = operand.charAt(0);
    char last = operand.charAt(operand.length() - 1);
    if (
        (!Character.isDigit(first) && first != '-' && first != '+' && first != '(')
            || (!Character.isDigit(last) && last != ')')
    ) {
      throw new IllegalArgumentException("wrong operand: " + operand);
    }
  }

  private enum Operation {

    plus('+', 2),
    minus('-', 2),
    multiplication('*', 1),
    division('/', 1);

    public static Operation getBySymbol(char chr) {
      return switch (chr) {
        case '+' -> plus;
        case '-' -> minus;
        case '*' -> multiplication;
        case '/' -> division;
        default -> null;
      };
    }

    private final int priority;

    private final char symbol;

    Operation(char symbol, int priority) {
      this.priority = priority;
      this.symbol = symbol;
    }

    public String toString() {
      return "" + symbol;
    }

    public int getPriority() {
      return priority;
    }
  }
}
