package io.kestra.plugin.typesense;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.executions.metrics.Counter;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
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
            code = {
                """
                    id: typesense
                    namespace: compnay.team

                    tasks:
                    - id: index_document
                      type: io.kestra.plugin.typesense.DocumentIndex
                      document: { "countryName":"France", "capital": "Paris", "gdp": 123456}
                      apiKey: test-key
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
    private Property<Map<String, Object>> document;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        Client client = getClient(runContext);
        String renderedCollection = renderCollection(runContext);
        client.collections(renderedCollection)
            .documents()
            .upsert(runContext.render(document).asMap(String.class, Object.class));

        runContext.metric(Counter.of("documentAdded", 1));
        logger.info("Successfully added documents to collection {}", renderedCollection);
        return null;
    }

}
