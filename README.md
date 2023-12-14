# KECCAK Function

## Overview

The project consists of 2 modules:

1. **sponge-api**: Provides a blueprint or template for various implementations of the Keccak function. As of right now
   the keccak-200 and keccak-1600 are implemented. The latter is implemented with r=1152 and c=448 and output 224 bits
   long. This means that it is faster because the message blocks have more size but on the other hand the size is not
   very big. Other possible solution for bigger output (512) is to have a block size of 576 bits and c 1024.

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
hash.

hash(message, messageSize);
```

Where messageSize is the size of the message in bits and the message is a stream of data.

