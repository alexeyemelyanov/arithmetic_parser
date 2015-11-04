package com.github.alexeyemelyanov.arithmetic_parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ExpressionTest {

    @Test
    public void parseTestReturnNotNull() {
        assertNotNull(Expression.parse("((-1+7)+(-1.9 *2)+((5.5 * 6.1)/+1 + (3 / 4 - 1)) * ( 1 / 1 ) / 1)"));
    }

    @Test
    public void parseTestReturnClassInstance() {
        assertEquals(
                Expression.parse("-1/2").getClass(),
                Expression.class
                );
    }

    @Test
    public void parseTestValidString0() {
        String string = "((-1+7)+(-1.9 *2)+((5.5 * 6.1)/+1 + (3 / 4 - 1)) * ( 1 / 1 ) / 1)";
        Expression expression = Expression.parse(string);
        assertEquals(string, expression.getResult().doubleValue(), 35.5, 0);
    }

    @Test
    public void parseTestValidString1() {
        String string = "-1+(+2)";
        Expression expression = Expression.parse(string);
        assertEquals(string, expression.getResult().doubleValue(), 1, 0);
    }

    @Test
    public void parseTestValidString2() {
        String string = "-1/2";
        Expression expression = Expression.parse(string);
        assertEquals(string, expression.getResult().doubleValue(), -0.5, 0);
    }

    @Test
    public void parseTestValidString3() {
        String string = "+1+-1";
        Expression expression = Expression.parse(string);
        assertEquals(string, expression.getResult().doubleValue(), 0, 0);
    }

    @Test
    public void parseTestValidString4() {
        String string = "+1--1";
        Expression expression = Expression.parse(string);
        assertEquals(string, expression.getResult().doubleValue(), 2, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestNullParam() {
        String string = null;
        Expression.parse(string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestEmptyStringParam() {
        String string = "";
        Expression.parse(string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestInvalidString0() {
        String string = "*((-1+7)+(-1.9 *2)+((5.5 * 6.1)/+1 + (3 / 4 - 1)) * ( 1 / 1 ) / 1)";
        Expression.parse(string);
    }
}
