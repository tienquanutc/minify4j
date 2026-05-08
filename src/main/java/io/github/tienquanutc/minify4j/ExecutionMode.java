package io.github.tienquanutc.minify4j;

/**
 * Selects how Chicory executes the bundled WebAssembly minifier.
 */
public enum ExecutionMode {
    /**
     * Compile WebAssembly to JVM bytecode at runtime before execution.
     */
    RUNTIME_COMPILER,

    /**
     * Execute WebAssembly through Chicory's interpreter without runtime bytecode generation.
     */
    INTERPRETER
}
