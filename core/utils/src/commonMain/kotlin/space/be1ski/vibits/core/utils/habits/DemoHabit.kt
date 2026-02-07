package space.be1ski.vibits.core.utils.habits

enum class DemoHabit(
  val id: String,
) {
  EXERCISE("exercise"),
  WATER("water"),
  READING("reading"),
  MEDITATION("meditation"),
  WALKING("walking"),
  LEARNING("learning"),
  NO_SUGAR("no_sugar"),
  EARLY_SLEEP("early_sleep"),
  ;

  companion object {
    private val BY_ID = entries.associateBy { it.id }

    fun fromId(id: String): DemoHabit? = BY_ID[id]
  }
}
