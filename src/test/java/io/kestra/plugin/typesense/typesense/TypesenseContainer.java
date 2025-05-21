package io.kestra.plugin.typesense.typesense;

import io.kestra.core.serializers.FileSerde;
import io.kestra.core.storages.StorageInterface;
import io.kestra.core.tenant.TenantService;
import io.kestra.plugin.typesense.Search.Output;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.typesense.api.Client;
import org.typesense.api.Configuration;
import org.typesense.api.FieldTypes;
import org.typesense.model.CollectionSchema;
import org.typesense.model.DeleteDocumentsParameters;
import org.typesense.model.Field;
import org.typesense.resources.Node;

public class TypesenseContainer {

    public static final String KEY = "test-key";
    public static final String PORT = "8108";
    public static final String HOST = "localhost";
    public static final String COLLECTION = "Countries";
    private static GenericContainer<?> typesenseContainer;
    protected static Client client;

    @BeforeAll
    public static void setUp() throws Exception {
        typesenseContainer = new FixedHostPortGenericContainer<>("typesense/typesense:0.24.0")
            .withExposedPorts(8108)
            .withFixedExposedPort(8108, 8108)
            .withEnv("TYPESENSE_API_KEY", KEY)
            .withEnv("TYPESENSE_DATA_DIR", "/tmp");

        typesenseContainer.start();

        Configuration configuration = new Configuration(
            List.of(new Node("http", HOST, PORT)),
            Duration.ofSeconds(2), KEY);

        client = new Client(configuration);

        List<Field> fields = new ArrayList<>();
        fields.add(new Field().name("countryName").type(FieldTypes.STRING));
        fields.add(new Field().name("capital").type(FieldTypes.STRING));
        fields.add(new Field().name("gdp").type(FieldTypes.INT32).facet(true).sort(true));

        CollectionSchema collectionSchema = new CollectionSchema();
        collectionSchema.name(COLLECTION).fields(fields).defaultSortingField("gdp");

        client.collections().create(collectionSchema);

    }

    @BeforeEach
    protected void init() throws Exception {
        client.collections(COLLECTION).documents()
            .delete(new DeleteDocumentsParameters().filterBy(""));
    }

    @AfterAll
    public static void tearDown() {
        // Stop the container after tests
        typesenseContainer.stop();
    }

    public Map<String, Object> buildDocument(String countryName, String capital, Integer dgp) {
        return Map.of("countryName", countryName, "capital", capital, "gdp", dgp);
    }

    public void insertDocument(Map<String, Object> document) throws Exception {
        client.collections(COLLECTION).documents().upsert(document);
    }

    protected Map<String, Object> getResults(Output runOutput, StorageInterface storageInterface)
        throws IOException {
        BufferedReader searchInputStream = new BufferedReader(
            new InputStreamReader(storageInterface.get(TenantService.MAIN_TENANT, null, runOutput.getUri())));
        List<Map<String, Object>> resultWrapper = new ArrayList<>();
        FileSerde.reader(searchInputStream, r -> resultWrapper.add((Map<String, Object>) r));
        return resultWrapper.getFirst();
    }
}
