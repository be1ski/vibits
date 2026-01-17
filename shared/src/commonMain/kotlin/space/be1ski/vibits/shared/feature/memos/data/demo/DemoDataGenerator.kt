package space.be1ski.vibits.shared.feature.memos.data.demo

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.shared.feature.habits.domain.labelFromTag
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Demo habit configuration with completion probability.
 */
internal data class DemoHabit(
  val tag: String,
  val color: String,
  val baseCompletionRate: Float,
  val weekendModifier: Float = 1.0f
)

/**
 * Generates mock memos for demo mode with realistic habit data.
 */
@Suppress("MagicNumber")
internal object DemoDataGenerator {

  private val demoHabits = listOf(
    DemoHabit("#habits/exercise", "#FF9800", 0.85f, 0.7f),
    DemoHabit("#habits/reading", "#2196F3", 0.70f, 1.1f),
    DemoHabit("#habits/meditation", "#9C27B0", 0.60f, 1.0f),
    DemoHabit("#habits/water", "#00BCD4", 0.90f, 0.95f),
    DemoHabit("#habits/learning", "#4CAF50", 0.50f, 0.6f),
    DemoHabit("#habits/walking", "#009688", 0.65f, 1.2f),
    DemoHabit("#habits/no_sugar", "#F44336", 0.45f, 0.8f),
    DemoHabit("#habits/early_sleep", "#3F51B5", 0.55f, 0.7f)
  )

  private const val MONTHS_OF_HISTORY = 18
  private const val ZERO_MOTIVATION_CHANCE = 0.12f
  private const val CONFIG_HOUR = 8
  private const val SEASONAL_MODIFIER_BASE = 5
  private const val SEASONAL_MODIFIER_FACTOR = 0.01f
  private const val MIN_COMPLETION_RATE = 0.1f
  private const val MAX_COMPLETION_RATE = 0.98f
  private const val POST_GENERATION_CHANCE = 0.15f
  private val DAILY_HOURS = listOf(7, 9, 12, 14, 18, 21, 23)

  /**
   * Generates all demo memos including config and daily memos.
   */
  fun generateDemoMemos(): List<Memo> {
    val memos = mutableListOf<Memo>()
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    val today = now.toLocalDateTime(timeZone).date

    // Config memo (created at the start of history)
    val configDate = today.minus(MONTHS_OF_HISTORY, DateTimeUnit.MONTH)
    val configInstant = instantForDate(configDate, hoursOffset = CONFIG_HOUR)
    memos.add(createConfigMemo(configInstant))

    // Daily memos for each day
    var currentDate = configDate
    val random = Random(42) // Fixed seed for reproducibility in demos

    while (currentDate <= today) {
      // Some days have zero motivation - skip them entirely
      if (random.nextFloat() >= ZERO_MOTIVATION_CHANCE) {
        val dailyHour = DAILY_HOURS[random.nextInt(DAILY_HOURS.size)]
        val dailyInstant = instantForDate(currentDate, hoursOffset = dailyHour)
        val completedHabits = selectCompletedHabits(currentDate, random)
        if (completedHabits.isNotEmpty()) {
          memos.add(createDailyMemo(currentDate, completedHabits, dailyInstant))
        }
      }
      // Generate regular posts randomly throughout the period
      if (random.nextFloat() < POST_GENERATION_CHANCE) {
        memos.add(createRandomPost(currentDate, random))
      }
      currentDate = currentDate.nextDay()
    }

    // Add a few recent sample memos for variety
    memos.addAll(createSampleMemos(today))

    return memos
  }

  private fun createConfigMemo(createTime: Instant): Memo {
    val content = buildString {
      appendLine("#habits/config")
      appendLine()
      demoHabits.forEach { habit ->
        val label = labelFromTag(habit.tag)
        appendLine("$label | ${habit.tag} | ${habit.color}")
      }
    }
    return Memo(
      name = "memos/demo_config",
      content = content.trim(),
      createTime = createTime,
      updateTime = createTime
    )
  }

