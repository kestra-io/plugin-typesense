package io.kestra.plugin.typesense;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.typesense.model.SearchParameters;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Search with facets in Typesense",
    description = "Performs a search and computes facet counts on the specified fields."
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Search documents with facet",
            full = true,
            code = {
                """
                id: typesense_facet_search
                namespace: company.team

                tasks:
                  - id: facet_search
                    type: io.kestra.plugin.typesense.FacetSearch
                    apiKey: "{{ secret('TYPESENSE_API_KEY') }}"
                    port: 8108
                    host: localhost
                    collection: Countries
                    query: Paris
                    queryBy: capital
                    filter: "countryName: [France, England]"
                    sortBy: "gdp:desc"
                    facetBy: gdp
                """
            }
        )
    }
)
public class FacetSearch extends Search {

    @Schema(
        title = "Facet fields",
        description = "Comma-separated list passed to `facet_by` to compute facet counts."
    )
    @NotNull
    protected Property<String> facetBy;

    @Override
    protected SearchParameters buildSearchParam(RunContext runContext)
        throws IllegalVariableEvaluationException {
        SearchParameters searchParameters = super.buildSearchParam(runContext);
        return searchParameters.facetBy(renderString(facetBy, runContext));
    }
}
