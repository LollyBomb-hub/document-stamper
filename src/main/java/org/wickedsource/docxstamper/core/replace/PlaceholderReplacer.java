package org.wickedsource.docxstamper.core.replace;

import com.sun.istack.NotNull;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.wickedsource.docxstamper.core.ParagraphWrapper;
import org.wickedsource.docxstamper.core.walk.CoordinatesWalker;
import org.wickedsource.docxstamper.core.walk.DocumentWalkerImplementation;
import org.wickedsource.docxstamper.core.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.exceptions.DocxStamperException;
import org.wickedsource.docxstamper.exceptions.ProxyException;
import org.wickedsource.docxstamper.resolver.context.language.ExpressionResolver;
import org.wickedsource.docxstamper.resolver.context.proxy.ProxyBuilder;
import org.wickedsource.docxstamper.resolver.registry.TypeResolverRegistry;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.AbstractTypeResolver;
import org.wickedsource.docxstamper.util.ExpressionUtil;
import org.wickedsource.docxstamper.util.RunUtil;

import java.util.List;

@SuppressWarnings("unused")
public class PlaceholderReplacer<T> {

    private final Logger logger = LoggerFactory.getLogger(PlaceholderReplacer.class);

    private final ExpressionUtil expressionUtil = new ExpressionUtil();
    private final TypeResolverRegistry typeResolverRegistry;
    private ExpressionResolver expressionResolver = new ExpressionResolver();
    private String lineBreakPlaceholder;

    private boolean leaveEmptyOnExpressionError = false;

    private boolean failOnUnResolvedExpression = false;

    private boolean replaceNullValues = false;

    public PlaceholderReplacer(TypeResolverRegistry typeResolverRegistry) {
        this.typeResolverRegistry = typeResolverRegistry;
    }

    public PlaceholderReplacer(TypeResolverRegistry typeResolverRegistry, String lineBreakPlaceholder) {
        this.typeResolverRegistry = typeResolverRegistry;
        this.lineBreakPlaceholder = lineBreakPlaceholder;
    }

    public boolean isLeaveEmptyOnExpressionError() {
        return leaveEmptyOnExpressionError;
    }

    public void setLeaveEmptyOnExpressionError(boolean leaveEmptyOnExpressionError) {
        this.leaveEmptyOnExpressionError = leaveEmptyOnExpressionError;
    }

    public void setReplaceNullValues(boolean replaceNullValues) {
        this.replaceNullValues = replaceNullValues;
    }

    public void setExpressionResolver(ExpressionResolver expressionResolver) {
        this.expressionResolver = expressionResolver;
    }

    /**
     * Finds expressions in a document and resolves them against the specified context object. The expressions in the
     * document are then replaced by the resolved values.
     *
     * @param document     the document in which to replace all expressions.
     * @param proxyBuilder builder for a proxy around the context root to customize its interface
     */
    public void resolveExpressions(final WordprocessingMLPackage document, ProxyBuilder<T> proxyBuilder) {
        try {
            final T expressionContext = proxyBuilder.build();
            CoordinatesWalker walker = new DocumentWalkerImplementation<>(document, expressionUtil, expressionResolver, expressionContext) {
                @Override
                protected void onParagraph(ParagraphCoordinates paragraphCoordinates) {
                    resolveExpressionsForParagraph(paragraphCoordinates, expressionContext, document);
                }
            };
            walker.walk();
        } catch (
                ProxyException e) {
            throw new DocxStamperException("could not create proxy around context root!", e);
        }

    }

    public void resolveExpressionsForParagraph(ParagraphCoordinates paragraphCoordinates, T expressionContext, WordprocessingMLPackage document) {

        ParagraphWrapper paragraphWrapper = new ParagraphWrapper(paragraphCoordinates.getParagraph());
        List<String> placeholders = expressionUtil.findVariableExpressions(paragraphWrapper.getText());

        for (String placeholder : placeholders) {
            logger.debug("Got placeholder: {}", placeholder);
            try {
                // Предопределённые выражения -> отдельно обрабатываю
                if (ExpressionConfigurators.expressionIsPresent(placeholder)) {
                    logger.debug("Expression is present in configurator: {}", placeholder);
                    ExpressionConfigurator ec = ExpressionConfigurators.getExpressionConfiguratorByHolder(placeholder);
                    if (ec != null) {
                        ec.configure(paragraphCoordinates, placeholder);
                    }
                } else {
                    // Это то, что вернёт SpEL
                    // Получаем данные из переданного нам представления -> Java объекта
                    logger.debug("Attempt to resolve property!");
                    Object replacement = expressionResolver.resolveExpression(placeholder, expressionContext);

                    processGivenReplacementValue(replacement, document, paragraphWrapper, placeholder);
                }
            } catch (SpelEvaluationException | SpelParseException e) {
                if (isFailOnUnResolvedExpression()) {
                    throw e;
                } else {
                    logger.warn(String.format(
                            "Expression %s could not be resolved against context root of type %s. Reason: %s. Set log level to TRACE to view Stacktrace.",
                            placeholder, expressionContext.getClass(), e.getMessage()));
                    logger.trace("Reason for skipping expression:", e);

                    if (isLeaveEmptyOnExpressionError()) {
                        AbstractTypeResolver.replace(paragraphWrapper, null, placeholder, null);
                    }
                }
            }
        }
        if (this.lineBreakPlaceholder != null) {
            replaceLineBreaks(paragraphWrapper);
        }
    }

    private void processGivenReplacementValue(Object replacement, @NotNull WordprocessingMLPackage document, @NotNull ParagraphWrapper currentParagraphWrapper, @NotNull String currentPlaceholder) {
        if (replacement != null) {
            processGivenNotNullReplacementValue(replacement, document, currentParagraphWrapper, currentPlaceholder);
        } else if (replaceNullValues) {
            AbstractTypeResolver.replace(currentParagraphWrapper, null, currentPlaceholder, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void processGivenNotNullReplacementValue(@NotNull Object replacement, @NotNull WordprocessingMLPackage document, @NotNull ParagraphWrapper currentParagraphWrapper, @NotNull String currentPlaceholder) {
        AbstractTypeResolver<Object, Object> resolver = typeResolverRegistry.getResolverForType(replacement.getClass());
        resolver.process(document, resolver.resolve(document, replacement), currentParagraphWrapper, currentPlaceholder);
        logger.debug("Replaced expression '{}' with replacement {} of class {} and value provided by TypeResolver {}", currentPlaceholder, replacement, replacement.getClass(), resolver.getClass());
    }

    private void replaceLineBreaks(ParagraphWrapper paragraphWrapper) {
        Br lineBreak = Context.getWmlObjectFactory().createBr();
        R run = RunUtil.create(lineBreak);
        while (paragraphWrapper.getText().contains(this.lineBreakPlaceholder)) {
            AbstractTypeResolver.replace(paragraphWrapper, null, this.lineBreakPlaceholder, run);
        }
    }

    public boolean isFailOnUnResolvedExpression() {
        return failOnUnResolvedExpression;
    }

    public void setFailOnUnResolvedExpression(boolean failOnUnResolvedExpression) {
        this.failOnUnResolvedExpression = failOnUnResolvedExpression;
    }
}
