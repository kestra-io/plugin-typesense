package io.kestra.plugin.typesense;

import io.kestra.core.models.annotations.Metric;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.executions.metrics.Counter;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.FileSerde;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.typesense.api.Client;
import org.typesense.model.ImportDocumentsParameters;
import org.typesense.model.IndexAction;
import reactor.core.publisher.Flux;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Bulk-insert documents into a Typesense DB.",
    description = "Index documents to a Typesense DB from an ION file"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Index documents to a Typesense DB from an ION file",
            full = true,
            code = {
                """
                id: typesense_bulk_index
                namespace: company.team

                tasks:
                  - id: bulk_index
                    type: io.kestra.plugin.typesense.BulkIndex
                    apiKey: "{{ secret('TYPESENSE_API_KEY') }}"
                    port: 8108
                    host: localhost
                    collection: Countries
                    from: file_uri
                """
            }
        )
    },
    metrics = {
        @Metric(name = "requests.count", type = Counter.TYPE, description = "The total number of bulk index requests sent to Typesense"),
        @Metric(name = "records", type = Counter.TYPE, description = "The total number of records indexed")
    }
)
public class BulkIndex extends AbstractTypesenseTask implements RunnableTask<BulkIndex.Output> {

    @Schema(
        title = "The file URI containing the documents to index"
    )
    @NotNull
    private Property<String> from;

    @Schema(
        title = "The chunk size for every bulk request"
    )
    @Builder.Default
    private Property<Integer> chunk = Property.ofValue(1000);

    @Override
    public BulkIndex.Output run(RunContext runContext) throws Exception {
        Client client = getClient(runContext);
        String renderedCollection = renderCollection(runContext);
        Logger logger = runContext.logger();

        URI uri = new URI(renderString(from, runContext));

        try (
            BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(runContext.storage().getFile(uri)), FileSerde.BUFFER_SIZE);
        ) {
            AtomicLong count = new AtomicLong();
            Long requestCount = FileSerde.readAll(inputStream)
                .doOnNext(l -> count.incrementAndGet())
                .buffer(runContext.render(chunk).as(Integer.class).orElse(1000))
                .flatMap(documents -> bulkIndex(client, renderedCollection, documents, logger))
                .count().blockOptional().orElse(0L);
            runContext.metric(Counter.of("requests.count", requestCount));
            runContext.metric(Counter.of("records", count.get()));
            logger.info(
                "Successfully send {} requests for {} records",
                requestCount,
                count.get()
            );
            return Output.builder()
                .size(count.get())
                .build();
        }
    }

    private static Flux<String> bulkIndex(Client client, String collection, List documents,
        Logger logger) {
        return Flux.defer(() -> {
            try {
                ImportDocumentsParameters queryParameters = new ImportDocumentsParameters();
                queryParameters.action(IndexAction.UPSERT);
                return Flux.just(client.collections(collection)
                    .documents()
                    .import_(documents, queryParameters));
            } catch (Exception e) {
                logger.error(
                    "Unexpected error while trying to bulk index documents in the collection {}",
                    collection, e);
                return Flux.error(e);
            }
        });
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "The size of the rows fetched."
        )
        private Long size;
    }

}
