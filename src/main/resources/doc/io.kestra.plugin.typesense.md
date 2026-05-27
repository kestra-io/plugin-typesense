# How to use the Typesense plugin

Index, search, and manage documents in Typesense from Kestra flows.

## Authentication

Set `host`, `port`, `apiKey`, and `collection` (all required). Set `https: true` for TLS connections. Store secrets in [secrets](https://kestra.io/docs/concepts/secret) and apply connection properties globally with [plugin defaults](https://kestra.io/docs/workflow-components/plugin-defaults).

## Tasks

`DocumentGet` retrieves a single document by `documentId` from the configured `collection`.

`DocumentIndex` upserts a single document — set `document` as a map of field names to values.

`BulkIndex` bulk-indexes documents from a file in internal storage — set `from` to a `kestra://` URI. Control batch size with `chunk` (default 1000).

`Search` runs a query — set `query` (the search text) and `queryBy` (comma-separated field names to search). Optionally narrow results with `filter` and control ordering with `sortBy`.

`FacetSearch` extends `Search` with faceting — additionally set `facetBy` as a comma-separated list of fields to facet on.
