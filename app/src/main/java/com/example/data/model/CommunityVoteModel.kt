package com.example.data.model

data class VotedTrack(
    val id: String,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val coverUrl: String,
    val uploadedBy: String = "Comunidad Arkaios",
    val votesCount: Int = 0,
    val userHasVoted: Boolean = false,
    val isOriginalMelody: Boolean = false
)

data class UserListeningStatus(
    val id: String,
    val username: String,
    val avatarUrl: String,
    val songTitle: String,
    val artistName: String,
    val platform: String = "Spotify-Arkaios 100GB",
    val timestampText: String = "Escuchando ahora"
)
