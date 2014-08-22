package org.maneau.syntaxbuilder;

/**
 * Created by maneau on 22/08/2014.
 */
public class ErrorCodes {
    public static final String INVALID_SYNTAX_MISSING_CLOSE_PARENTHESIS = "Missing %s close parenthesis.";
    public static final String INVALID_SYNTAX_MISSING_OPEN_PARENTHESIS = "Missing %s open parenthesis.";
    public static final String INVALID_SYNTAX_CLOSING_NONE_OPENED_PARENTHESIS = "Closing parenthesis but none have been open.";
    public static final String INVALID_SYNTAX_MISSING_TERM_AFTER_OPERATOR = "Missing term after operator.";

    public static String invalidSyntaxMissingCloseParenthesis(long nb) {
        return String.format(INVALID_SYNTAX_MISSING_CLOSE_PARENTHESIS, nb);
    }

    public static String invalidSyntaxMissingOpenParenthesis(long nb) {
        return String.format(INVALID_SYNTAX_MISSING_OPEN_PARENTHESIS, nb);
    }
}
