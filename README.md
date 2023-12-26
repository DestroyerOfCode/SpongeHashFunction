# KECCAK Function

## Overview

The project consists of 3 modules:

1. **sponge-api**: Provides a blueprint or template for various implementations of the Keccak function. As of right now
   the keccak-200 and keccak-1600 are implemented.
2. **keccak-1600-256**: Implemented with r=1088 and c=512 and output 256 bits
   long. The hash method can be called without knowing the size of the message, or knowing it.
   In the former case, the message is an InputStream, in the second case it is a long[].
3. **keccak-200-168**: Implemented with r=168 and c=32 and outputs 168 bits long.
   The hash method can be called without knowing the size of the message, or knowing it.
   In the former case, the message is an InputStream, in the second case it is a byte[].

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
final Permutation permutation = new PermutationImpl();
final byte[] message = {33, -127, 10, 33, -127, 10, 33}; // 7
final InputStream is = new ByteArrayInputStream(message);
final Hash hashImpl = new SpongeHashKeccak200Impl(permutation);

hashImpl.hash(message, message.length);
```

**or**

```java
final Permutation permutation = new PermutationImpl();
final byte[] bytes = new byte[1];
final Hash hashImpl = new SpongeHashKeccak200Impl(permutation);

hashImpl.hash(message);
```

Where messageSize is the size of the message in bits and the message is a stream of data.

## REQUIREMENTS

1. Java 21
