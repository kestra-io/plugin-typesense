package io.kestra.plugin.typesense;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.typesense.typesense.TypesenseContainer;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * This test will only test the main task, this allows you to send any input parameters to your task
 * and test the returning behaviour easily.
 */
@KestraTest
class DocumentGetTest extends TypesenseContainer {

    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void should_get_document_when_found() throws Exception {
        insertDocument(buildDocument("France", "Paris", 123456));

        RunContext runContext = runContextFactory.of(Map.of());

        DocumentGet task = DocumentGet.builder()
            .documentId(Property.of("0"))
            .apiKey(Property.of(KEY))
            .port(Property.of(PORT))
            .host(Property.of(HOST))
            .collection(Property.of(COLLECTION))
            .build();

        DocumentGet.Output runOutput = task.run(runContext);

        assertThat(runOutput.getDocument().get("countryName"), is("France"));
        assertThat(runOutput.getDocument().get("capital"), is("Paris"));
        assertThat(runOutput.getDocument().get("gdp"), is(123456));
    }

}
