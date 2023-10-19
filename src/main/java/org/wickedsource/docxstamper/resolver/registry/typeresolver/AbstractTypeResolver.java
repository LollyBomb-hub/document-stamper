package org.wickedsource.docxstamper.resolver.registry.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.core.ParagraphWrapper;
import org.wickedsource.docxstamper.core.replace.ExpressionConfigurator;
import org.wickedsource.docxstamper.util.RunUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A type resolver is responsible for mapping an object of a certain Java class to an object of the DOCX4J api that
 * can be put into the .docx document. Type resolvers are used to replace placeholders within the .docx template.
 * </p>
 * <p>
 * Example: if an expression returns a Date object as result, this date object is passed to a DateResolver which
 * creates a org.docx4j.wml.R object (run of text) containing the properly formatted date string.
 * </p>
 * <p>
 * To use your own type resolver, implement this interface and register your implementation by calling
 * DocxStamper.getTypeResolverRegistry().addTypeResolver().
 * </p>
 */

@SuppressWarnings("unused")
public abstract class AbstractTypeResolver<S, T> {

    protected final Map<String, Object> configuration = new HashMap<>();

    /**
     * This method is called when a placeholder in the .docx template is to replaced by the result of an expression that
     * was found in the .docx template. It creates an object of the DOCX4J api that is put in the place of the found
     * expression.
     *
     * @param document         the Word document that can be accessed via the DOCX4J api.
     * @param expressionResult the result of an expression. Only objects of classes this type resolver is registered for
     *                         within the TypeResolverRegistry are passed into this method.
     * @return an object of the DOCX4J api (usually of type org.docx4j.wml.R = "run of text") that will be put in the place of an
     * expression found in the .docx document.
     */
    public abstract T resolve(WordprocessingMLPackage document, S expressionResult);

    public void process(WordprocessingMLPackage document, S resolvedObject, ParagraphWrapper paragraphWrapper, String placeholder) {
        T replacementObject = resolve(document, resolvedObject);
        replace(paragraphWrapper, null, placeholder, replacementObject);
    }

    public static void replace(ParagraphWrapper p, R stylingRun, String placeholder, Object replacementObject) {
        if (replacementObject == null) {
            replacementObject = RunUtil.create("");
        }
        if (replacementObject instanceof String) {
            p.replace(placeholder, RunUtil.createReplaceForString((String)replacementObject));
        } else if (replacementObject instanceof R) {
            RunUtil.applyParagraphStyle(p.getParagraph(), (R) replacementObject);
            if (stylingRun != null) {
                ((R) replacementObject).setRPr(stylingRun.getRPr());
            }
            p.replace(placeholder, (R) replacementObject);
        }
    }

    public Object getConfiguredProperty(String property) {
        return configuration.getOrDefault(property, null);
    }

    public void configure(String property, Object value) {
        configuration.put(property, value);
    }

    public void deleteProperty(String property) {
        configuration.remove(property);
    }

    public void clearConfiguration() {
        configuration.clear();
    }

    public ExpressionConfigurator[] getConfigurators() {
        return new ExpressionConfigurator[0];
    }
}
