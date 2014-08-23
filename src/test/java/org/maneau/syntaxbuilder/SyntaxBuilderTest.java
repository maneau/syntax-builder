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

    /**
     * Checking the syntax error : ( term1
     *
     * @throws Exception
     */
    @Test
    public void testCheckMissingEnd() throws Exception {
        thrown.expect(InvalidSyntaxException.class);
        thrown.expectMessage(ErrorCodes.invalidSyntaxMissingCloseParenthesis(1));
        SyntaxBuilder.init().begin().term("term1").soutPrint().check();
    }

    /**
     * Checking the syntax error : term1 )
     *
     * @throws Exception
     */
    @Test
    public void testCheckMissingBegin() throws Exception {
        thrown.expect(InvalidSyntaxException.class);
        thrown.expectMessage(ErrorCodes.INVALID_SYNTAX_CLOSING_NONE_OPENED_PARENTHESIS);
        SyntaxBuilder.init().term("term1").end().soutPrint().check();
    }

    /**
     * Checking the syntax error : (term1 AND OR term2)
     *
     * @throws Exception
     */
    @Test
    public void testCheckSuccessiveOperator() throws Exception {
        thrown.expect(InvalidSyntaxException.class);
        thrown.expectMessage(ErrorCodes.INVALID_SYNTAX_WAITING_FOR_A_TERM);
        SyntaxBuilder.init().begin().term("term1").and().or().term("term2").end().soutPrint().check();
    }

    @Test
    public void testAddTerm() throws Exception {
        String res = SyntaxBuilder.init().term("Test", "Value").soutPrint().check().toString();
        assertEquals(res, "Test:Value");
    }

    @Test
    public void testLog() throws Exception {
        SyntaxBuilder.init().term("Test", "Value").logDebug(LOGGER).check();
    }

    @Test
    public void testIncludeSyntaxBuilder() throws Exception {
        SyntaxBuilder.init().term("term1", "value1").and(
                SyntaxBuilder.init().term("term2", "value2")
        ).logInfo(LOGGER).check();
    }

    @Test
    public void testIncludeSyntaxBuilderWithParenthesis() throws Exception {
        SyntaxBuilder sb1 = SyntaxBuilder.init().term("term2", "value2").and().begin().exactTerm("term3", "value 3").end().check();
        SyntaxBuilder sb2 = SyntaxBuilder.init().begin()
                .term("term1", "value1").and(sb1)
                .end().logInfo(LOGGER).check();

        System.out.println("PRETTY>" + sb2.toPrettyString());
        System.out.println("RES   >" + sb2.toString());
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
                .begin().exactTerm("Test2", "this is an exact term").end()
                .end()
                .and("YOU")
                .soutPrint()
                .check().toString();
        System.out.println("RES>" + res);
    }

    @Test
    public void testPerf() throws Exception {
        final long iterations = 10000;
        long count = 0;
        long start = System.currentTimeMillis();
        long stringSize = 0;
        for (long i = 0; i < iterations; i++) {
            SyntaxBuilder sbPrincipal = SyntaxBuilder.init().term("term0");
            for (long j = 0; j < 100; j++) {
                SyntaxBuilder sb1 = SyntaxBuilder.init()
                        .begin().term("term" + j, String.valueOf(j)).end()
                        .and()
                        .begin().term("termWithoutValue").end()
                        .or()
                        .begin().exactTerm("term3", "this is an exact term").end()
                        .check();
                sbPrincipal.and(sb1);
            }
            sbPrincipal.check().toString();
            if (stringSize == 0) {
                stringSize = sbPrincipal.soutPrint().toString().length();
            }
            count += 100;
        }
        long stop = System.currentTimeMillis();
        long totalTime = (stop - start) / 1000;
        LOGGER.info("total time for " + count + " syntax created : " + totalTime + "s");
        LOGGER.info("Q/S : " + count / totalTime);
        LOGGER.info("Size of the syntax : " + stringSize);

    }

}