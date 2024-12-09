package io.kestra.plugin.typesense;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.FileSerde;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.typesense.api.Client;
import org.typesense.model.SearchParameters;
import org.typesense.model.SearchResult;
import reactor.core.publisher.Flux;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Search documents",
    description = "Search documents from a Typesense DB"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Search documents",
            code = {
                """
                    id: typesense
                    namespace: company.team

                    tasks:
                    - id: search
                      type: io.kestra.plugin.typesense.Search
                      apiKey: test-key
                      port: 8108
                      host: localhost
                      collection: Countries
                      query: Paris
                      queryBy: capital
                      filter: countryName: [France, England]
                      sortBy: gdp:desc
                    """
            }
        )
    }
)
public class Search extends AbstractTypesenseTask implements RunnableTask<Search.Output> {

    @Schema(
        title = "The query"
    )
    @NotNull
    protected Property<String> query;

    @Schema(
        title = "The fields to query",
        example= "country, capital"
    )
    @NotNull
    protected Property<String> queryBy;

    @Schema(
        title = "The filters to apply to the query"
    )
    protected Property<String> filter;

    @Schema(
        title = "The sorts to apply to the query"
    )
    protected Property<String> sortBy;


    @Override
    public Output run(RunContext runContext) throws Exception {
        Client client = getClient(runContext);
        SearchParameters searchParameters = buildSearchParam(runContext);
        Logger logger = runContext.logger();
        logger.debug("Search with query: {}", searchParameters);
        SearchResult searchResult = client.collections(renderCollection(runContext)).documents()
            .search(searchParameters);
        return generateOutput(runContext, searchResult);
    }

    protected SearchParameters buildSearchParam(RunContext runContext)
        throws IllegalVariableEvaluationException {
        return new SearchParameters()
            .q(renderString(query, runContext))
            .queryBy(renderString(queryBy, runContext))
            .filterBy(runContext.render(filter).as(String.class).orElse(""))
            .sortBy(runContext.render(sortBy).as(String.class).orElse(""));
    }

    protected static Output generateOutput(RunContext runContext, SearchResult searchResult)
        throws IOException {
        File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
        try (var output = new BufferedWriter(new FileWriter(tempFile), FileSerde.BUFFER_SIZE)) {
            FileSerde.writeAll(output, Flux.just(searchResult)).blockOptional();

            return Output.builder()
                .uri(runContext.storage().putFile(tempFile))
                .totalHits(searchResult.getFound())
                .build();
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {

        @Schema(title = "URI to output", description = "Results URI to an Amazon .ion file")
        private final URI uri;
        @Schema(title = "Hits number", description = "Number of items hit by the search request")
        private final Integer totalHits;
    }

}
