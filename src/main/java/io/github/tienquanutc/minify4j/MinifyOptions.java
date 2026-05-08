package io.github.tienquanutc.minify4j;

/**
 * Configuration used when creating a {@link Minify4j} instance.
 */
public final class MinifyOptions {
    private final ExecutionMode executionMode;
    private final boolean interpreterFallbackEnabled;

    private MinifyOptions(Builder builder) {
        this.executionMode = builder.executionMode;
        this.interpreterFallbackEnabled = builder.interpreterFallbackEnabled;
    }

    /**
     * Returns the default minifier options.
     *
     * @return default options
     */
    public static MinifyOptions defaults() {
        return builder().build();
    }

    /**
     * Creates a builder for minifier options.
     *
     * @return a new options builder
     */
    public static Builder builder() {
        return new Builder();
    }

    ExecutionMode executionMode() {
        return executionMode;
    }

    boolean interpreterFallbackEnabled() {
        return interpreterFallbackEnabled;
    }

    /**
     * Builder for {@link MinifyOptions}.
     */
    public static final class Builder {
        private ExecutionMode executionMode = ExecutionMode.RUNTIME_COMPILER;
        private boolean interpreterFallbackEnabled = true;

        private Builder() {
        }

        /**
         * Selects the Chicory execution mode.
         *
         * @param executionMode execution mode to use
         * @return this builder
         */
        public Builder executionMode(ExecutionMode executionMode) {
            if (executionMode == null) {
                throw new IllegalArgumentException("executionMode must not be null");
            }
            this.executionMode = executionMode;
            return this;
        }

        /**
         * Enables or disables interpreter fallback for runtime compiler mode.
         *
         * @param interpreterFallbackEnabled true to allow fallback when runtime compilation cannot compile a function
         * @return this builder
         */
        public Builder interpreterFallbackEnabled(boolean interpreterFallbackEnabled) {
            this.interpreterFallbackEnabled = interpreterFallbackEnabled;
            return this;
        }

        /**
         * Builds immutable minifier options.
         *
         * @return minifier options
         */
        public MinifyOptions build() {
            return new MinifyOptions(this);
        }
    }
}
