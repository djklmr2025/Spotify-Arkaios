package com.example.data.model

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val tracks: List<Track> = emptyList(),
    val isSystemPlaylist: Boolean = false,
    val author: String = "Arkaios Tify",
    val followersCount: Int = 1240
)
