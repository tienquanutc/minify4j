mod abi;
mod css;
mod error;
mod js;

use crate::abi::{alloc_buffer, dealloc_buffer, read_utf8_input, write_buffer, NULL_PTR};
use crate::css::minify_css_source;
use crate::error::set_error;
use crate::js::minify_js_source;

/// Allocate bytes inside wasm linear memory.
/// Java writes input bytes into the returned pointer.
#[no_mangle]
pub extern "C" fn alloc(len: u32) -> u32 {
    alloc_buffer(len)
}

/// Free bytes previously allocated by `alloc`.
#[no_mangle]
pub extern "C" fn dealloc(ptr: u32, len: u32) {
    dealloc_buffer(ptr, len);
}

/// Minify CSS.
///
/// Input: UTF-8 bytes at `(ptr, len)`.
/// Return: packed `(out_ptr, out_len)` as u64.
/// Return 0 on error. Call `last_error_message` to inspect the error.
#[no_mangle]
pub extern "C" fn minify_css(ptr: u32, len: u32) -> u64 {
    unsafe {
        let css = match read_utf8_input(ptr, len) {
            Ok(input) => input,
            Err(e) => {
                set_error(e);
                return NULL_PTR as u64;
            }
        };

        match minify_css_source(css) {
            Ok(output) => write_buffer(output.as_bytes()),
            Err(e) => {
                set_error(e);
                NULL_PTR as u64
            }
        }
    }
}

/// Minify JavaScript using Oxc.
///
/// Input: UTF-8 bytes at `(ptr, len)`.
/// Return: packed `(out_ptr, out_len)` as u64.
/// Return 0 on error. Call `last_error_message` to inspect the error.
#[no_mangle]
pub extern "C" fn minify_js(ptr: u32, len: u32) -> u64 {
    unsafe {
        let js = match read_utf8_input(ptr, len) {
            Ok(input) => input,
            Err(e) => {
                set_error(e);
                return NULL_PTR as u64;
            }
        };

        match minify_js_source(js) {
            Ok(output) => write_buffer(output.as_bytes()),
            Err(e) => {
                set_error(e);
                NULL_PTR as u64
            }
        }
    }
}

/// Return packed `(ptr, len)` for the last error message.
/// Java should read it but does not need to free it; Rust reuses/frees it on next error.
#[no_mangle]
pub extern "C" fn last_error_message() -> u64 {
    unsafe { error::last_error_message() }
}
