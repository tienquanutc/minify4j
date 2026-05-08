use std::alloc::{alloc as raw_alloc, dealloc as raw_dealloc, Layout};
use std::ptr;
use std::slice;
use std::str;

pub const NULL_PTR: u32 = 0;
pub const EMPTY_BUFFER_ALLOCATION_LEN: u32 = 1;
pub const PTR_LEN_SHIFT_BITS: u64 = 32;

const ERROR_EMPTY_INPUT: &str = "empty input";
const ERROR_INVALID_UTF8: &str = "input is not valid UTF-8";

pub fn pack_ptr_len(ptr: u32, len: u32) -> u64 {
    ((ptr as u64) << PTR_LEN_SHIFT_BITS) | (len as u64)
}

pub unsafe fn write_buffer(bytes: &[u8]) -> u64 {
    if bytes.is_empty() {
        let ptr = alloc_buffer(EMPTY_BUFFER_ALLOCATION_LEN);
        if ptr == NULL_PTR {
            return NULL_PTR as u64;
        }
        return pack_ptr_len(ptr, NULL_PTR);
    }

    let ptr = alloc_buffer(bytes.len() as u32);
    if ptr == NULL_PTR {
        return NULL_PTR as u64;
    }

    ptr::copy_nonoverlapping(bytes.as_ptr(), ptr as *mut u8, bytes.len());
    pack_ptr_len(ptr, bytes.len() as u32)
}

pub unsafe fn read_utf8_input<'a>(ptr: u32, len: u32) -> Result<&'a str, String> {
    if ptr == NULL_PTR || len == 0 {
        return Err(ERROR_EMPTY_INPUT.to_string());
    }

    let bytes = slice::from_raw_parts(ptr as *const u8, len as usize);
    str::from_utf8(bytes).map_err(|e| format!("{ERROR_INVALID_UTF8}: {e}"))
}

pub fn alloc_buffer(len: u32) -> u32 {
    if len == 0 {
        return NULL_PTR;
    }

    let layout = match Layout::array::<u8>(len as usize) {
        Ok(layout) => layout,
        Err(_) => return NULL_PTR,
    };

    unsafe {
        let ptr = raw_alloc(layout);
        if ptr.is_null() {
            NULL_PTR
        } else {
            ptr as u32
        }
    }
}

pub fn dealloc_buffer(ptr: u32, len: u32) {
    if ptr == NULL_PTR || len == 0 {
        return;
    }

    let layout = match Layout::array::<u8>(len as usize) {
        Ok(layout) => layout,
        Err(_) => return,
    };

    unsafe {
        raw_dealloc(ptr as *mut u8, layout);
    }
}
