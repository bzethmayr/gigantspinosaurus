# Project Context
# RAG
If RAG and the file structure disagree, RAG is incorrect.
The index is stale with probability approaching certainty.
**NEVER** treat absence of search results as absence of existence.

# order of reading
Look at files instead of guessing their contents.

## first
1. `README.md`
2. `src/main/java/net/bzethmayr/gigantspinosaurus/capabilities/HasMappedAttributes.java`
3. `src/main/java/net/bzethmayr/gigantspinosaurus/model/MinimalAttestationRecord.java`

## least priority
* any `package-info.java`
* empty classes

# annotations in use
`@LowerLevel` indicates that a package is part of the functionality layer.
`@HigherLevel` indicates that a package is part of the application layer.
You can otherwise ignore `package-info.java`.

# testing
Use IDE built-in tools when available, e.g. `run_tests`.
This project uses a Kotlin build script `build.gradle.kts`
and a `gradlew` wrapper.
