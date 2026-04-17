You are operating inside a code editor with access to tools for, for example, reading and writing project files.

You may reference information that is materially available to you through the tools you have access to.

If you need the contents of a file, YAML key, Markdown section, or graph node, you MUST request it using the appropriate tool.

Do not guess or infer the contents of any file or graph node.
Do not invent project structure, graph structure, functions, modules, or APIs.

When calling tools, output ONLY the tool call in the required JSON format.


# Project Context
# RAG
If RAG and the file structure disagree, RAG is incorrect.
The index is stale with probability approaching certainty.
**NEVER** treat absence of search results as absence of existence.

# order of reading
Look at files instead of guessing their contents.

## first
1. `README.md`
2. `src/main/java/net/bzethmayr/gigantspinosaurus/capabilities/HasCanonicalAttributes.java`
3. `src/main/java/net/bzethmayr/gigantspinosaurus/model/mar/ExposesMar.java`
4. `src/main/java/net/bzethmayr/gigantspinosaurus/usage/MarCreation.java`

## detailed structure
* the `src/main/java/net/bzethmayr/gigantspinosaurus/capabilities` package
* subpackages under `src/main/java/net/bzethmayr/gigantspinosaurus/model`

## least priority
* the `src/main/java/net/bzethmayr/gigantspinosaurus/util` package

# annotations in use
Circular class references are discouraged. Circular package references are not allowed.
`@LowerLevel` indicates that a package is part of the functionality layer and should not depend on application layers.
`@HigherLevel` indicates that a package is part of the application layer.

# testing
Use IDE built-in tools when available, e.g. `run_tests`.
This project uses a Kotlin build script `build.gradle.kts`
and a `gradlew` wrapper.
