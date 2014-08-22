package org.maneau.syntaxbuilder;

import org.maneau.syntaxbuilder.exceptions.InvalidSyntaxException;
import org.slf4j.Logger;

/**
 * Created by maneau on 22/08/2014.
 */
public class SyntaxBuilder {
    private StringBuilder sb;
    private long nbBeginEnd = 0;
    private boolean waiting4term = false;
    private static final String BEGIN = "(";
    private static final String END = ")";
    private static final String EQUAL = ":";
    private static final String EXACT = "\"";
    private static final String SPACE = "\t";
    private static final String NEWLINE = "\n";

    protected StringBuilder getStringBuilder() {
        return sb;
    }

    /**
     * Enum with operator
     */
    private static enum Op {
        AND(" AND "), OR(" OR ");
        String op;

        private Op(String s) {
            this.op = s;
        }

        public String toString() {
            return op;
        }
    }

    /*
     * Privates methods
     */
    private SyntaxBuilder() {
        this.sb = new StringBuilder();
    }


    private void minus() throws InvalidSyntaxException {
        if (--nbBeginEnd < 0) {
            throw new InvalidSyntaxException(ErrorCodes.INVALID_SYNTAX_CLOSING_NONE_OPENED_PARENTHESIS);
        }
    }

    private SyntaxBuilder space() {
        this.newline();
        for (long i = 0; i < nbBeginEnd; i++) {
            sb.append(SPACE);
        }
        return this;
    }

    private SyntaxBuilder space(long nb) {
        this.newline();
        for (long i = 0; i < nb; i++) {
            sb.append(SPACE);
        }
        return this;
    }

    private SyntaxBuilder newline() {
        sb.append(NEWLINE);
        return this;
    }

    private SyntaxBuilder append(String s) {
        sb.append(s);
        return this;
    }

    private SyntaxBuilder append(SyntaxBuilder s) {
        sb.append(s);
        return this;
    }

    private SyntaxBuilder op(Op operator) {
        this.space();
        waiting4term = true;
        sb.append(operator);
        return this;
    }

    /*
     * Public methods
     */

    public static SyntaxBuilder init() {
        return new SyntaxBuilder();
    }

    public SyntaxBuilder and() {
        return this.op(Op.AND);
    }

    public SyntaxBuilder and(String s) {
        return this.and().term(s);
    }

    public SyntaxBuilder and(SyntaxBuilder syntax) throws InvalidSyntaxException {
        return this.space().and().begin().include(syntax).end();
    }

    public SyntaxBuilder or() {
        return this.op(Op.OR);
    }

    public SyntaxBuilder begin() {
        return this.space(nbBeginEnd++).append(BEGIN);
    }

    public SyntaxBuilder end() throws InvalidSyntaxException {
        minus();
        return this.space().append(END);
    }

    public SyntaxBuilder include(SyntaxBuilder syntax) {
        waiting4term = false;
        return this.space().append(syntax);
    }

    public boolean isValid() {
        return (nbBeginEnd == 0 && !waiting4term);
    }

    /**
     * Generate a term equal to value
     * </p>
     * Example of generation : term:"value"
     *
     * @param term
     * @param value
     * @return
     */
    public SyntaxBuilder term(String term, String value) {
        waiting4term = false;
        return this.space().append(term).append(EQUAL).append(value);
    }

    /**
     * Generate a term equal to exact value
     * </p>
     * Example of generation : term:"value"
     *
     * @param term
     * @param value
     * @return
     */
    public SyntaxBuilder exactTerm(String term, String value) {
        waiting4term = false;
        return this.space().append(term).append(EQUAL).append(EXACT).append(value).append(EXACT);
    }

    public SyntaxBuilder check() throws InvalidSyntaxException {
        if (nbBeginEnd > 0) {
            throw new InvalidSyntaxException(ErrorCodes.invalidSyntaxMissingCloseParenthesis(nbBeginEnd));
        } else if (nbBeginEnd < 0) {
            throw new InvalidSyntaxException(ErrorCodes.invalidSyntaxMissingOpenParenthesis(-nbBeginEnd));
        }
        if (waiting4term) {
            throw new InvalidSyntaxException(ErrorCodes.INVALID_SYNTAX_MISSING_TERM_AFTER_OPERATOR);
        }
        return this;
    }

    public String toString() {
        return sb.toString().replaceAll(SPACE, "").replaceAll(NEWLINE, "");
    }

    public String toPrettyString() {
        return sb.toString();
    }

    public SyntaxBuilder soutPrint() {
        System.out.println(sb.toString());
        return this;
    }

    public SyntaxBuilder term(String s) {
        waiting4term = false;
        this.space();
        sb.append(s);
        return this;
    }

    public SyntaxBuilder debug(Logger logger) {
        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
        return this;
    }



}
