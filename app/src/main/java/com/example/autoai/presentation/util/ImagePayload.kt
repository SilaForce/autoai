package com.example.autoai.presentation.util

import androidx.compose.runtime.Immutable

/**
 * Stable wrapper around a JPEG-compressed byte array. Used by Compose state in the chat
 * and edit-profile features.
 *
 * Why: `ByteArray.equals` is reference equality, so a `List<ByteArray>` in `@Stable` /
 * `@Immutable` Compose state lies — two equal-content lists never compare `==`, and every
 * `setState { copy(selectedImages = ...) }` forces unnecessary recomposition. Wrapping in
 * a class with content-based equality fixes both the stability contract and incidental
 * over-recomposition.
 */
@Immutable
class ImagePayload(val bytes: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImagePayload) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}
