package io.kestra.plugin.typesense;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Builder;
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
    title = "Get a document from a Typesense DB."
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Get a document from a Typesense DB",
            full = true,
            code = {
                """
                id: typesense_get_document
                namespace: compnay.team

                tasks:
                  - id: get_document
                    type: io.kestra.plugin.typesense.DocumentGet
                    documentId: "0"
                    apiKey: "{{ secret('TYPESENSE_API_KEY') }}"
                    port: 8108
                    host: localhost
                    collection: Countries
                """
            }
        )
    }
)
public class DocumentGet extends AbstractTypesenseTask implements RunnableTask<DocumentGet.Output> {

    @Schema(
        title = "The id of the document to get"
    )
    @NotNull
    private Property<String> documentId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Client client = getClient(runContext);
        Map<String, Object> document = null;
        String renderedDocumentId = runContext.render(documentId).as(String.class).orElseThrow();
        document = client.collections(renderCollection(runContext))
            .documents(renderedDocumentId)
            .retrieve();

        Logger logger = runContext.logger();
        logger.debug("Document {} successfully retrieved", document);
        return Output.builder()
            .document(document)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {

        @Schema(
            title = "The document fetched from the database"
        )
        private Map<String, Object> document;
    }

}
