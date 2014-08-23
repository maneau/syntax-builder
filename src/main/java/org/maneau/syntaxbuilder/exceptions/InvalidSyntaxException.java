package org.maneau.syntaxbuilder.exceptions;

import org.maneau.syntaxbuilder.SyntaxBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by maneau on 22/08/2014.
 */
public class InvalidSyntaxException extends Exception {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyntaxBuilder.class);

    public InvalidSyntaxException(String s) {
        super(s);
    }

}