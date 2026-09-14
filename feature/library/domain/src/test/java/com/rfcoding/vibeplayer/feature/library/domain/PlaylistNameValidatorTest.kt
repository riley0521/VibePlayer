package com.rfcoding.vibeplayer.feature.library.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.core.domain.util.Result
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PlaylistNameValidatorTest {

    @Test
    fun `a valid name comes back trimmed`() {
        assertThat(PlaylistNameValidator.validate("  Friday Chill \n")).isEqualTo(Result.Success("Friday Chill"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "   \t\n"])
    fun `an empty or whitespace-only name is blank`(name: String) {
        assertThat(PlaylistNameValidator.validate(name)).isEqualTo(Result.Error(PlaylistNameError.BLANK))
    }

    @Test
    fun `a name of exactly 40 characters is accepted`() {
        val name = "a".repeat(40)

        assertThat(PlaylistNameValidator.validate(name)).isEqualTo(Result.Success(name))
    }

    @Test
    fun `a name of 41 characters is too long`() {
        assertThat(PlaylistNameValidator.validate("a".repeat(41))).isEqualTo(Result.Error(PlaylistNameError.TOO_LONG))
    }

    @Test
    fun `surrounding spaces don't count towards the limit`() {
        val name = "a".repeat(40)

        assertThat(PlaylistNameValidator.validate("  $name  ")).isEqualTo(Result.Success(name))
    }
}
