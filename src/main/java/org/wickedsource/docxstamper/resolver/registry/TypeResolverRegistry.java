package org.wickedsource.docxstamper.resolver.registry;

import org.wickedsource.docxstamper.resolver.registry.typeresolver.AbstractTypeResolver;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.FallbackResolver;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.table.TableResolver;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for all implementations of ITypeResolver that are used by DocxStamper.
 */
@SuppressWarnings("unused")
public class TypeResolverRegistry {

    public static final AbstractTypeResolver<?,?> TblResolver = new TableResolver();
    private AbstractTypeResolver<?,?> defaultResolver = new FallbackResolver();

    private final Map<Class<?>, AbstractTypeResolver<?,?>> typeResolversByType = new HashMap<>();

    public TypeResolverRegistry() {
        registerTypeResolver(Table.class, TblResolver);
    }

    public <T> void registerTypeResolver(Class<T> resolvedType, AbstractTypeResolver<?,?> resolver) {
        typeResolversByType.put(resolvedType, resolver);
    }

    public void setDefaultResolver(AbstractTypeResolver<?, ?> defaultResolver) {
        this.defaultResolver = defaultResolver;
    }

    /**
     * Gets the ITypeResolver that was registered for the specified type.
     *
     * @param type the class for which to find the ITypeResolver.
     * @param <T>  the type resolved by the ITypeResolver.
     * @return the ITypeResolver implementation that was earlier registered for the given class, or the default ITypeResolver
     * if none is found.
     */
    public <T> AbstractTypeResolver getResolverForType(Class<T> type) {
        return Objects.requireNonNullElse(typeResolversByType.get(type), defaultResolver);
    }

}