package com.rfcoding.vibeplayer.feature.library.data.scanner

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class MusicFileRulesTest {

    @Test
    fun `only mp3 file names count, in any case`() {
        assertThat(isMp3("song.mp3")).isTrue()
        assertThat(isMp3("SONG.MP3")).isTrue()
        assertThat(isMp3("song.m4a")).isFalse()
        assertThat(isMp3("song.mp3.txt")).isFalse()
    }

    @Test
    fun `relative path matches only the Music folder itself`() {
        assertThat(isDirectlyInMusicFolder(relativePath = "Music/")).isTrue()
        assertThat(isDirectlyInMusicFolder(relativePath = "music/")).isTrue()
        assertThat(isDirectlyInMusicFolder(relativePath = "Music/Rock/")).isFalse()
        assertThat(isDirectlyInMusicFolder(relativePath = "Download/")).isFalse()
        assertThat(isDirectlyInMusicFolder(relativePath = "Download/Music/")).isFalse()
        assertThat(isDirectlyInMusicFolder(relativePath = null)).isFalse()
    }

    @Test
    fun `file path matches only files whose parent is the Music folder`() {
        val musicDir = "/storage/emulated/0/Music"

        assertThat(isDirectlyInMusicFolder("$musicDir/song.mp3", musicDir)).isTrue()
        assertThat(isDirectlyInMusicFolder("$musicDir/song.mp3", "$musicDir/")).isTrue()
        assertThat(isDirectlyInMusicFolder("/storage/emulated/0/music/song.mp3", musicDir)).isTrue()
        assertThat(isDirectlyInMusicFolder("$musicDir/Rock/song.mp3", musicDir)).isFalse()
        assertThat(isDirectlyInMusicFolder("/storage/emulated/0/Download/song.mp3", musicDir)).isFalse()
        assertThat(isDirectlyInMusicFolder("${musicDir}Backup/song.mp3", musicDir)).isFalse()
    }
}
