package io.kestra.plugin.typesense;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.devskiller.friendly_id.FriendlyId;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.storages.StorageInterface;
import io.kestra.core.tenant.TenantService;
import io.kestra.plugin.typesense.BulkIndex.Output;
import io.kestra.plugin.typesense.typesense.TypesenseContainer;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * This test will only test the main task, this allows you to send any input parameters to your task
 * and test the returning behaviour easily.
 */
@KestraTest
class BulkIndexTest extends TypesenseContainer {

    @Inject
    private RunContextFactory runContextFactory;
    @Inject
    StorageInterface storageInterface;

    @Test
    void should_bulk_index_documents() throws Exception {

        URI source = storageInterface.put(
            TenantService.MAIN_TENANT,
            null,
            new URI("/" + FriendlyId.createFriendlyId() + ".ion"),
            new FileInputStream(new File(Objects.requireNonNull(BulkIndexTest.class.getClassLoader()
                    .getResource("files/bulk_import.ion"))
                .toURI()))
        );

        RunContext runContext = runContextFactory.of(Map.of());

        BulkIndex task = BulkIndex.builder()
            .apiKey(Property.ofValue(KEY))
            .port(Property.ofValue(PORT))
            .host(Property.ofValue(HOST))
            .collection(Property.ofValue(COLLECTION))
            .from(Property.ofValue(source.toString()))
            .chunk(Property.ofValue(2))
            .build();

        Output output = task.run(runContext);

        assertThat(output.getSize(), is(3L));

        String export = client.collections(COLLECTION).documents().export();

        assertThat(export, containsString("France"));
        assertThat(export, containsString("Germany"));
        assertThat(export, containsString("England"));

        assertThat(runContext.metrics().size(), is(2));
        assertThat(runContext.metrics().get(0).getName(), is("requests.count"));
        assertThat(runContext.metrics().get(0).getValue(), is(2D));
        assertThat(runContext.metrics().get(1).getName(), is("records"));
        assertThat(runContext.metrics().get(1).getValue(), is(3D));
    }


}
