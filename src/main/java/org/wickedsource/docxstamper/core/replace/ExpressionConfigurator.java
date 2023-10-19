package org.wickedsource.docxstamper.core.replace;

import org.wickedsource.docxstamper.core.walk.coordinates.ParagraphCoordinates;

public interface ExpressionConfigurator {

    void configure(ParagraphCoordinates pw, String placeholder);
    boolean supports(String placeholder);

}
