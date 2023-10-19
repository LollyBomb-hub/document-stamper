package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.context.TestContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

class DocxStamperTest {

    private static DocxStamper<TestContext> docxStamper;
    private static TestContext testContext;

    @BeforeAll
    public static void setUp() {
        testContext = new TestContext("A", "B");
        docxStamper = new DocxStamper<>();
    }

    @Test
    void stamp() throws IOException, Docx4JException, URISyntaxException {
        OutputStream baos = Files.newOutputStream(Path.of("src/test/resources/templates/emptyPlaceholderResult.docx"));
        URL url = Thread.currentThread().getContextClassLoader().getResource("templates/emptyPlaceholder.docx");
        assert url != null;
        docxStamper.stamp(Files.newInputStream(Path.of(url.toURI())), testContext, baos);
    }
}