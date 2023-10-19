package org.wickedsource.docxstamper;

import org.wickedsource.docxstamper.exceptions.UnresolvedExpressionException;
import org.wickedsource.docxstamper.resolver.context.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.resolver.context.language.NoOpEvaluationContextConfigurer;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.AbstractTypeResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides configuration parameters for DocxStamper.
 */
@SuppressWarnings("unused")
public class DocxStamperConfiguration {

  private String lineBreakPlaceholder;

  private EvaluationContextConfigurer<?> evaluationContextConfigurer = new NoOpEvaluationContextConfigurer<>();

  private boolean failOnUnresolvedExpression = true;

  private boolean leaveEmptyOnExpressionError = false;

  private boolean replaceNullValues = false;

  private final Map<Class<?>, AbstractTypeResolver<?, ?>> typeResolvers = new HashMap<>();

  private final Map<Class<?>, Map<String, Object>> propertyConfiguration = new HashMap<>();

  private AbstractTypeResolver<Object, ?> defaultTypeResolver = null;

  private final Map<Class<?>, Object> expressionFunctions = new HashMap<>();

  /**
   * The String provided as lineBreakPlaceholder will be replaces with a line break
   * when stamping a document. If no lineBreakPlaceholder is provided, no replacement
   * will take place.
   *
   * @param lineBreakPlaceholder the String that should be replaced with line breaks during stamping.
   * @return the configuration object for chaining.
   */
  public DocxStamperConfiguration setLineBreakPlaceholder(String lineBreakPlaceholder) {
    this.lineBreakPlaceholder = lineBreakPlaceholder;
    return this;
  }

  /**
   * Provides an {@link EvaluationContextConfigurer} which may change the configuration of a Spring
   * {@link org.springframework.expression.EvaluationContext} which is used for evaluating expressions
   * in comments and text.
   * @param evaluationContextConfigurer the configurer to use.
   */
  public DocxStamperConfiguration setEvaluationContextConfigurer(EvaluationContextConfigurer<?> evaluationContextConfigurer) {
    this.evaluationContextConfigurer = evaluationContextConfigurer;
    return this;
  }

  /**
   * If set to true, stamper will throw an {@link UnresolvedExpressionException}
   * if a variable expression or processor expression within the document or within the comments is encountered that cannot be resolved. Is set to true by default.
   */
  public DocxStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
    this.failOnUnresolvedExpression = failOnUnresolvedExpression;
    return this;
  }

  /**
   * <p>
   * Registers the given ITypeResolver for the given class. The registered ITypeResolver's resolve() method will only
   * be called with objects of the specified class.
   * </p>
   * <p>
   * Note that each type can only be resolved by ONE ITypeResolver implementation. Multiple calls to addTypeResolver()
   * with the same resolvedType parameter will override earlier calls.
   * </p>
   *
   * @param resolvedType the class whose objects are to be passed to the given ITypeResolver.
   * @param resolver     the resolver to resolve objects of the given type.
   * @param <T>          the type resolved by the ITypeResolver.
   */
  public <T> DocxStamperConfiguration addTypeResolver(Class<T> resolvedType, AbstractTypeResolver<?,?> resolver) {
    this.typeResolvers.put(resolvedType, resolver);
    return this;
  }

  /**
   * Exposes all methods of a given interface to the expression language.
   * @param interfaceClass the interface whose methods should be exposed in the expression language.
   * @param implementation  the implementation that should be called to evaluate invocations of the interface methods
   *                        within the expression language. Must implement the interface above.
   */
  public DocxStamperConfiguration exposeInterfaceToExpressionLanguage(Class<?> interfaceClass, Object implementation) {
    this.expressionFunctions.put(interfaceClass, implementation);
    return this;
  }

  /**
   * If an error is caught while evaluating an expression the expression will be replaced with an empty string instead
   * of leaving the original expression in the document.
   * @param leaveEmpty true to replace expressions with empty string when an error is caught while evaluating
   */
  public DocxStamperConfiguration leaveEmptyOnExpressionError(boolean leaveEmpty) {
    this.leaveEmptyOnExpressionError = leaveEmpty;
    return this;
  }

  /**
   * Indicates if expressions that resolve to null should be processed.
   * @param replaceNullValues true to replace null value expression with resolved value (which is null), false to leave the expression as is
   */
  public DocxStamperConfiguration replaceNullValues(boolean replaceNullValues) {
    this.replaceNullValues = replaceNullValues;
    return this;
  }

  /**
   * Creates a {@link DocxStamper} instance configured with this configuration.
   */
  public DocxStamper<?> build() {
    return new DocxStamper<>(this);
  }

  EvaluationContextConfigurer<?> getEvaluationContextConfigurer() {
    return evaluationContextConfigurer;
  }

  boolean isFailOnUnresolvedExpression() {
    return failOnUnresolvedExpression;
  }

  Map<Class<?>, AbstractTypeResolver<?, ?>> getTypeResolvers() {
    return typeResolvers;
  }

  AbstractTypeResolver<Object, ?> getDefaultTypeResolver() {
    return defaultTypeResolver;
  }

  public DocxStamperConfiguration setDefaultTypeResolver(AbstractTypeResolver<Object, ?> defaultTypeResolver) {
    this.defaultTypeResolver = defaultTypeResolver;
    return this;
  }

  public boolean isLeaveEmptyOnExpressionError() {
    return leaveEmptyOnExpressionError;
  }

  public boolean isReplaceNullValues() {
    return replaceNullValues;
  }

  String getLineBreakPlaceholder() {
    return lineBreakPlaceholder;
  }

  public Map<Class<?>, Object> getExpressionFunctions() {
    return expressionFunctions;
  }

  public void configureDefaultTypeResolver(Class<?> type, String property, Object value) {
    if (!propertyConfiguration.containsKey(type))
      propertyConfiguration.put(type, new HashMap<>());
    propertyConfiguration.get(type).put(property, value);
  }

  public Map<Class<?>, Map<String, Object>> getPropertyConfiguration() {
    return propertyConfiguration;
  }
}
