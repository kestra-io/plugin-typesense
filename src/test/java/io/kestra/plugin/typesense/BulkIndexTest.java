package io.kestra.plugin.typesense;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.devskiller.friendly_id.FriendlyId;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.storages.StorageInterface;
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
            null,
            null,
            new URI("/" + FriendlyId.createFriendlyId()),
            new FileInputStream(new File(Objects.requireNonNull(BulkIndexTest.class.getClassLoader()
                    .getResource("files/bulk_import.ion"))
                .toURI()))
        );

        RunContext runContext = runContextFactory.of(Map.of());

        BulkIndex task = BulkIndex.builder()
            .apiKey(Property.of(KEY))
            .port(Property.of(PORT))
            .host(Property.of(HOST))
            .collection(Property.of(COLLECTION))
            .from(Property.of(source.toString()))
            .build();

        task.run(runContext);

        String export = client.collections(COLLECTION).documents().export();

        assertThat(export, containsString("France"));
        assertThat(export, containsString("Germany"));
        assertThat(export, containsString("England"));
    }


}
