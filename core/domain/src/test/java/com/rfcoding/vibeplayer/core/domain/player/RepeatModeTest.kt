package com.rfcoding.vibeplayer.core.domain.player

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class RepeatModeTest {

    @Test
    fun `the repeat button cycles off, all, one and back to off`() {
        assertThat(RepeatMode.Off.next()).isEqualTo(RepeatMode.All)
        assertThat(RepeatMode.All.next()).isEqualTo(RepeatMode.One)
        assertThat(RepeatMode.One.next()).isEqualTo(RepeatMode.Off)
    }
}
