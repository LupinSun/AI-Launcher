package de.mm20.launcher2.aisearch

import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import de.mm20.launcher2.base.R as BaseR
import de.mm20.launcher2.icons.ColorLayer
import de.mm20.launcher2.icons.StaticLauncherIcon
import de.mm20.launcher2.icons.TintedIconLayer
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.search.SearchableSerializer

data class AiSearchResult(
    val query: String,
    val answer: String,
    override val labelOverride: String? = null,
) : SavableSearchable {

    override val domain: String = Domain
    override val key: String = "ai_search_${query.hashCode()}"
    override val label: String = query
    override val preferDetailsOverLaunch: Boolean = true

    override fun overrideLabel(label: String): AiSearchResult = copy(labelOverride = label)

    override fun launch(context: Context, options: Bundle?): Boolean = false

    override fun getPlaceholderIcon(context: Context): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = TintedIconLayer(
                icon = ContextCompat.getDrawable(context, BaseR.drawable.auto_awesome_24dp)!!,
                scale = 0.65f,
                color = 0,
            ),
            backgroundLayer = ColorLayer(0),
        )
    }

    override fun getSerializer(): SearchableSerializer = NullAiSearchSerializer()

    companion object {
        const val Domain = "ai_search"
    }
}

private class NullAiSearchSerializer : SearchableSerializer {
    override val typePrefix: String = AiSearchResult.Domain
    override fun serialize(searchable: SavableSearchable): String? = null
}
