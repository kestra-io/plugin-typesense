package io.kestra.plugin.typesense;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.executions.metrics.Counter;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.typesense.api.Client;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Index a document to a Typesense DB"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Index a document to a Typesense DB",
            full = true,
            code = {
                """
                id: typesense_index_document
                namespace: compnay.team

                tasks:
                  - id: index_document
                    type: io.kestra.plugin.typesense.DocumentIndex
                    document: { "countryName":"France", "capital": "Paris", "gdp": 123456}
                    apiKey: "{{ secret('TYPESENSE_API_KEY') }}"
                    port: 8108
                    host: localhost
                    collection: Countries
                """
            }
        )
    }
)
public class DocumentIndex extends AbstractTypesenseTask implements RunnableTask<VoidOutput> {
    @Schema(
        title = "The document to index",
        description = "The document to index must be a Map<String, Object>"
    )
    @NotNull
    private Property<Map<String, Object>> document;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        Client client = getClient(runContext);
        String renderedCollection = renderCollection(runContext);
        client.collections(renderedCollection)
            .documents()
            .upsert(runContext.render(document).asMap(String.class, Object.class));

        Logger logger = runContext.logger();
        logger.debug("Successfully added documents to collection {}", renderedCollection);
        return null;
    }

}
