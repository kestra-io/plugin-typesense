package io.kestra.plugin.typesense;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.storages.StorageInterface;
import io.kestra.plugin.typesense.typesense.TypesenseContainer;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

/**
 * This test will only test the main task, this allows you to send any input parameters to your task
 * and test the returning behaviour easily.
 */
@KestraTest
class SearchTest extends TypesenseContainer {

    @Inject
    private RunContextFactory runContextFactory;
    @Inject
    private StorageInterface storageInterface;

    @Test
    void should_search_documents() throws Exception {
        insertDocument(buildDocument("France", "Paris", 5));
        insertDocument(buildDocument("Germany", "Berlin", 25));
        insertDocument(buildDocument("England", "London", 200));

        RunContext runContext = runContextFactory.of(Map.of());

        Search task = Search.builder()
            .apiKey(Property.of(KEY))
            .port(Property.of(PORT))
            .host(Property.of(HOST))
            .collection(Property.of(COLLECTION))
            .query(Property.of("Paris"))
            .queryBy(Property.of("capital"))
            .build();

        Search.Output runOutput = task.run(runContext);
        assertThat(runOutput.getTotalHits(), is(1));

        Map<String, Object> result = getResults(runOutput, storageInterface);
        Map<String, Object> document = (Map<String, Object>) ((List<Map>) result.get("hits")).get(0).get("document");
        assertThat(document.get("countryName"), Is.is("France"));
        assertThat(document.get("capital"), Is.is("Paris"));
        assertThat(document.get("gdp"), Is.is(5));
    }

    @Test
    void should_search_documents_filter_and_sort() throws Exception {
        insertDocument(buildDocument("country1", "CapitalCity", 5));
        insertDocument(buildDocument("country2", "CapitalCity", 25));
        insertDocument(buildDocument("England", "CapitalCity", 200));

        RunContext runContext = runContextFactory.of(Map.of());

        Search task = Search.builder()
            .apiKey(Property.of(KEY))
            .port(Property.of(PORT))
            .host(Property.of(HOST))
            .collection(Property.of(COLLECTION))
            .query(Property.of("CapitalCity"))
            .queryBy(Property.of("capital"))
            .filter(Property.of("countryName: [country1, country2]"))
            .sortBy(Property.of("gdp:desc"))
            .build();

        Search.Output runOutput = task.run(runContext);
        assertThat(runOutput.getTotalHits(), is(2));

        Map<String, Object> result = getResults(runOutput, storageInterface);
        Map<String, Object> document1 = (Map<String, Object>) ((List<Map>) result.get("hits")).get(0).get("document");
        assertThat(document1.get("countryName"), Is.is("country2"));
        assertThat(document1.get("capital"), Is.is("CapitalCity"));
        assertThat(document1.get("gdp"), Is.is(25));

        Map<String, Object> document2 = (Map<String, Object>) ((List<Map>) result.get("hits")).get(1).get("document");
        assertThat(document2.get("countryName"), Is.is("country1"));
        assertThat(document2.get("capital"), Is.is("CapitalCity"));
        assertThat(document2.get("gdp"), Is.is(5));
    }

    @Test
    void should_search_documents_no_hits() throws Exception {
        insertDocument(buildDocument("France", "Paris", 5));

        RunContext runContext = runContextFactory.of(Map.of());

        Search task = Search.builder()
            .apiKey(Property.of(KEY))
            .port(Property.of(PORT))
            .host(Property.of(HOST))
            .collection(Property.of(COLLECTION))
            .query(Property.of("Tokyo"))
            .queryBy(Property.of("capital"))
            .build();

        Search.Output runOutput = task.run(runContext);
        assertThat(runOutput.getTotalHits(), is(0));

        Map<String, Object> result = getResults(runOutput, storageInterface);
        assertTrue(((List<Map>) result.get("hits")).isEmpty());
    }

}
