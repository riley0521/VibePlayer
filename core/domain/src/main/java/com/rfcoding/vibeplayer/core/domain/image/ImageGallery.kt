package com.rfcoding.vibeplayer.core.domain.image

import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult

/** The device's shared picture gallery, where images the user exports from the app are saved. */
interface ImageGallery {
    /** Saves an encoded PNG as [displayName] (e.g. `VibePlayer_505_1700000000000.png`). */
    suspend fun savePng(bytes: ByteArray, displayName: String): EmptyResult<DataError.Local>
}
