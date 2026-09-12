package com.rfcoding.vibeplayer.core.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class DurationFormatterTest {

    @Test
    fun `formats zero as 0 00`() {
        assertThat(0L.toDurationText()).isEqualTo("0:00")
    }

    @Test
    fun `treats a negative duration as zero`() {
        assertThat((-5_000L).toDurationText()).isEqualTo("0:00")
    }

    @Test
    fun `pads seconds below ten`() {
        assertThat(65_000L.toDurationText()).isEqualTo("1:05")
    }

    @Test
    fun `drops the milliseconds remainder`() {
        assertThat(225_900L.toDurationText()).isEqualTo("3:45")
    }

    @Test
    fun `formats an exact minute`() {
        assertThat(120_000L.toDurationText()).isEqualTo("2:00")
    }

    @Test
    fun `formats a duration over ten minutes`() {
        assertThat(725_000L.toDurationText()).isEqualTo("12:05")
    }

    @Test
    fun `adds an hours part past an hour`() {
        assertThat(3_723_000L.toDurationText()).isEqualTo("1:02:03")
    }

    @Test
    fun `pads minutes in the hours format`() {
        assertThat(3_600_000L.toDurationText()).isEqualTo("1:00:00")
    }
}
