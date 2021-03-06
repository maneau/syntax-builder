package org.maneau.syntaxbuilder;

import org.maneau.syntaxbuilder.exceptions.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Created by maneau on 22/08/2014.
 */
public class SyntaxBuilder {
    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SyntaxBuilder.class);

    /**
     * The string containing the syntax
     */
    private StringBuilder sb;
    /**
     * The number of open parenthesis
     */
    private long nbOpenParenthesis = 0;
    /**
     * Boolean for indicating that we are waiting for a term
     */
    private boolean waiting4term = false;
    private static final String BEGIN = "(";
    private static final String END = ")";
    private static final String EQUAL = ":";
    private static final String EXACT = "\"";
    private static final String INDENT = "\t";
    private static final String NEW_LINE = "\n";
    private static final String NOT = "-";
    private static final String MUST = "+";

    private static final String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
    private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
    private static final String REPLACEMENT_STRING = "\\\\$0";

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

    private void logSyntaxError() {
        LOGGER.error("Invalid syntax detected for : [" + this.toString() + "]");
    }

    /*
     * Privates methods
     */

    /**
     * Private method for indent the text
     *
     * @return SyntaxBuilder
     */
    private SyntaxBuilder indent() {
        this.newline();
        for (long i = 0; i < nbOpenParenthesis; i++) {
            sb.append(INDENT);
        }
        return this;
    }

    private SyntaxBuilder indent(long nb) {
        this.newline();
        for (long i = 0; i < nb; i++) {
            sb.append(INDENT);
        }
        return this;
    }

    private SyntaxBuilder newline() {
        sb.append(NEW_LINE);
        return this;
    }

    private SyntaxBuilder append(String s) {
        sb.append(s);
        return this;
    }

    private SyntaxBuilder appendExact(String s) {
        sb.append(EXACT).append(s).append(EXACT);
        return this;
    }

    private SyntaxBuilder appendTerm(String term, String value) {
        sb.append(term).append(EQUAL).append(BEGIN).append(escapeChar(value)).append(END);
        return this;
    }

    private SyntaxBuilder append(SyntaxBuilder s) {
        sb.append(s.toString());
        return this;
    }

    private SyntaxBuilder op(Op operator) throws InvalidSyntaxException {
        this.indent();
        sb.append(operator);
        if (waiting4term) {
            logSyntaxError();
            throw new InvalidSyntaxException(ErrorCodes.INVALID_SYNTAX_WAITING_FOR_A_TERM);
        } else {
            waiting4term = true;
        }
        return this;
    }

    /**
     * Method to escape special char for lucene
     *
     * @param s containing special chars
     * @return string containing escaped chars
     */
    private String escapeChar(final String s) {
        return LUCENE_PATTERN.matcher(s).replaceAll(REPLACEMENT_STRING);
    }

    /*
     * Public methods
     */

    /**
     * Constructor
     */
    public SyntaxBuilder() {
        this.sb = new StringBuilder();
    }

    public static SyntaxBuilder builder() {
        return new SyntaxBuilder();
    }

    public SyntaxBuilder and() throws InvalidSyntaxException {
        return this.op(Op.AND);
    }

    public SyntaxBuilder and(String s) throws InvalidSyntaxException {
        return this.and().term(s);
    }

    /**
     * Operator AND with a list of strings
     *
     * @param term
     * @param values
     * @return SyntaxBuilder
     * @throws InvalidSyntaxException
     */
    public SyntaxBuilder and(String term, String... values) throws InvalidSyntaxException {
        for (String value : values) {
            this.and().term(term, value);
        }
        return this;
    }

    public SyntaxBuilder and(SyntaxBuilder syntax) throws InvalidSyntaxException {
        return this.and().begin().include(syntax).end();
    }

    /**
     * Operator OR with a list of strings
     *
     * @param term
     * @param values
     * @return SyntaxBuilder
     * @throws InvalidSyntaxException
     */
    public SyntaxBuilder or(String term, String... values) throws InvalidSyntaxException {
        for (String value : values) {
            this.or().term(term, value);
        }
        return this;
    }

    public SyntaxBuilder or() throws InvalidSyntaxException {
        return this.op(Op.OR);
    }

    public SyntaxBuilder begin() throws InvalidSyntaxException {
        return this.indent(nbOpenParenthesis++).append(BEGIN);
    }

    public SyntaxBuilder end() throws InvalidSyntaxException {
        nbOpenParenthesis--;
        this.indent().append(END);
        if (nbOpenParenthesis < 0) {
            //If the indentation is less than 0 that means we close an none opened parenthesis
            logSyntaxError();
            throw new InvalidSyntaxException(ErrorCodes.INVALID_SYNTAX_CLOSING_NONE_OPENED_PARENTHESIS);
        }
        return this;
    }

    public SyntaxBuilder include(SyntaxBuilder syntax) throws InvalidSyntaxException {
        waiting4term = false;
        return this.indent().append(syntax);
    }

    public boolean isValid() {
        return (nbOpenParenthesis == 0 && !waiting4term);
    }

    /**
     * Generate a term equal to value
     * </p>
     * Example of generation : term:value
     *
     * @param term
     * @param value
     * @return
     */
    public SyntaxBuilder term(String term, String value) throws InvalidSyntaxException {
        waiting4term = false;
        return this.indent().appendTerm(term, value);
    }

    /**
     * Generate a must on a term
     * </p>
     * Example of generation : +(term:value)
     *
     * @param term
     * @param value
     * @return
     */
    public SyntaxBuilder must(String term, String value) throws InvalidSyntaxException {
        waiting4term = false;
        return this.indent().append(MUST).append(BEGIN).appendTerm(term, value).append(END);
    }

    /**
     * Generate a not clause on a term
     * </p>
     * Example of generation : -(term:value)
     *
     * @param term
     * @param value
     * @return
     */
    public SyntaxBuilder not(String term, String value) throws InvalidSyntaxException {
        waiting4term = false;
        return this.indent().append(NOT).append(BEGIN).appendTerm(term, value).append(END);
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
    public SyntaxBuilder exactTerm(String term, String value) throws InvalidSyntaxException {
        waiting4term = false;
        return this.indent().append(term).append(EQUAL).append(BEGIN).appendExact(escapeChar(value)).append(END);
    }

    /**
     * Public method too check the validity of the syntax
     *
     * @return SyntaxBuilder
     * @throws InvalidSyntaxException if it's not valid
     */
    public SyntaxBuilder check() throws InvalidSyntaxException {
        if (nbOpenParenthesis > 0) {
            logSyntaxError();
            throw new InvalidSyntaxException(ErrorCodes.invalidSyntaxMissingCloseParenthesis(nbOpenParenthesis));
        } else if (nbOpenParenthesis < 0) {
            logSyntaxError();
            throw new InvalidSyntaxException(ErrorCodes.invalidSyntaxMissingOpenParenthesis(-nbOpenParenthesis));
        }
        if (waiting4term) {
            logSyntaxError();
            throw new InvalidSyntaxException(ErrorCodes.INVALID_SYNTAX_MISSING_TERM_AFTER_OPERATOR);
        }
        return this;
    }

    public String toString() {
        return sb.toString().replaceAll(INDENT, "").replaceAll(NEW_LINE, "");
    }

    /**
     * Method too get the pretty syntax
     *
     * @return
     */
    public String toPrettyString() {
        return sb.toString();
    }

    /**
     * Method too System.out.println the syntax
     *
     * @return
     */
    public SyntaxBuilder soutPrint() {
        System.out.println(sb.toString());
        return this;
    }

    /**
     * Method too add a term
     *
     * @param s the term too add
     * @return SyntaxBuilder
     */
    public SyntaxBuilder term(String s) {
        waiting4term = false;
        return this.indent().append(s);
    }

    /**
     * Method too debug the
     *
     * @param logger used too print
     * @return SyntaxBuilder
     */
    public SyntaxBuilder logDebug(Logger logger) {
        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
        return this;
    }

    /**
     * Method too debug the
     *
     * @param logger used too print
     * @return SyntaxBuilder
     */
    public SyntaxBuilder logWarn(Logger logger) {
        if (logger.isWarnEnabled()) {
            logger.warn(this.toString());
        }
        return this;
    }

    /**
     * Method to log in info
     *
     * @param logger used too print
     * @return SyntaxBuilder
     */
    public SyntaxBuilder logInfo(Logger logger) {
        if (logger.isInfoEnabled()) {
            logger.info(this.toString());
        }
        return this;
    }

    /**
     * Method to log in info
     *
     * @param logger used too print
     * @return SyntaxBuilder
     */
    public SyntaxBuilder logError(Logger logger) {
        logger.error(this.toString());
        return this;
    }

}
