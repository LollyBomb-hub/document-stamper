package org.wickedsource.docxstamper.resolver.registry.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.RunUtil;

/**
 * Abstract ITypeResolver that takes a String from the implementing sub class and creates a Run of text
 * from it.
 *
 * @param <S> the type which to map into a run of text.
 */
public abstract class AbstractToTextResolver<S> extends AbstractTypeResolver<S, R> {

    protected abstract String resolveStringForObject(S object);

    @Override
    public R resolve(WordprocessingMLPackage document, S expressionResult) {
        String text = resolveStringForObject(expressionResult);
        return RunUtil.create(text);
    }
}
