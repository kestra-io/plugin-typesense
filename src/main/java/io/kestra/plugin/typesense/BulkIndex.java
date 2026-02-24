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
    title = "Bulk upsert documents into Typesense",
    description = "Streams records from an Amazon ION file in internal storage and upserts them into the target collection."
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
                    from: kestra://data/countries.ion
                """
            }
        )
    },
    metrics = {
 @Metric(name = "requests.count", description = "Number of request", type = Counter.TYPE),
        @Metric(name = "records", description = "Number of records", type = Counter.TYPE),
    }
)
public class BulkIndex extends AbstractTypesenseTask implements RunnableTask<BulkIndex.Output> {

    @Schema(
        title = "Input ION file URI",
        description = "kestra:// or other storage URI pointing to an Amazon ION file with one JSON document per line."
    )
    @NotNull
    private Property<String> from;

    @Schema(
        title = "Bulk chunk size",
        description = "Number of documents per Typesense bulk call. Default 1000; lower to reduce memory, raise to improve throughput."
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
            title = "Indexed document count",
            description = "Total number of documents read from the input and sent to Typesense."
        )
        private Long size;
    }

}
