package at.crowdware.paperproto

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class Page(
    val id: Int,
    val name: String,
    val picture: String,
    val hotSpots: List<HotSpot> = emptyList()
)

@Serializable
data class Picture(val url: String)

@Serializable
data class HotSpot(val x: Int, val y: Int, val width: Int, val height: Int, val link: String)