package org.wickedsource.docxstamper.resolver.context;

import org.springframework.expression.EvaluationContext;

/**
 * Allows for custom configuration of a spring expression language {@link org.springframework.expression.EvaluationContext}.
 * This can  for example be used to add custom {@link org.springframework.expression.PropertyAccessor}s and {@link org.springframework.expression.MethodResolver}s.
 */
public interface EvaluationContextConfigurer <T extends EvaluationContext> {
    T getCurrentEvaluationContext();

}
