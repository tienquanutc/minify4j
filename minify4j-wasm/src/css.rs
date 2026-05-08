use lightningcss::printer::PrinterOptions;
use lightningcss::stylesheet::{ParserOptions, StyleSheet};

pub fn minify_css_source(source_text: &str) -> Result<String, String> {
    let stylesheet = StyleSheet::parse(source_text, ParserOptions::default())
        .map_err(|e| format!("failed to parse CSS: {e}"))?;

    stylesheet
        .to_css(PrinterOptions {
            minify: true,
            ..PrinterOptions::default()
        })
        .map(|output| output.code)
        .map_err(|e| format!("failed to print CSS: {e}"))
}