  private fun createDailyMemo(
    date: LocalDate,
    completedHabits: List<DemoHabit>,
    createTime: Instant
  ): Memo {
    val content = buildString {
      appendLine("#habits/daily $date")
      appendLine()
      completedHabits.forEach { habit ->
        appendLine(habit.tag)
      }
    }
    return Memo(
      name = "memos/demo_daily_$date",
      content = content.trim(),
      createTime = createTime,
      updateTime = createTime
    )
  }

  private fun selectCompletedHabits(date: LocalDate, random: Random): List<DemoHabit> {
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    return demoHabits.filter { habit ->
      val rate = if (isWeekend) {
        habit.baseCompletionRate * habit.weekendModifier
      } else {
        habit.baseCompletionRate
      }
      // Add some seasonal variation
      val seasonalModifier = 1.0f + (date.month.ordinal - SEASONAL_MODIFIER_BASE) * SEASONAL_MODIFIER_FACTOR
      val adjustedRate = (rate * seasonalModifier).coerceIn(MIN_COMPLETION_RATE, MAX_COMPLETION_RATE)
      random.nextFloat() < adjustedRate
    }
  }

  private val randomPostContents = listOf(
    "Quick thought: Need to organize my workspace tomorrow.",
    "Idea: Try the new coffee shop on 5th street.",
    "Note to self: Call dentist for appointment.",
    "Interesting article about productivity I found today.",
    "Weather is nice, should go for a walk later.",
    "Meeting notes from today's standup.",
    "Reminder: Pick up groceries on the way home.",
    "Book recommendation from a friend - need to check it out.",
    "Finished debugging that tricky issue finally!",
    "Planning the weekend trip - need to book hotel.",
    "New recipe to try: Thai curry with coconut milk.",
    "Watched a great documentary about space exploration.",
    "Phone battery dying too fast, might need replacement.",
    "Good conversation with team today about the project.",
    "Found a useful shortcut in the IDE today."
  )

  private fun createRandomPost(date: LocalDate, random: Random): Memo {
    val hour = DAILY_HOURS[random.nextInt(DAILY_HOURS.size)]
    val content = randomPostContents[random.nextInt(randomPostContents.size)]
    val instant = instantForDate(date, hoursOffset = hour)
    return Memo(
      name = "memos/demo_post_${date}_$hour",
      content = content,
      createTime = instant,
      updateTime = instant
    )
  }

  private fun createSampleMemos(today: LocalDate): List<Memo> {
    return listOf(
      Memo(
        name = "memos/demo_note_1",
        content = "Weekly review: Good progress on meditation practice this week.",
        createTime = instantForDate(today.minus(3, DateTimeUnit.DAY), hoursOffset = 14),
        updateTime = instantForDate(today.minus(3, DateTimeUnit.DAY), hoursOffset = 14)
      ),
      Memo(
        name = "memos/demo_note_2",
        content = "Book finished: Atomic Habits by James Clear. Great insights on habit formation.",
        createTime = instantForDate(today.minus(10, DateTimeUnit.DAY), hoursOffset = 20),
        updateTime = instantForDate(today.minus(10, DateTimeUnit.DAY), hoursOffset = 20)
      ),
      Memo(
        name = "memos/demo_note_3",
        content = "New goal: Increase walking to 12K steps by end of month.",
        createTime = instantForDate(today.minus(7, DateTimeUnit.DAY), hoursOffset = 9),
        updateTime = instantForDate(today.minus(7, DateTimeUnit.DAY), hoursOffset = 9)
      )
    )
  }

  private fun instantForDate(date: LocalDate, hoursOffset: Int): Instant {
    val epochDays = date.toEpochDays()
    val epochSeconds = epochDays * 24 * 60 * 60 + hoursOffset * 60 * 60
    return Instant.fromEpochSeconds(epochSeconds)
  }

  private fun LocalDate.nextDay(): LocalDate =
    LocalDate.fromEpochDays(this.toEpochDays() + 1)
}
