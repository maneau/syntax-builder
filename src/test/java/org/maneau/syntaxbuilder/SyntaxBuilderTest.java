package org.maneau.syntaxbuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.maneau.syntaxbuilder.exceptions.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SyntaxBuilderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyntaxBuilderTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSimpleSyntax() throws Exception {
        assertTrue(SyntaxBuilder.init().begin().term("Test").end().soutPrint().isValid());
    }

    @Test
    public void testCheckMissingEnd() throws Exception {
        thrown.expect(InvalidSyntaxException.class);
        thrown.expectMessage(ErrorCodes.invalidSyntaxMissingCloseParenthesis(1));
        SyntaxBuilder.init().begin().term("Test").soutPrint().check();
    }

    @Test
    public void testCheckMissingBegin() throws Exception {
        thrown.expect(InvalidSyntaxException.class);
        thrown.expectMessage(ErrorCodes.INVALID_SYNTAX_CLOSING_NONE_OPENED_PARENTHESIS);
        SyntaxBuilder.init().term("Test").end().soutPrint().check();
    }

    @Test
    public void testAddTerm() throws Exception {
        String res = SyntaxBuilder.init().term("Test", "Value").soutPrint().check().toString();
        assertEquals(res, "Test:Value");
    }

    @Test
    public void testLog() throws Exception {
        SyntaxBuilder.init().term("Test", "Value").debug(LOGGER).check();
    }

    @Test
    public void testPrettyPrint() throws Exception {
        System.out.println("--------------------");
        System.out.println("testPrettyPrint");
        String res = SyntaxBuilder.init()
                .begin()
                .begin().term("Test", "Value").end()
                .and()
                .begin().term("Test2").end()
                .or()
                .begin().exactTerm("Test2","this is an exact term").end()
                .end()
                .and("YOU")
                .soutPrint()
                .check().toString();
        System.out.println("RES>"+res);
    }

}