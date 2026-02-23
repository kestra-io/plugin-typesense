package io.kestra.plugin.typesense;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.typesense.api.Client;
import org.typesense.api.Configuration;
import org.typesense.resources.Node;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public abstract class AbstractTypesenseTask extends Task {

    @Schema(
        title = "Typesense host",
        description = "Hostname or IP address of the Typesense cluster node or load balancer"
    )
    @NotNull
    protected Property<String> host;

    @Schema(
        title = "Typesense port",
        description = "TCP port for the Typesense HTTP API; 8108 is the Typesense default"
    )
    @NotNull
    protected Property<String> port;

    @Schema(
        title = "Typesense API key",
        description = "Admin or search key used for this request; must allow access to the target collection"
    )
    @NotNull
    protected Property<String> apiKey;

    @Schema(
        title = "Collection name",
        description = "Name of the Typesense collection; value is rendered before each call"
    )
    @NotNull
    protected Property<String> collection;

    @Schema(
        title = "Use HTTPS",
        description = "Default false (HTTP). Set to true to call Typesense over HTTPS/TLS"
    )
    protected Property<Boolean> https;

    protected Client getClient(RunContext context) throws IllegalVariableEvaluationException {
        Configuration configuration = new Configuration(
            List.of(new Node(
                context.render(https).as(Boolean.class).orElse(false) ? "https": "http",
                context.render(host).as(String.class).orElseThrow(),
                context.render(port).as(String.class).orElseThrow())
            ),
            Duration.ofSeconds(2),context.render(apiKey).as(String.class).orElseThrow());
        return new Client(configuration);
    }

    protected String renderCollection(RunContext context)
        throws IllegalVariableEvaluationException {
        return context.render(collection).as(String.class).orElseThrow();
    }

    protected String renderString(Property<String> property, RunContext context)
        throws IllegalVariableEvaluationException {
        return context.render(property).as(String.class).orElseThrow();
    }

}
