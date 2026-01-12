package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.be1ski.memos.shared.domain.model.Memo

/**
 * Posts tab showing raw memos list.
 */
@Composable
fun PostsScreen(memos: List<Memo>) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    items(memos) { memo ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(memo.content, style = MaterialTheme.typography.bodyMedium)
        }
      }
    }
  }
}
