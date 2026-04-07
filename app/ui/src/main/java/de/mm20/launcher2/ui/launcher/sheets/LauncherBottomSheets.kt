package de.mm20.launcher2.ui.launcher.sheets

import androidx.compose.runtime.Composable
import de.mm20.launcher2.ui.launcher.assistant.AssistantSheet

@Composable
fun LauncherBottomSheets() {
    val bottomSheetManager = LocalBottomSheetManager.current
    AssistantSheet(
        expanded = bottomSheetManager.assistantSheetShown.value,
        onDismiss = { bottomSheetManager.dismissAssistantSheet() },
    )
    CustomizeSearchableSheet(
        searchable = bottomSheetManager.customizeSearchableSheetShown.value,
        onDismiss = { bottomSheetManager.dismissCustomizeSearchableModal() })
    EditFavoritesSheet(
        expanded = bottomSheetManager.editFavoritesSheetShown.value,
        onDismiss = { bottomSheetManager.dismissEditFavoritesSheet() })
    EditTagSheet(
        expanded = bottomSheetManager.editTagSheetShown.value != null,
        tag = bottomSheetManager.editTagSheetShown.value,
        onDismiss = { bottomSheetManager.dismissEditTagSheet() }
    )
    FailedGestureSheet(
        bottomSheetManager.failedGestureSheetShown.value,
        onDismiss = { bottomSheetManager.dismissFailedGestureSheet() }
    )
}