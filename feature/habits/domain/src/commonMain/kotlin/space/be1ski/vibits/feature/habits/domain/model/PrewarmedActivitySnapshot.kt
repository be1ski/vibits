package space.be1ski.vibits.feature.habits.domain.model

data class PrewarmedActivitySnapshot(
  val forRange: String,
  val snapshots: List<ActivitySnapshot>,
)
