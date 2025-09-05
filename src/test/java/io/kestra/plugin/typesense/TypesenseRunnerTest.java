package io.kestra.plugin.typesense;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.queues.QueueException;
import io.kestra.core.repositories.LocalFlowRepositoryLoader;
import io.kestra.core.runners.RunnerUtils;
import io.kestra.core.runners.TestRunner;
import io.kestra.core.tenant.TenantService;
import io.kestra.plugin.typesense.typesense.TypesenseContainer;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test will load all flow located in `src/test/resources/flows/`
 * and will run an in-memory runner to be able to test a full flow. There is also a
 * configuration file in `src/test/resources/application.yml` that is only for the full runner
 * test to configure in-memory runner.
 */
@KestraTest
class TypesenseRunnerTest extends TypesenseContainer {
    @Inject
    protected TestRunner runner;

    @Inject
    protected RunnerUtils runnerUtils;

    @Inject
    protected LocalFlowRepositoryLoader repositoryLoader;

    @BeforeEach
    protected void init() throws IOException, URISyntaxException {
        repositoryLoader.load(Objects.requireNonNull(TypesenseRunnerTest.class.getClassLoader().getResource("flows")));
        this.runner.run();
    }

    @Test
    void flow() throws TimeoutException, QueueException {
        Execution execution = runnerUtils.runOne(TenantService.MAIN_TENANT, "io.kestra.plugin.typesense", "typesense");

        assertThat(execution.getTaskRunList(), hasSize(2));
        assertThat(execution.getTaskRunList().get(0).getOutputs().size(), is(0));
        assertThat(execution.getTaskRunList().get(1).getOutputs().get("document"),
            is(Map.of("countryName", "France", "capital", "Paris", "gdp", 123456, "id", "0")));
    }
}
