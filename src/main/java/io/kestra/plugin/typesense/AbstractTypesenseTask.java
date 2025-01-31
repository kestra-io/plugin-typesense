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
        title = "The host of the typesense DB"
    )
    @NotNull
    protected Property<String> host;

    @Schema(
        title = "The port of the typesense DB"
    )
    @NotNull
    protected Property<String> port;

    @Schema(
        title = "The API key to connect to the typesense DB"
    )
    @NotNull
    protected Property<String> apiKey;

    @Schema(
        title = "The name of the typesense collection"
    )
    @NotNull
    protected Property<String> collection;

    @Schema(
        title = "Is HTTPS used",
        description = "By default, HTTP protocol will be use. Set this value to true tu use HTTPS"
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
