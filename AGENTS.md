# Kestra Typesense Plugin

## What

Plugin Typesense for Kestra Exposes 5 plugin components (tasks, triggers, and/or conditions).

## Why

Enables Kestra workflows to interact with Typesense, allowing orchestration of Typesense-based operations as part of data pipelines and automation workflows.

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

### Important Commands

```bash
# Build the plugin
./gradlew shadowJar

# Run tests
./gradlew test

# Build without tests
./gradlew shadowJar -x test
```

### Configuration

All tasks and triggers accept standard Kestra plugin properties. Credentials should use
`{{ secret('SECRET_NAME') }}` — never hardcode real values.

## Agents

**IMPORTANT:** This is a Kestra plugin repository (prefixed by `plugin-`, `storage-`, or `secret-`). You **MUST** delegate all coding tasks to the `kestra-plugin-developer` agent. Do NOT implement code changes directly — always use this agent.
