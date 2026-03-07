package space.be1ski.vibits.core.ui.test.hero

enum class HeroVariant { LIGHT, DARK }

data class HeroCanvasSize(
  val width: Int,
  val height: Int,
)

data class HeroPosition(
  val top: Int? = null,
  val bottom: Int? = null,
  val left: Int? = null,
  val right: Int? = null,
)

data class HeroDevice(
  val id: String,
  val type: String,
  val theme: String,
  val scenario: String,
  val screenWidth: Int? = null,
  val bodyWidth: Int? = null,
  val position: HeroPosition,
  val rotate: Int,
  val zIndex: Int,
)

data class HeroDot(
  val color: String,
  val size: Int,
  val top: Int,
  val left: Int? = null,
  val right: Int? = null,
)

data class HeroConfig(
  val canvas: HeroCanvasSize,
  val devices: List<HeroDevice>,
  val dots: List<HeroDot>,
)

val heroConfig =
  HeroConfig(
    canvas = HeroCanvasSize(width = 1400, height = 800),
    devices =
      listOf(
        HeroDevice(
          id = "desktop-4",
          type = "macbook",
          theme = "dark",
          scenario = "app_habits_week",
          screenWidth = 580,
          position = HeroPosition(top = 190, left = 280),
          rotate = -2,
          zIndex = 4,
        ),
        HeroDevice(
          id = "desktop-3",
          type = "macbook",
          theme = "dark",
          scenario = "app_habits_month",
          screenWidth = 460,
          position = HeroPosition(top = 80, left = 420),
          rotate = 2,
          zIndex = 2,
        ),
        HeroDevice(
          id = "desktop-1",
          type = "macbook",
          theme = "light",
          scenario = "app_feed",
          screenWidth = 380,
          position = HeroPosition(top = 30, left = 60),
          rotate = -4,
          zIndex = 1,
        ),
        HeroDevice(
          id = "desktop-2",
          type = "macbook",
          theme = "dark",
          scenario = "app_stats_week",
          screenWidth = 400,
          position = HeroPosition(top = 40, right = 90),
          rotate = 5,
          zIndex = 1,
        ),
        HeroDevice(
          id = "phone-1",
          type = "iphone",
          theme = "dark",
          scenario = "app_habits_year",
          bodyWidth = 155,
          position = HeroPosition(top = 100, left = 50),
          rotate = -12,
          zIndex = 6,
        ),
        HeroDevice(
          id = "phone-2",
          type = "iphone",
          theme = "light",
          scenario = "app_feed",
          bodyWidth = 148,
          position = HeroPosition(top = 330, left = 130),
          rotate = 7,
          zIndex = 7,
        ),
        HeroDevice(
          id = "phone-3",
          type = "iphone",
          theme = "dark",
          scenario = "app_habit_editor",
          bodyWidth = 140,
          position = HeroPosition(top = 110, left = 620),
          rotate = -5,
          zIndex = 6,
        ),
        HeroDevice(
          id = "phone-4",
          type = "iphone",
          theme = "light",
          scenario = "app_habits_quarter",
          bodyWidth = 155,
          position = HeroPosition(top = 290, right = 150),
          rotate = 10,
          zIndex = 7,
        ),
      ),
    dots =
      listOf(
        HeroDot("purple", 10, 25, left = 200),
        HeroDot("blue", 8, 12, left = 520),
        HeroDot("purple", 11, 55, right = 280),
        HeroDot("blue", 7, 760, left = 90),
        HeroDot("purple", 9, 740, left = 550),
        HeroDot("blue", 11, 430, right = 10),
        HeroDot("purple", 8, 370, left = 8),
        HeroDot("blue", 7, 18, right = 40),
        HeroDot("purple", 7, 570, left = 700),
      ),
  )
