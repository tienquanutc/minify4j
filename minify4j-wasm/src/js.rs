use oxc_allocator::Allocator;
use oxc_codegen::{Codegen, CodegenOptions, CommentOptions};
use oxc_mangler::MangleOptions;
use oxc_minifier::{CompressOptions, Minifier, MinifierOptions};
use oxc_parser::Parser;
use oxc_span::SourceType;

const ERROR_JS_PARSE: &str = "failed to parse JavaScript";

pub fn minify_js_source(source_text: &str) -> Result<String, String> {
    let allocator = Allocator::default();
    let source_type = SourceType::unambiguous();
    let ret = Parser::new(&allocator, source_text, source_type).parse();

    if !ret.errors.is_empty() {
        return Err(format!("{ERROR_JS_PARSE}: {:?}", ret.errors));
    }

    let mut program = ret.program;
    let options = MinifierOptions {
        mangle: Some(MangleOptions::default()),
        compress: Some(CompressOptions::smallest()),
    };
    let minifier_return = Minifier::new(options).minify(&allocator, &mut program);

    Ok(Codegen::new()
        .with_options(CodegenOptions {
            minify: true,
            comments: CommentOptions::disabled(),
            ..CodegenOptions::default()
        })
        .with_scoping(minifier_return.scoping)
        .build(&program)
        .code)
}
