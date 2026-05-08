package io.github.tienquanutc.minify4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minify4jTest {
    @Test
    void minifiesCssWithStaticFacade() {
        assertEquals(".foo{color:red;margin:0}", Minify4j.getDefaultInstance().minifyCss(".foo { color: red; margin: 0; }"));
    }

    @Test
    void minifiesJavaScriptWithStaticFacade() {
        assertEquals("function add(e,t){return e+t}", Minify4j.getDefaultInstance().minifyJs("function add(first, second) { return first + second; }"));
    }

    @Test
    void minifiesCss() {
        Minify4j minifier = Minify4j.create();

        assertEquals(".foo{color:red;margin:0}", minifier.minifyCss(".foo { color: red; margin: 0; }"));
    }

    @Test
    void minifiesJavaScript() {
        Minify4j minifier = Minify4j.create();

        assertEquals("function add(e,t){return e+t}", minifier.minifyJs("function add(first, second) { return first + second; }"));
    }

    @Test
    void reportsMinifierErrors() {
        Minify4j minifier = Minify4j.create();

        assertThrows(Minify4jException.class, () -> minifier.minifyCss(""));
    }

    @Test
    void createsWithOptionsWithoutExposingWasmResource() {
        MinifyOptions options = MinifyOptions.builder()
            .executionMode(ExecutionMode.RUNTIME_COMPILER)
            .interpreterFallbackEnabled(true)
            .build();

        Minify4j minifier = Minify4j.create(options);

        assertEquals(".foo{color:red}", minifier.minifyCss(".foo { color: red; }"));
    }

    @Test
    void minifiesWithInterpreterMode() {
        MinifyOptions options = MinifyOptions.builder()
            .executionMode(ExecutionMode.INTERPRETER)
            .build();

        Minify4j minifier = Minify4j.create(options);

        assertEquals(".foo{color:red}", minifier.minifyCss(".foo { color: red; }"));
    }

    @Test
    void rejectsNullExecutionMode() {
        assertThrows(IllegalArgumentException.class, () -> MinifyOptions.builder().executionMode(null));
    }
}
