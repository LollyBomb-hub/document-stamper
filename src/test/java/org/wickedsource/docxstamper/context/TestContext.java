package org.wickedsource.docxstamper.context;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@AllArgsConstructor
public class TestContext {

    private final Logger logger = LoggerFactory.getLogger(TestContext.class);

    private String a;
    private String b;

    public String test(String str) {
        logger.debug("Got string in test method: {}", str);
        return str;
    }

}
