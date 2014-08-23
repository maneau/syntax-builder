package org.maneau.syntaxbuilder;

import org.junit.Ignore;
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
        assertTrue(SyntaxBuilder.builder().begin().term("Test").end().soutPrint().isValid());
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
        SyntaxBuilder.builder().begin().term("term1").soutPrint().check();
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
        SyntaxBuilder.builder().term("term1").end().soutPrint().check();
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
        SyntaxBuilder.builder().begin().term("term1").and().or().term("term2").end().soutPrint().check();
    }

    @Test
    public void testAddTerm() throws Exception {
        String res = SyntaxBuilder.builder().term("Test", "Value and data").soutPrint().check().toString();
        assertEquals(res, "Test:(Value and data)");
    }

    @Test
    public void testLog() throws Exception {
        SyntaxBuilder.builder().term("Test", "Value").logDebug(LOGGER).check();
    }

    @Test
    public void testIncludeSyntaxBuilder() throws Exception {
        SyntaxBuilder sb = SyntaxBuilder.builder().term("term1", "value1").and(
                SyntaxBuilder.builder().term("term2", "value2")
        ).logInfo(LOGGER).check();
        assertEquals("term1:(value1) AND (term2:(value2))", sb.toString());
    }

    /**
     * Testing escaped chars
     *
     * @throws Exception
     */
    @Test
    public void testEscapedCars() throws Exception {
        assertEquals("term1:(value1)", SyntaxBuilder.builder().term("term1", "value1").check().soutPrint().toString());
        assertEquals("term1:(value\\+)", SyntaxBuilder.builder().term("term1", "value+").check().soutPrint().toString());
        assertEquals("term1:(value \\{)", SyntaxBuilder.builder().term("term1", "value {").check().soutPrint().toString());
        assertEquals("term1:(value \\(\\)\\{\\}\\-\\!\\+\\?)", SyntaxBuilder.builder().term("term1", "value (){}-!+?").check().soutPrint().toString());
        assertEquals("term1:(\"value \\(\\)\\{\\}\\-\\!\\+\\?\")", SyntaxBuilder.builder().exactTerm("term1", "value (){}-!+?").check().soutPrint().toString());
    }

    /**
     * Testing must and not operations
     *
     * @throws Exception
     */
    @Test
    public void testMustAndNot() throws Exception {
        assertEquals("+(term1:(value1))", SyntaxBuilder.builder().must("term1", "value1").check().soutPrint().toString());
        assertEquals("-(term1:(value1))", SyntaxBuilder.builder().not("term1", "value1").check().soutPrint().toString());
    }

    /**
     * Testing must and not operations
     *
     * @throws Exception
     */
    @Test
    public void testAndMultiTerms() throws Exception {
        SyntaxBuilder sb = new SyntaxBuilder().term("term0", "value0")
                .and("term1", "value1", "value2", "value3")
                .check().soutPrint();

        assertEquals("term0:(value0) AND term1:(value1) AND term1:(value2) AND term1:(value3)", sb.toString());
    }

    @Test
    public void testIncludeSyntaxBuilderWithParenthesis() throws Exception {
        SyntaxBuilder sb1 = SyntaxBuilder.builder().term("term2", "value2").and().begin()
                .exactTerm("term3", "value 3").end().check().soutPrint();
        SyntaxBuilder sb2 = SyntaxBuilder.builder().begin()
                .term("term1", "value1").and(sb1)
                .end().logInfo(LOGGER).check().soutPrint();

        assertEquals("(term1:(value1) AND (term2:(value2) AND (term3:(\"value 3\"))))",sb2.toString());
    }

    @Test
    public void testPrettyPrint() throws Exception {
        SyntaxBuilder sb = SyntaxBuilder.builder()
                .begin()
                .begin().term("Test", "Value").end()
                .and()
                .begin().term("Test2").end()
                .or()
                .begin().exactTerm("Test2", "this is an exact term").end()
                .end()
                .and("YOU")
                .soutPrint()
                .check();
        String syntax = sb.toString();
        String prettySyntax = sb.toPrettyString();

        System.out.println("RES>" + syntax);
        assertEquals("((Test:(Value)) AND (Test2) OR (Test2:(\"this is an exact term\"))) AND YOU", syntax);
        assertEquals("\n(\n" +
                "\t(\n" +
                "\t\tTest:(Value)\n" +
                "\t)\n" +
                "\t AND \n" +
                "\t(\n" +
                "\t\tTest2\n" +
                "\t)\n" +
                "\t OR \n" +
                "\t(\n" +
                "\t\tTest2:(\"this is an exact term\")\n" +
                "\t)\n" +
                ")\n" +
                " AND \n" +
                "YOU", prettySyntax);
    }

    @Test
    @Ignore
    public void testPerf() throws Exception {
        final long iterations = 10000;
        long count = 0;
        long start = System.currentTimeMillis();
        long stringSize = 0;
        for (long i = 0; i < iterations; i++) {
            SyntaxBuilder sbPrincipal = SyntaxBuilder.builder().term("term0");
            for (long j = 0; j < 100; j++) {
                SyntaxBuilder sb1 = SyntaxBuilder.builder()
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