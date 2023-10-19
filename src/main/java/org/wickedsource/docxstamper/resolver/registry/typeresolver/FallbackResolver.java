package org.wickedsource.docxstamper.resolver.registry.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.core.ParagraphWrapper;

/**
 * This ITypeResolver may serve as a fallback when there is no ITypeResolver available for a certain type. Hence, this
 * resolver is able to map all objects to their String value.
 */
public class FallbackResolver extends AbstractToTextResolver<Object> {

    @Override
    protected String resolveStringForObject(Object object) {
        if(object!=null)
            return String.valueOf(object);
        else
            return "";
    }

    @Override
    public void process(WordprocessingMLPackage document, Object resolvedObject, ParagraphWrapper paragraphWrapper, String placeholder) {
        replace(
                paragraphWrapper,
                null,
                placeholder,
                resolvedObject
        );
    }

}
