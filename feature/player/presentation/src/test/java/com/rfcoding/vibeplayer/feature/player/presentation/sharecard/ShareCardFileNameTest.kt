package com.rfcoding.vibeplayer.feature.player.presentation.sharecard

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThanOrEqualTo
import org.junit.jupiter.api.Test

class ShareCardFileNameTest {

    @Test
    fun `name has the app prefix, the title, the timestamp and the png extension`() {
        assertThat(shareCardFileName("505", 1_700_000_000_000)).isEqualTo("VibePlayer_505_1700000000000.png")
    }

    @Test
    fun `characters that file systems reject become underscores`() {
        assertThat(shareCardFileName("AC/DC: Who?", 1)).isEqualTo("VibePlayer_AC_DC_ Who__1.png")
        assertThat(shareCardFileName("<a|b\"c*d\\e>", 1)).isEqualTo("VibePlayer__a_b_c_d_e__1.png")
    }

    @Test
    fun `runs of whitespace collapse and the ends are trimmed`() {
        assertThat(shareCardFileName("  Do   I\tWanna\nKnow  ", 1)).isEqualTo("VibePlayer_Do I Wanna Know_1.png")
    }

    @Test
    fun `long titles are clamped`() {
        val name = shareCardFileName("a".repeat(500), 1)

        assertThat(name.length).isLessThanOrEqualTo("VibePlayer__1.png".length + 60)
    }

    @Test
    fun `a blank title falls back to Song`() {
        assertThat(shareCardFileName("   ", 1)).isEqualTo("VibePlayer_Song_1.png")
    }
}
