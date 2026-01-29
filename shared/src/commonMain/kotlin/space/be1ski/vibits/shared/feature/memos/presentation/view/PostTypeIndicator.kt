package space.be1ski.vibits.shared.feature.memos.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.shared.feature.memos.domain.usecase.ClassifyPostTypeUseCase

@Composable
internal fun PostTypeIndicator(
  memo: Memo,
  modifier: Modifier = Modifier,
) {
  val postType = ClassifyPostTypeUseCase(memo)

  val color =
    when (postType) {
      PostFilter.CONFIG -> AppColors.habitPurple.resolve()
      PostFilter.HABIT_TRACKING -> AppColors.habitGreen.resolve()
      PostFilter.REGULAR -> AppColors.habitBlue.resolve()
      PostFilter.ALL -> AppColors.habitBlue.resolve()
    }

  Box(
    modifier =
      modifier
        .width(Indent.x3s)
        .fillMaxHeight()
        .background(color),
  )
}
