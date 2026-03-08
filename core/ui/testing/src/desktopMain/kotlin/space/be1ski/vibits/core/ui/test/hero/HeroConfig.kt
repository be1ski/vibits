package space.be1ski.vibits.core.ui.test.hero

enum class HeroVariant {
  LIGHT,
  DARK,
  ;

  val theme: String get() = name.lowercase()
}

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
  val scenario: String,
  val screenWidth: Int? = null,
  val bodyWidth: Int? = null,
  val position: HeroPosition,
  val rotate: Int,
  val alpha: Float = 1f,
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
          id = "pair-habits-week-desktop-front",
          type = "macbook",
          scenario = "app_habits_week",
          screenWidth = 400,
          position = HeroPosition(top = 195, right = 350),
          rotate = -4,
          zIndex = 8,
        ),
        HeroDevice(
          id = "pair-habits-week-mobile-back",
          type = "iphone",
          scenario = "app_habits_week",
          bodyWidth = 168,
          position = HeroPosition(top = -22, right = 610),
          rotate = 17,
          alpha = 0.8f,
          zIndex = 2,
        ),
        HeroDevice(
          id = "pair-memos-mobile-front",
          type = "iphone",
          scenario = "app_feed",
          bodyWidth = 168,
          position = HeroPosition(top = 330, right = 160),
          rotate = -14,
          zIndex = 7,
        ),
        HeroDevice(
          id = "pair-memos-desktop-back",
          type = "macbook",
          scenario = "app_feed",
          screenWidth = 340,
          position = HeroPosition(top = 70, right = 100),
          rotate = 6,
          alpha = 0.78f,
          zIndex = 1,
        ),
        HeroDevice(
          id = "pair-habits-year-mobile-front",
          type = "iphone",
          scenario = "app_habits_year",
          bodyWidth = 176,
          position = HeroPosition(top = 230, left = 150),
          rotate = -15,
          zIndex = 7,
        ),
        HeroDevice(
          id = "pair-habits-year-desktop-back",
          type = "macbook",
          scenario = "app_habits_quarter",
          screenWidth = 370,
          position = HeroPosition(top = 12, left = 110),
          rotate = 0,
          alpha = 0.78f,
          zIndex = 1,
        ),
        HeroDevice(
          id = "pair-settings-desktop-front",
          type = "macbook",
          scenario = "app_settings_online",
          screenWidth = 260,
          position = HeroPosition(bottom = 34, left = 60),
          rotate = 2,
          alpha = 0.9f,
          zIndex = 5,
        ),
        HeroDevice(
          id = "pair-settings-mobile-back",
          type = "iphone",
          scenario = "app_settings_online",
          bodyWidth = 144,
          position = HeroPosition(top = 316, left = 30),
          rotate = 15,
          alpha = 0.78f,
          zIndex = 2,
        ),
        HeroDevice(
          id = "pair-stats-week-desktop-back",
          type = "macbook",
          scenario = "app_memos_year",
          screenWidth = 275,
          position = HeroPosition(bottom = 37, right = 630),
          rotate = -3,
          alpha = 0.74f,
          zIndex = 2,
        ),
        HeroDevice(
          id = "pair-stats-week-mobile-front",
          type = "iphone",
          scenario = "app_memos_week",
          bodyWidth = 144,
          position = HeroPosition(bottom = 36, right = 730),
          rotate = 11,
          zIndex = 6,
        ),
      ),
    dots =
      listOf(
        HeroDot("purple", 10, 25, left = 200),
        HeroDot("blue", 8, 12, left = 520),
        HeroDot("purple", 11, 55, right = 280),
        HeroDot("purple", 9, 740, left = 560),
        HeroDot("blue", 11, 430, right = 16),
        HeroDot("purple", 8, 370, left = 12),
        HeroDot("blue", 7, 18, right = 40),
      ),
  )
