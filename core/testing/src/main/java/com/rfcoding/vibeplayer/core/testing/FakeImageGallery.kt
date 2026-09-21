package com.rfcoding.vibeplayer.core.testing

import com.rfcoding.vibeplayer.core.domain.image.ImageGallery
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result

class FakeImageGallery : ImageGallery {
    /** Set to make [savePng] fail; the attempt is still recorded in [savedNames]. */
    var error: DataError.Local? = null

    /** Every display name passed to [savePng], in call order. */
    val savedNames = mutableListOf<String>()

    override suspend fun savePng(bytes: ByteArray, displayName: String): EmptyResult<DataError.Local> {
        savedNames += displayName
        error?.let { return Result.Error(it) }
        return Result.Success(Unit)
    }
}
