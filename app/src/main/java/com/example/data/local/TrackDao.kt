package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM cached_tracks ORDER BY addedTimestamp DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM cached_tracks WHERE isDownloaded = 1 ORDER BY addedTimestamp DESC")
    fun getDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM cached_tracks WHERE isFavorite = 1 ORDER BY addedTimestamp DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM cached_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE cached_tracks SET isDownloaded = :downloaded, localFilePath = :path WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, downloaded: Boolean, path: String?)

    @Query("UPDATE cached_tracks SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, favorite: Boolean)

    @Query("DELETE FROM cached_tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdTimestamp DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackToPlaylist(crossRef: PlaylistTrackCrossRefEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)

    @Query("""
        SELECT t.* FROM cached_tracks t 
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId 
        WHERE pt.playlistId = :playlistId 
        ORDER BY pt.addedTimestamp ASC
    """)
    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>>

    @Query("SELECT playlistId FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun getPlaylistsContainingTrack(trackId: String): List<String>
}
