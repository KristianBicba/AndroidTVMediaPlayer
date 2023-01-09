package tpo.mediaplayer.lib_communications.shared

sealed interface PlaybackStatus {
    data class Playing(val data: NowPlaying) : PlaybackStatus
    data class Error(val error: String) : PlaybackStatus
    object Idle : PlaybackStatus
}