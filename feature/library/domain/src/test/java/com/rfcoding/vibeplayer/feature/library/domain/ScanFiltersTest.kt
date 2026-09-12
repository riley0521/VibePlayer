package com.rfcoding.vibeplayer.feature.library.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ScanFiltersTest {

    @Test
    fun `defaults are 30 seconds and 100KB`() {
        assertThat(ScanFilters()).isEqualTo(
            ScanFilters(minDuration = MinDuration.ThirtySeconds, minSize = MinSize.OneHundredKb),
        )
    }

    @ParameterizedTest
    @EnumSource(MinDuration::class)
    fun `a duration exactly at the minimum is accepted and one below is not`(minDuration: MinDuration) {
        val filters = ScanFilters(minDuration = minDuration)

        assertThat(filters.acceptsDuration(minDuration.millis)).isTrue()
        assertThat(filters.acceptsDuration(minDuration.millis - 1)).isFalse()
    }

    @ParameterizedTest
    @EnumSource(MinSize::class)
    fun `a size exactly at the minimum is accepted and one byte below is not`(minSize: MinSize) {
        val filters = ScanFilters(minSize = minSize)

        assertThat(filters.acceptsSize(minSize.bytes)).isTrue()
        assertThat(filters.acceptsSize(minSize.bytes - 1)).isFalse()
    }
}
