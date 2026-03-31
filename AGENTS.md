# order of reading
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