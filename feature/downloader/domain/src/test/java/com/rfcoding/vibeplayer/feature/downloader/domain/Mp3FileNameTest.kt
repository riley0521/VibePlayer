package com.rfcoding.vibeplayer.feature.downloader.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThanOrEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

class Mp3FileNameTest {

    @Test
    fun `a free title becomes title dot mp3`() {
        assertThat(mp3FileName("Midnight Drive", "Owls", "id") { false }).isEqualTo("Midnight Drive.mp3")
    }

    @Test
    fun `a taken title gets the artist appended`() {
        val taken = setOf("Intro.mp3")

        assertThat(mp3FileName("Intro", "Band B", "id") { it in taken }).isEqualTo("Intro - Band B.mp3")
    }

    @Test
    fun `when title and artist are both taken a counter is added`() {
        val taken = setOf("Intro.mp3", "Intro - Band.mp3", "Intro (1).mp3")

        assertThat(mp3FileName("Intro", "Band", "id") { it in taken }).isEqualTo("Intro (2).mp3")
    }

    @Test
    fun `a taken title without an artist goes straight to the counter`() {
        val taken = setOf("Intro.mp3")

        assertThat(mp3FileName("Intro", null, "id") { it in taken }).isEqualTo("Intro (1).mp3")
    }

    @Test
    fun `characters shared storage rejects are removed`() {
        assertThat(mp3FileName("AC/DC: Back?  In <Black>|*", "A/B", "id") { false })
            .isEqualTo("ACDC Back In Black.mp3")
        assertThat(mp3FileName("Song", "A/B", "id") { it == "Song.mp3" }).isEqualTo("Song - AB.mp3")
    }

    @Test
    fun `a title with nothing usable falls back to the given name`() {
        assertThat(mp3FileName("???", null, "dQw4w9WgXcQ") { false }).isEqualTo("dQw4w9WgXcQ.mp3")
    }

    @Test
    fun `long titles are capped`() {
        val name = mp3FileName("a".repeat(500), "b".repeat(500), "id") { it.length < 200 && it.startsWith("a") && !it.contains("(") }

        assertThat(name.length).isLessThanOrEqualTo(130)
    }

    @Test
    fun `sanitizing leading dots and blanks leaves nothing`() {
        assertThat(sanitizeFileName(" .. ")).isNull()
        assertThat(sanitizeFileName("..hidden")).isEqualTo("hidden")
    }
}
