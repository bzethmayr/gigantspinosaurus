# DEVOXXGENIE.md

## Project Guidelines
### RAG
If RAG and the file structure disagree, RAG is incorrect.
The index is stale with probability approaching certainty.
There is also a relevance threshold in place,
and an existing file is not guaranteed to appear relevant to itself.
Don't treat absence of search results as absence of existence.

### Tests
Use the `run_tests` tool if available instead of invoking Gradle directly.

### Build Commands

This project uses a Kotlin build script `build.gradle.kts`.

- **Build:** `./gradlew build`
- **Test:** The run_tests tool or optionally `./gradlew test`
- **Single Test:** `./gradlew test --tests ClassName.methodName`
- **Clean:** `./gradlew clean`
- **Run:** `./gradlew run`

### Code Style

- **Formatting:** Use IDE or checkstyle for formatting
- **Naming:**
  - Use camelCase for variables, methods, and fields
  - Use PascalCase for classes and interfaces
  - Use SCREAMING_SNAKE_CASE for constants
- **Documentation:** Use JavaDoc for documentation
- **Imports:** Organize imports and avoid wildcard imports
- **Exception Handling:**
  - Prefer factory methods from `net.zethmayr.fungu.core.ExceptionFactory`.
  - Use specific exceptions and document throws.
  - *Do not* use SneakyThrows.
  - When composing `java.util.function` types, you can 
    1. use `net.zethmayr.fungu.throwing.SinkableHelper` methods to obtain
    2. `net.zethmayr.fungu.throwing.Sinkable` which combine with 
    3. sinks from `net.zethmayr.fungu.throwing.SinkFactory`
    4. to tunnel or defer checked exception handling.


### Project Tree

```
gigantspinosaurus/
  src/
    main/
      java/
        net/
          bzethmayr/
            gigantspinosaurus/
              util/
                package-info.java
                CollectionHelper.java
              model/
                Face.java
                Frame.java
                North.java
                Vertical.java
                ChainFrame.java
                Handedness.java
                Geoposition.java
                Orientation.java
                MarSignature.java
                package-info.java
                BoundAttributes.java
                AttributePlumbing.java
                AttestationContext.java
                AttributeVisibility.java
                MinimalAttestationRecord.java
              Main.java
              capabilities/
                nonce/
                  package-info.java
                  GeneratesNonce.java
                  DefaultNonceFactory.java
                orientation/
                  package-info.java
                  QuaternionHelper.java
                  ExposesQuaternion.java
                package-info.java
                AttributeValuations.java
                HasMappedAttributes.java
                HasRequiredAttributes.java
                HasCanonicalAttributes.java
      resources/
    test/
      java/
        net/
          bzethmayr/
            gigantspinosaurus/
              util/
                CollectionHelperTest.java
              model/
                FrameTest.java
                OrientationTest.java
              capabilities/
                orientation/
                  QuaternionHelperTest.java
                  TestsWithQuaternions.java
                HasMappedAttributesTest.java
              TestsWithEnums.java
      resources/
  gradle/
    wrapper/
      gradle-wrapper.properties
  AGENTS.md
  README.md
  LICENSE.md
  DEVOXXGENIE.md

```
