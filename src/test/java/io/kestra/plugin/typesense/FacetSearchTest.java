package io.kestra.plugin.typesense;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.storages.StorageInterface;
import io.kestra.plugin.typesense.Search.Output;
import io.kestra.plugin.typesense.typesense.TypesenseContainer;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * This test will only test the main task, this allows you to send any input parameters to your task
 * and test the returning behaviour easily.
 */
@KestraTest
class FacetSearchTest extends TypesenseContainer {

    @Inject
    private RunContextFactory runContextFactory;
    @Inject
    private StorageInterface storageInterface;

    @Test
    void should_search_documents_with_facet() throws Exception {
        insertDocument(buildDocument("France", "CapitalCity", 5));
        insertDocument(buildDocument("Germany", "CapitalCity", 25));
        insertDocument(buildDocument("England", "CapitalCity", 200));

        RunContext runContext = runContextFactory.of(Map.of());

        FacetSearch task = FacetSearch.builder()
            .apiKey(Property.of(KEY))
            .port(Property.of(PORT))
            .host(Property.of(HOST))
            .collection(Property.of(COLLECTION))
            .query(Property.of("CapitalCity"))
            .queryBy(Property.of("capital"))
            .facetBy(Property.of("gdp"))
            .build();

        Output runOutput = task.run(runContext);
        assertThat(runOutput.getTotalHits(), is(3));

        Map<String, Object> result = getResults(runOutput, storageInterface);
        Map<String, Object> facet = (Map<String, Object>) ((List<Map>) result.get("facet_counts")).get(0);
        assertThat(((List)facet.get("counts")).size(), is(3));
        assertThat(facet.get("field_name"), is("gdp"));
    }

}
