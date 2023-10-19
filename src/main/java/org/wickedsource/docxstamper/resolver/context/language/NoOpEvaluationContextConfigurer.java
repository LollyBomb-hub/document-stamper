package org.wickedsource.docxstamper.resolver.context.language;

import org.springframework.expression.EvaluationContext;
import org.wickedsource.docxstamper.resolver.context.EvaluationContextConfigurer;

/**
 * {@link EvaluationContextConfigurer} that does no customization.
 */
public class NoOpEvaluationContextConfigurer<T extends EvaluationContext> implements EvaluationContextConfigurer<T> {

    @Override
    public T getCurrentEvaluationContext() {
        return null;
    }

}
