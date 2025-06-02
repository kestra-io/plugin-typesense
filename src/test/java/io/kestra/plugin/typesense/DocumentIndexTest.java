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
 * This test will only test the main task, this allow you to send any input
 * parameters to your task and test the returning behaviour easily.
 */
@KestraTest
class DocumentIndexTest extends TypesenseContainer {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void should_index_document() throws Exception {
        RunContext runContext = runContextFactory.of(Map.of());

        DocumentIndex task = DocumentIndex.builder()
            .document(Property.ofValue(Map.of("countryName", "France", "capital", "Paris", "gdp", 123456)))
            .apiKey(Property.ofValue(KEY))
            .port(Property.ofValue(PORT))
            .host(Property.ofValue(HOST))
            .collection(Property.ofValue(COLLECTION))
            .build();

        task.run(runContext);

        Map<String, Object> documents = client.collections(COLLECTION).documents("0").retrieve();

        assertThat(documents.get("countryName"), is("France"));
        assertThat(documents.get("capital"), is("Paris"));
        assertThat(documents.get("gdp"), is(123456));
    }
}
