package org.wickedsource.docxstamper.exceptions;

public class UnresolvedExpressionException extends DocxStamperException {

    public UnresolvedExpressionException(String expression, Throwable cause) {
        super(String.format("The following expression could not be processed by any comment processor: %s", expression), cause);
    }
}
