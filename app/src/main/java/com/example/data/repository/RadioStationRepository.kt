package com.example.data.repository

import com.example.data.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RadioStationRepository {

    private val defaultStations = listOf(
        RadioStation(
            id = "jango_latin_top100",
            name = "Latin Top 100",
            genre = "Reggaeton & Pop Latino",
            streamUrl = "https://stream.zeno.fm/f3wvbbqmdg8uv",
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
            description = "Los éxitos más calientes de la música urbana y pop latino 24/7",
            listenersCount = "48.2k sintonizados"
        ),
        RadioStation(
            id = "jango_classic_jazz",
            name = "Classic Jazz Masters",
            genre = "Jazz & Soul",
            streamUrl = "https://stream.zeno.fm/7xup0051m7zuv",
            coverUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=600&auto=format&fit=crop&q=80",
            description = "Elegancia pura con saxofón, piano y los grandes maestros del jazz",
            listenersCount = "32.1k sintonizados"
        ),
        RadioStation(
            id = "jango_rock_legends",
            name = "Rock Legends & Classic Hits",
            genre = "Rock Clásico",
            streamUrl = "https://stream.zeno.fm/0r0xa792kwzuv",
            coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=600&auto=format&fit=crop&q=80",
            description = "Lo mejor del rock de los 70s, 80s y 90s sin interrupciones",
            listenersCount = "64.9k sintonizados"
        ),
        RadioStation(
            id = "jango_hot_country",
            name = "Hot Country Nashville",
            genre = "Country & Folk",
            streamUrl = "https://stream.zeno.fm/wr2y8w04m7zuv",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            description = "Guitarras acústicas, historias y el auténtico sonido country",
            listenersCount = "19.5k sintonizados"
        ),
        RadioStation(
            id = "jango_synthwave_80s",
            name = "Cyberpunk & Synthwave 80s",
            genre = "Synthwave / Retro",
            streamUrl = "https://stream.zeno.fm/f1400bc031zuv",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            description = "Vibras retro-futuristas, sintetizadores analógicos y luces de neón",
            listenersCount = "28.7k sintonizados"
        ),
        RadioStation(
            id = "jango_latin_workout",
            name = "Latin Workout & Energy",
            genre = "Fitness & Dance",
            streamUrl = "https://stream.zeno.fm/t3qsm9kmdg8uv",
            coverUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80",
            description = "Ritmo imparable y alta adrenalina para tus sesiones de entrenamiento",
            listenersCount = "22.3k sintonizados"
        ),
        RadioStation(
            id = "jango_lofi_lounge",
            name = "Chillhop & Lo-Fi Lounge",
            genre = "Lo-Fi / Relax",
            streamUrl = "https://stream.zeno.fm/038xwt92kwzuv",
            coverUrl = "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=600&auto=format&fit=crop&q=80",
            description = "Beats relajantes 24/7 para estudiar, trabajar o relajarse",
            listenersCount = "51.8k sintonizados"
        ),
        RadioStation(
            id = "jango_world_music",
            name = "World Music & Ethnic Beats",
            genre = "World Music",
            streamUrl = "https://stream.zeno.fm/v2k031a0kwzuv",
            coverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
            description = "Sonidos globales, afrobeat, percusión tradicional y folk internacional",
            listenersCount = "14.6k sintonizados"
        )
    )

    private val _stations = MutableStateFlow<List<RadioStation>>(defaultStations)
    val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    fun addCustomStation(name: String, genre: String, streamUrl: String, coverUrl: String? = null) {
        val newStation = RadioStation(
            id = "custom_${System.currentTimeMillis()}",
            name = name.ifBlank { "Estación Personalizada" },
            genre = genre.ifBlank { "Radio Web" },
            streamUrl = streamUrl.trim(),
            coverUrl = coverUrl?.ifBlank { null } ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            description = "Estación de radio M3U / Web personalizada",
            listenersCount = "En Vivo"
        )
        _stations.value = listOf(newStation) + _stations.value
    }
}
