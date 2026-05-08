# minify4j

Java CSS and JavaScript minifier backed by a bundled Rust WebAssembly engine and
[Chicory](https://github.com/dylibso/chicory).

## Installation

Gradle:

```gradle
dependencies {
    implementation "io.github.tienquanutc:minify4j:0.1.0"
}
```

Maven:

```xml
<dependency>
  <groupId>io.github.tienquanutc</groupId>
  <artifactId>minify4j</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage

Use the shared default instance for simple calls:

```java
import io.github.tienquanutc.minify4j.Minify4j;

String css = Minify4j.getDefaultInstance()
    .minifyCss(".foo { color: red; margin: 0; }");

String js = Minify4j.getDefaultInstance()
    .minifyJs("function add(first, second) { return first + second; }");
```

Create and reuse an instance when you want explicit lifecycle control:

```java
import io.github.tienquanutc.minify4j.Minify4j;

Minify4j minifier = Minify4j.create();

String css = minifier.minifyCss(".foo { color: red; margin: 0; }");
String js = minifier.minifyJs("function add(first, second) { return first + second; }");
```

Configure execution mode:

```java
import io.github.tienquanutc.minify4j.ExecutionMode;
import io.github.tienquanutc.minify4j.Minify4j;
import io.github.tienquanutc.minify4j.MinifyOptions;

MinifyOptions options = MinifyOptions.builder()
    .executionMode(ExecutionMode.RUNTIME_COMPILER)
    .interpreterFallbackEnabled(true)
    .build();

Minify4j minifier = Minify4j.create(options);
```

`ExecutionMode.RUNTIME_COMPILER` is the default and uses Chicory's runtime
compiler. `ExecutionMode.INTERPRETER` runs the bundled WebAssembly through
Chicory's interpreter without runtime bytecode generation.

## API

- `Minify4j.getDefaultInstance()`: returns a shared default minifier.
- `Minify4j.create()`: creates a new minifier with default options.
- `Minify4j.create(options)`: creates a new minifier with explicit options.
- `minifyCss(css)`: minifies CSS source text.
- `minifyJs(js)`: minifies JavaScript source text.

The bundled `wasm/minify4j.wasm` resource is an internal implementation detail.

## Coordinates

```text
groupId: io.github.tienquanutc
artifactId: minify4j
version: 0.1.0
Java package: io.github.tienquanutc.minify4j
```

## Build From Source

Requirements:

- JDK 17+
- Rust stable
- `wasm32-unknown-unknown` Rust target
- Bash for helper scripts

Install the Rust target once:

```bash
rustup target add wasm32-unknown-unknown
```

Build the WASM engine and copy it into Java resources:

```bash
./scripts/build-wasm.sh
```

Build and test:

```bash
./gradlew clean build
```

## Release

Create a Maven Central bundle:

```bash
./scripts/build-wasm.sh
./gradlew centralPortalBundle
```

Upload to Central Portal:

```bash
CENTRAL_PORTAL_USERNAME="your-token-username" \
CENTRAL_PORTAL_PASSWORD="your-token-password" \
SIGNING_KEY_BASE64="your-base64-private-key" \
SIGNING_PASSWORD="your-signing-key-password" \
./gradlew uploadCentralPortalBundle
```

By default uploads are `USER_MANAGED`, so finish publishing from the Central
Portal deployments page after validation.

## License

MIT License. See [LICENSE](LICENSE).
