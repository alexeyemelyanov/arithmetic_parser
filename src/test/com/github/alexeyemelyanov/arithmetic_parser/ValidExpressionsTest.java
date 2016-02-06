package com.github.alexeyemelyanov.arithmetic_parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class ValidExpressionsTest {

    @Parameters
    public static Iterable<Object[]> getValidStrings() {
        return asList(
                new Object[][]{
                        {"((-1+7)+(-1.9 *2)+((5.5 * 6.1)/+1 + (3 / 4 - 1)) * ( 1 / 1 ) / 1)", 35.5},
                        {"-1/2",                                                              -0.5},
                        {"+1+-1",                                                               0D},
                        {"+1--1",                                                               2D},
                        {"-1+(+2)",                                                             1D}
                }
        );
    }

    @Parameter(0)
    public String expressionString;

    @Parameter(1)
    public Double result;

    @Test
    public void test() {
        assertThat(Expression.parse(expressionString).getResult(), is(result));
    }
}
