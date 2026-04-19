# Kestra Typesense Plugin

## What

- Provides plugin components under `io.kestra.plugin.typesense`.
- Includes classes such as `DocumentGet`, `FacetSearch`, `Search`, `BulkIndex`.

## Why

- What user problem does this solve? Teams need to load data into and query Typesense collections from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Typesense steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Typesense.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `typesense`

### Key Plugin Classes

- `io.kestra.plugin.typesense.BulkIndex`
- `io.kestra.plugin.typesense.DocumentGet`
- `io.kestra.plugin.typesense.DocumentIndex`
- `io.kestra.plugin.typesense.FacetSearch`
- `io.kestra.plugin.typesense.Search`

### Project Structure

```
plugin-typesense/
├── src/main/java/io/kestra/plugin/typesense/
├── src/test/java/io/kestra/plugin/typesense/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
