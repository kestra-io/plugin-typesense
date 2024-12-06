package io.kestra.plugin.typesense;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.FileSerde;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.typesense.api.Client;
import org.typesense.model.ImportDocumentsParameters;
import org.typesense.model.IndexAction;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Bulk insert documents",
    description = "Index documents to a Typesense DB from an ION file"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Index documents to a Typesense DB from an ION file",
            code = {
                """
                    id: typesense
                    namespace: company.team

                    tasks:
                    - id: bulk_index
                      type: io.kestra.plugin.typesense.BulkIndex
                      apiKey: test-key
                      port: 8108
                      host: localhost
                      collection: Countries
                      from: file_uri
                    """
            }
        )
    }
)
public class BulkIndex extends AbstractTypesenseTask implements RunnableTask<VoidOutput> {

    @Schema(
        title = "The file URI containing the documents to index"
    )
    private Property<String> from;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        Client client = getClient(runContext);
        String renderedCollection = renderCollection(runContext);

        URI uri = new URI(renderString(from, runContext));

        try (
            BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(runContext.storage().getFile(uri)), FileSerde.BUFFER_SIZE);
        ) {
            List documents = FileSerde.readAll(inputStream, List.class).blockLast();

            ImportDocumentsParameters queryParameters = new ImportDocumentsParameters();
            queryParameters.action(IndexAction.UPSERT);
            client.collections(renderedCollection)
                .documents()
                .import_(documents, queryParameters);
        }
        return null;
    }

}
