package io.kestra.plugin.typesense;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.typesense.api.Client;
import org.typesense.api.exceptions.ObjectNotFound;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get a document from a Typesense DB"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Get a document from a Typesense DB",
            code = {
                """
                    id: typesense
                    namespace: compnay.team

                    tasks:
                    - id: get_document
                      type: io.kestra.plugin.typesense.DocumentGet
                      documentId: "0"
                      apiKey: test-key
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
        title = "The id of the document to get",
        example = "0"
    )
    private Property<String> documentId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        Client client = getClient(runContext);
        Map<String, Object> document = null;
        String renderedDocumentId = runContext.render(documentId).as(String.class).orElseThrow();
        try {
            document = client.collections(renderCollection(runContext))
                .documents(renderedDocumentId)
                .retrieve();
        } catch (ObjectNotFound e){
            logger.error("No document found for id {}", renderedDocumentId, e);
        }

        return Output.builder()
            .document(document)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {

        @Schema(
            title = "Short description for this output",
            description = "Full description of this output"
        )
        private Map<String, Object> document;
    }

}
