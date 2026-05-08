# WASM ABI

The Java library talks to `minify4j-wasm` through a small manual ABI. This is
not WIT and not the WebAssembly component model.

## Exported Functions

```text
alloc(len: i32) -> i32
dealloc(ptr: i32, len: i32) -> void
minify_css(ptr: i32, len: i32) -> i64
minify_js(ptr: i32, len: i32) -> i64
last_error_message() -> i64
```

The Java side resolves these exports in `Minify4j` and calls them through
Chicory `ExportFunction` instances.

## Packed Return Value

`minify_css`, `minify_js`, and `last_error_message` return a packed `i64`:

```text
high 32 bits = pointer
low 32 bits  = length
```

Java unpacking:

```java
int ptr = (int) (packed >>> 32);
int len = (int) packed;
```

A packed value of `0` from `minify_css` or `minify_js` means failure.

## Memory Ownership

Input ownership:

- Java calls `alloc(inputLen)`.
- Java writes UTF-8 input bytes into WASM linear memory.
- Java calls `dealloc(inputPtr, inputLen)` in a `finally` block.

Output ownership:

- Rust allocates the output buffer and returns a packed pointer/length.
- Java reads the output bytes from WASM memory.
- Java calls `dealloc(outPtr, max(outLen, 1))`.

Error ownership:

- Rust stores the last error message internally.
- Java reads it through `last_error_message()`.
- Java must not free the last-error buffer.
- Rust frees and replaces the previous last-error buffer when a new error is stored.

## Error Semantics

`minify_css` and `minify_js` return `0` on failure.

On failure:

1. Rust writes a human-readable message into last-error storage.
2. Java calls `last_error_message()`.
3. Java reads the returned pointer/length.
4. Java throws `Minify4jException`.

If no error message is available, Java reports `Unknown WASM minifier error`.

## Input Constraints

- Inputs are encoded as UTF-8 by Java.
- Rust rejects `ptr == 0` or `len == 0` as `empty input`.
- Rust rejects invalid UTF-8.
- CSS parsing and printing are handled by `lightningcss`.
- JavaScript parsing, minification, mangling, and code generation are handled by Oxc.

## Compatibility Rules

Keep these stable across compatible releases:

- Exported function names.
- Pointer/length packing format.
- `0` as the minifier error return.
- Classpath resource name `wasm/minify4j.wasm`.
- Java package `io.github.tienquanutc.minify4j`.

Any incompatible ABI change should be documented in `CHANGELOG.md` and released
with an appropriate version change.
