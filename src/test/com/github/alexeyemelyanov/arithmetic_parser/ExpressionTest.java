package com.github.alexeyemelyanov.arithmetic_parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
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

    @Test(expected = IllegalArgumentException.class)
    public void parseTestNullParam() {
        Expression.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestEmptyStringParam() {
        String string = "";
        Expression.parse(string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestInvalidString0() {
        String string = "*((-1+7)+(-1.9 *2)+((5.5 * 6.1)|/+1 + (3 / 4 - 1)) * ( 1 // 1 ) / 1)";
        Expression.parse(string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestInvalidString1() {
        String string = "*()";
        Expression.parse(string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestInvalidString2() {
        String string = "()(-)";
        Expression.parse(string);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseTestInvalidString3() {
        String string = "(DFERFERFRF";
        Expression.parse(string);
    }
}
