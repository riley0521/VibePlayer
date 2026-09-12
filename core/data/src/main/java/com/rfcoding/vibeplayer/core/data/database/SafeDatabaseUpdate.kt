package com.rfcoding.vibeplayer.core.data.database

import android.database.sqlite.SQLiteFullException
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Wraps a database write; reads are called directly. */
suspend inline fun <T> safeDatabaseUpdate(update: suspend () -> T): Result<T, DataError.Local> {
    return try {
        Result.Success(update())
    } catch (_: SQLiteFullException) {
        Result.Error(DataError.Local.DISK_FULL)
    } catch (_: Exception) {
        currentCoroutineContext().ensureActive()
        Result.Error(DataError.Local.UNKNOWN)
    }
}
