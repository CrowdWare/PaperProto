package at.crowdware.paperproto

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


fun savePages(context: Context, pages: List<Page>) {
    val pagesFile = File(context.filesDir, "pages.json")
    pagesFile.writeText(Json.encodeToString(pages))
}


fun loadPages(context: Context): List<Page> {
    val pagesFile = File(context.filesDir, "pages.json")
    return if (pagesFile.exists()) {
        Json.decodeFromString(pagesFile.readText())
    } else {
        emptyList()
    }
}

fun addNewPage(context: Context, imagePath: String, pages: MutableList<Page>) : Page {
    val newId = (pages.maxOfOrNull { it.id } ?: 0) + 1
    val newPage = Page(
        id = newId,
        name = "Page $newId",
        picture = imagePath
    )
    pages.add(newPage)
    savePages(context, pages)
    return newPage
}

fun deletePage(context: Context, page: Page, pages: MutableList<Page>) {
    pages.remove(page)

    val imageFile = File(page.picture)
    if (imageFile.exists())
        imageFile.delete()

    savePages(context, pages)
}
