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
            .document(Property.of(Map.of("countryName", "France", "capital", "Paris", "gdp", 123456)))
            .apiKey(Property.of(KEY))
            .port(Property.of(PORT))
            .host(Property.of(HOST))
            .collection(Property.of(COLLECTION))
            .build();

        task.run(runContext);

        assertThat(runContext.metrics().size(), is(1));
        assertThat(runContext.metrics().getFirst().getName(), is("documentAdded"));
        assertThat(runContext.metrics().getFirst().getValue(), is(1D));
    }
}
