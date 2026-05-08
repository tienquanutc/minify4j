package io.github.tienquanutc.minify4j;

import com.dylibso.chicory.compiler.InterpreterFallback;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Machine;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Java facade for CSS and JavaScript minification backed by the bundled Rust WebAssembly engine.
 */
public final class Minify4j {
    private static final String DEFAULT_WASM_RESOURCE = "wasm/minify4j.wasm";
    private static final String TEMP_WASM_FILE_PREFIX = "minify4j";
    private static final String TEMP_WASM_FILE_SUFFIX = ".wasm";

    private static final String EXPORT_FUNC_ALLOC = "alloc";
    private static final String EXPORT_FUNC_DEALLOC = "dealloc";
    private static final String EXPORT_FUNC_MINIFY_CSS = "minify_css";
    private static final String EXPORT_FUNC_MINIFY_JS = "minify_js";
    private static final String EXPORT_FUNC_LAST_ERROR_MESSAGE = "last_error_message";

    private static final String WASM_ERROR_UNKNOWN = "Unknown WASM minifier error";
    private static final String WASM_ERROR_ALLOC_FAILED = "WASM alloc failed for input length ";

    private static final int POINTER_LENGTH_SHIFT_BITS = 32;
    private static final int POINTER_WASM_NULL = 0;
    private static final int MIN_WASM_ALLOCATION_LENGTH = 1;

    private final Memory memory;
    private final ExportFunction allocFunc;
    private final ExportFunction deallocFunc;
    private final ExportFunction minifyCssFunc;
    private final ExportFunction minifyJsFunc;
    private final ExportFunction lastErrorMessageFunc;

    private Minify4j(File wasmFile, MinifyOptions options) {
        WasmModule module = Parser.parse(wasmFile);
        Instance.Builder runtimeBuilder = Instance.builder(module);

        if (options.executionMode() == ExecutionMode.RUNTIME_COMPILER) {
            runtimeBuilder.withMachineFactory(compileMachineFactory(module, options));
        }

        Instance runtimeInstance = runtimeBuilder.build();
        this.memory = runtimeInstance.memory();
        this.allocFunc = runtimeInstance.export(EXPORT_FUNC_ALLOC);
        this.deallocFunc = runtimeInstance.export(EXPORT_FUNC_DEALLOC);
        this.minifyCssFunc = runtimeInstance.export(EXPORT_FUNC_MINIFY_CSS);
        this.minifyJsFunc = runtimeInstance.export(EXPORT_FUNC_MINIFY_JS);
        this.lastErrorMessageFunc = runtimeInstance.export(EXPORT_FUNC_LAST_ERROR_MESSAGE);
    }

    private static Function<Instance, Machine> compileMachineFactory(WasmModule module, MinifyOptions options) {
        return MachineFactoryCompiler.builder(module)
            .withInterpreterFallback(options.interpreterFallbackEnabled() ? InterpreterFallback.SILENT : InterpreterFallback.FAIL)
            .compile();
    }

    /**
     * Creates a minifier with default options.
     *
     * @return a new minifier instance
     */
    public static Minify4j create() {
        return create(MinifyOptions.defaults());
    }

    /**
     * Creates a minifier with explicit options.
     *
     * @param options minifier runtime options
     * @return a new minifier instance
     */
    public static Minify4j create(MinifyOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
        return fromClasspath(options);
    }

    /**
     * Returns the shared default minifier instance.
     *
     * @return the shared default minifier
     */
    public static Minify4j getDefaultInstance() {
        return DefaultMinifier.INSTANCE;
    }

    private static Minify4j fromClasspath(MinifyOptions options) {
        try (InputStream in = Minify4j.class.getClassLoader().getResourceAsStream(Minify4j.DEFAULT_WASM_RESOURCE)) {
            if (in == null) {
                throw new Minify4jException("Missing resource: " + Minify4j.DEFAULT_WASM_RESOURCE + ". Build Rust WASM first.");
            }
            Path temp = Files.createTempFile(TEMP_WASM_FILE_PREFIX, TEMP_WASM_FILE_SUFFIX);
            temp.toFile().deleteOnExit();
            Files.copy(in, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return new Minify4j(temp.toFile(), options);
        } catch (IOException e) {
            throw new Minify4jException("Failed to load WASM resource: " + Minify4j.DEFAULT_WASM_RESOURCE, e);
        }
    }

    /**
     * Minifies CSS source text.
     *
     * @param css CSS source text
     * @return minified CSS
     */
    public synchronized String minifyCss(String css) {
        return minifyWith(css, minifyCssFunc);
    }

    /**
     * Minifies JavaScript source text.
     *
     * @param js JavaScript source text
     * @return minified JavaScript
     */
    public synchronized String minifyJs(String js) {
        return minifyWith(js, minifyJsFunc);
    }

    private String minifyWith(String source, ExportFunction minifier) {
        byte[] input = source.getBytes(StandardCharsets.UTF_8);
        int inputPtr = 0;

        try {
            inputPtr = (int) allocFunc.apply(input.length)[0];
            if (inputPtr == POINTER_WASM_NULL) {
                throw new Minify4jException(WASM_ERROR_ALLOC_FAILED + input.length);
            }

            memory.write(inputPtr, input);

            long packed = minifier.apply(inputPtr, input.length)[0];
            if (packed == POINTER_WASM_NULL) {
                throw new Minify4jException(readLastError());
            }

            int outPtr = (int) (packed >>> POINTER_LENGTH_SHIFT_BITS);
            int outLen = (int) packed;
            String result = memory.readString(outPtr, outLen);
            deallocFunc.apply(outPtr, Math.max(outLen, MIN_WASM_ALLOCATION_LENGTH));
            return result;
        } finally {
            if (inputPtr != POINTER_WASM_NULL) {
                deallocFunc.apply(inputPtr, input.length);
            }
        }
    }

    private String readLastError() {
        long packed = lastErrorMessageFunc.apply()[0];
        int ptr = (int) (packed >>> POINTER_LENGTH_SHIFT_BITS);
        int len = (int) packed;
        if (ptr == POINTER_WASM_NULL || len == 0) {
            return WASM_ERROR_UNKNOWN;
        }
        return memory.readString(ptr, len);
    }

    private static final class DefaultMinifier {
        private static final Minify4j INSTANCE = Minify4j.create();
    }
}
