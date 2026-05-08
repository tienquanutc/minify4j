# Release

## Maven Coordinates

```text
groupId: io.github.tienquanutc
artifactId: minify4j
version: 0.1.0
```

The version is defined in the root `build.gradle`.

## Central Portal Release

Run from the repository root:

```bash
./scripts/build-wasm.sh
./gradlew centralPortalBundle
```

This flow:

1. Builds `minify4j-wasm` for `wasm32-unknown-unknown`.
2. Copies `minify4j-wasm/target/wasm32-unknown-unknown/release/minify4j_wasm.wasm` to `src/main/resources/wasm/minify4j.wasm`.
3. Runs the Java build, Javadoc, sources JAR, signing, and Maven publication into `build/central-staging`.
4. Builds `minify4j-0.1.0.jar`, `minify4j-0.1.0-sources.jar`, and `minify4j-0.1.0-javadoc.jar`.
5. Creates `build/central-portal/central-bundle.zip`.

## Manual Build

Build WASM:

```bash
./scripts/build-wasm.sh
```

Build Java and run tests:

```bash
./gradlew clean build
```

Build the standalone JAR:

```bash
./gradlew fatJar
```

Create the Central Portal bundle:

```bash
./gradlew centralPortalBundle
```

## Upload To Central Portal

Generate a Central Portal user token, then run:

```bash
export CENTRAL_PORTAL_USERNAME="your-token-username"
export CENTRAL_PORTAL_PASSWORD="your-token-password"
export SIGNING_KEY="your-ascii-armored-private-key"
export SIGNING_PASSWORD="your-signing-key-password"

./gradlew uploadCentralPortalBundle
```

The upload task sends `build/central-portal/central-bundle.zip` to
`https://central.sonatype.com/api/v1/publisher/upload` with
`publishingType=USER_MANAGED` by default.

To request automatic publishing after validation:

```bash
export CENTRAL_PUBLISHING_TYPE="AUTOMATIC"
./gradlew uploadCentralPortalBundle
```

You can also pass credentials as Gradle properties:

```bash
./gradlew uploadCentralPortalBundle \
  -PcentralPortalUsername="your-token-username" \
  -PcentralPortalPassword="your-token-password" \
  -PsigningKey="your-ascii-armored-private-key" \
  -PsigningPassword="your-signing-key-password"
```

## Pre-Release Checklist

- `rustup target add wasm32-unknown-unknown` has been run on the release machine.
- `./scripts/build-wasm.sh` passes.
- `./gradlew clean build fatJar` passes.
- `./gradlew centralPortalBundle` creates `build/central-portal/central-bundle.zip`.
- `Minify4jTest` passes.
- `build/libs/minify4j-0.1.0.jar` contains `wasm/minify4j.wasm`.
- README, CHANGELOG, and docs are updated.
- Version in `build.gradle` is correct.
