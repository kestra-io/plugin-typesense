# Kestra Typesense Plugin

## What

- Provides plugin components under `io.kestra.plugin.typesense`.
- Includes classes such as `DocumentGet`, `FacetSearch`, `Search`, `BulkIndex`.

## Why

- This plugin integrates Kestra with Typesense.
- It provides tasks that load data into and query Typesense collections.

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
