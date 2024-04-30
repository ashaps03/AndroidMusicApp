import com.google.gson.annotations.SerializedName

data class PlaylistTrackResult(
    @SerializedName("items") val items: List<PlaylistTrack>
)

data class PlaylistTrack(
    @SerializedName("track") val track: Track
)

data class Track(
    @SerializedName("name") val name: String,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("external_urls") val externalUrls: ExternalUrls,
    @SerializedName("album") val album: Album,
    @SerializedName("preview_url") val previewUrl: String

)

data class Artist(
    @SerializedName("name") val name: String
)

data class ExternalUrls(
    @SerializedName("spotify") val spotify: String
)

data class Album(
    @SerializedName("images") val images: List<Image>
)

data class Image(
    @SerializedName("url") val url: String
)
