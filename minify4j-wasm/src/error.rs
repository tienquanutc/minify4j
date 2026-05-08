use crate::abi::{dealloc_buffer, pack_ptr_len, write_buffer, NULL_PTR, PTR_LEN_SHIFT_BITS};

static mut LAST_ERROR_PTR: u32 = 0;
static mut LAST_ERROR_LEN: u32 = 0;

pub unsafe fn set_error(message: String) {
    if LAST_ERROR_PTR != NULL_PTR && LAST_ERROR_LEN != 0 {
        dealloc_buffer(LAST_ERROR_PTR, LAST_ERROR_LEN);
    }

    let bytes = message.into_bytes();
    let packed = write_buffer(&bytes);

    if packed == NULL_PTR as u64 {
        LAST_ERROR_PTR = NULL_PTR;
        LAST_ERROR_LEN = 0;
        return;
    }

    LAST_ERROR_PTR = (packed >> PTR_LEN_SHIFT_BITS) as u32;
    LAST_ERROR_LEN = packed as u32;
}

pub unsafe fn last_error_message() -> u64 {
    pack_ptr_len(LAST_ERROR_PTR, LAST_ERROR_LEN)
}
