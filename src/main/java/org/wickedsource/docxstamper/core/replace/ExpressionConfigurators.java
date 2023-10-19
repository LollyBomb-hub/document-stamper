package org.wickedsource.docxstamper.core.replace;

import org.wickedsource.docxstamper.resolver.registry.TypeResolverRegistry;

public enum ExpressionConfigurators {

    Table(TypeResolverRegistry.TblResolver.getConfigurators());

    private final ExpressionConfigurator[] abstractTypeResolver;

    ExpressionConfigurators(ExpressionConfigurator[] abstractTypeResolver) {
        this.abstractTypeResolver = abstractTypeResolver;
    }

    public static boolean expressionIsPresent(String expression) {
        return getExpressionConfiguratorByHolder(expression) != null;
    }

    public static ExpressionConfigurator getExpressionConfiguratorByHolder(String placeholder) {
        for (ExpressionConfigurators ec : values()) {
            for (ExpressionConfigurator expressionConfigurator: ec.getConfigurators()) {
                if (expressionConfigurator.supports(placeholder)) {
                    return expressionConfigurator;
                }
            }
        }
        return null;
    }

    public ExpressionConfigurator[] getConfigurators() {
        return abstractTypeResolver;
    }
}
