# KECCAK Function

## Overview
The project consists of 2 modules:

1. **sponge-api**: Provides a blueprint or template for various implementations of the Keccak function. Currently, only the Keccak-200 implementation is available due to its minimal memory requirements (only a 200-bit state).

## Implementation
The core of the implementation is the hash method, which comes in 2 forms:

- Knowing the message size
- Not knowing the message size

The former is preferred for its lower memory requirements.

## Integration
1. Build the project with Gradle from the root directory:
    ``./gradlew clean build``
2. Copy the generated JAR file from the `build/libs` directory.
3. Create the objects:
```java
Permutation permutation = new PermutationImpl();
Hash hash = new SpongeHashKeccak200Impl(permutation);
hash.hash(message, messageSize);
```
Where messageSize is the size of the message in bits and the message is a stream of data.

