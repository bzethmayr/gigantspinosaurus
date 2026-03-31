# DEVOXXGENIE.md

## Project Guidelines

### Build Commands

- **Build:** `./gradlew build`
- **Test:** `./gradlew test`
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
- **Exception Handling:** Prefer specific exceptions and document throws



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
