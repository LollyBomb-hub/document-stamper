package org.wickedsource.docxstamper.resolver.context.language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.wickedsource.docxstamper.resolver.context.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.util.ExpressionUtil;

public class ExpressionResolver {

    private static final ExpressionUtil expressionUtil = new ExpressionUtil();
    private final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);
    private final EvaluationContextConfigurer<?> evaluationContextConfigurer;

    public ExpressionResolver() {
        this.evaluationContextConfigurer = new NoOpEvaluationContextConfigurer<>();
    }

    public ExpressionResolver(EvaluationContextConfigurer<?> evaluationContextConfigurer) {
        this.evaluationContextConfigurer = evaluationContextConfigurer;
    }

    /**
     * Runs the given expression against the given context object and returns the result of the evaluated expression.
     *
     * @param expressionString the expression to evaluate.
     * @param contextRoot      the context object against which the expression is evaluated.
     * @return the result of the evaluated expression.
     */
    public Object resolveExpression(String expressionString, Object contextRoot) {

        if ((expressionString.startsWith("${") || expressionString.startsWith("#{")) && expressionString.endsWith("}")) {
            expressionString = expressionUtil.stripExpression(expressionString);
        }

        if (expressionString.contains("“")) {
            expressionString = expressionString.replace("“", "\"");
        }

        if (expressionString.contains("”")) {
            expressionString = expressionString.replace("”", "\"");
        }

        if (expressionString.contains("‘")) {
            expressionString = expressionString.replace("‘", "'");
        }

        if (expressionString.contains("’")) {
            expressionString = expressionString.replace("’", "'");
        }

        logger.debug("Processed input string: {}", expressionString);

        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext evaluationContext = evaluationContextConfigurer.getCurrentEvaluationContext() == null ? SimpleEvaluationContext
                .forReadOnlyDataBinding()
                .withRootObject(contextRoot)
                .withInstanceMethods()
                .build() :
                evaluationContextConfigurer.getCurrentEvaluationContext();

        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext, contextRoot);
    }

    public Object resolveConditionalExpression(String expressionString, Object contextRoot) {

        if (expressionString.startsWith("$if{") && expressionString.endsWith("}")) {
            expressionString = expressionUtil.stripConditionalExpression(expressionString);
        }

        if (expressionString.contains("“")) {
            expressionString = expressionString.replace("“", "\"");
        }

        if (expressionString.contains("”")) {
            expressionString = expressionString.replace("”", "\"");
        }

        logger.debug("Processed input string: {}", expressionString);

        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext evaluationContext = evaluationContextConfigurer.getCurrentEvaluationContext() == null ? SimpleEvaluationContext
                .forReadOnlyDataBinding()
                .withRootObject(contextRoot)
                .withInstanceMethods()
                .build() :
                evaluationContextConfigurer.getCurrentEvaluationContext();

        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext, contextRoot);
    }

}
