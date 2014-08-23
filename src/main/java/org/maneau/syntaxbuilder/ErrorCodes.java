package org.maneau.syntaxbuilder;

/**
 * Created by maneau on 22/08/2014.
 */
public class ErrorCodes {
    public static final String INVALID_SYNTAX_MISSING_CLOSE_PARENTHESIS = "Missing %s close parenthesis. (Ex : '(( term )' is invalid)";
    public static final String INVALID_SYNTAX_MISSING_OPEN_PARENTHESIS = "Missing %s open parenthesis. (Ex : '( term ))' is invalid)";
    public static final String INVALID_SYNTAX_CLOSING_NONE_OPENED_PARENTHESIS = "Closing parenthesis but none have been open. (Ex : '( term ))' is invalid)";
    public static final String INVALID_SYNTAX_MISSING_TERM_AFTER_OPERATOR = "Missing term after operator.";
    public static final String INVALID_SYNTAX_WAITING_FOR_A_TERM = "An operator is already set, waiting for a term. (Ex : 'AND OR' is invalid)";

    public static String invalidSyntaxMissingCloseParenthesis(long nb) {
        return String.format(INVALID_SYNTAX_MISSING_CLOSE_PARENTHESIS, nb);
    }

    public static String invalidSyntaxMissingOpenParenthesis(long nb) {
        return String.format(INVALID_SYNTAX_MISSING_OPEN_PARENTHESIS, nb);
    }
}
