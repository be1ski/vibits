package space.be1ski.vibits.core.ui.test.hero

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
          id = "desktop-1",
          type = "macbook",
          theme = "light",
          screenWidth = 380,
          position = HeroPosition(top = 30, left = 60),
          rotate = -4,
          zIndex = 1,
        ),
        HeroDevice(
          id = "desktop-2",
          type = "macbook",
          theme = "dark",
          screenWidth = 400,
          position = HeroPosition(top = 40, right = 90),
          rotate = 5,
          zIndex = 1,
        ),
        HeroDevice(
          id = "desktop-3",
          type = "macbook",
          theme = "dark",
          screenWidth = 460,
          position = HeroPosition(top = 80, left = 420),
          rotate = 2,
          zIndex = 2,
        ),
        HeroDevice(
          id = "desktop-5",
          type = "macbook",
          theme = "light",
          screenWidth = 420,
          position = HeroPosition(bottom = 20, left = 170),
          rotate = 6,
          zIndex = 3,
        ),
        HeroDevice(
          id = "desktop-4",
          type = "macbook",
          theme = "dark",
          screenWidth = 580,
          position = HeroPosition(top = 190, left = 280),
          rotate = -2,
          zIndex = 4,
        ),
        HeroDevice(
          id = "phone-1",
          type = "iphone",
          theme = "dark",
          bodyWidth = 155,
          position = HeroPosition(top = 100, left = 50),
          rotate = -12,
          zIndex = 6,
        ),
        HeroDevice(
          id = "phone-2",
          type = "iphone",
          theme = "light",
          bodyWidth = 148,
          position = HeroPosition(top = 330, left = 130),
          rotate = 7,
          zIndex = 7,
        ),
        HeroDevice(
          id = "phone-3",
          type = "iphone",
          theme = "dark",
          bodyWidth = 140,
          position = HeroPosition(top = 110, left = 620),
          rotate = -5,
          zIndex = 6,
        ),
        HeroDevice(
          id = "phone-4",
          type = "iphone",
          theme = "light",
          bodyWidth = 155,
          position = HeroPosition(top = 290, right = 150),
          rotate = 10,
          zIndex = 7,
        ),
        HeroDevice(
          id = "phone-5",
          type = "iphone",
          theme = "dark",
          bodyWidth = 150,
          position = HeroPosition(top = 80, right = 50),
          rotate = 8,
          zIndex = 6,
        ),
        HeroDevice(
          id = "phone-6",
          type = "iphone",
          theme = "light",
          bodyWidth = 145,
          position = HeroPosition(bottom = 30, right = 80),
          rotate = -9,
          zIndex = 5,
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
        HeroDot("purple", 6, 785, left = 900),
        HeroDot("blue", 9, 780, right = 130),
        HeroDot("purple", 8, 370, left = 8),
        HeroDot("blue", 7, 18, right = 40),
        HeroDot("purple", 7, 570, left = 700),
        HeroDot("blue", 8, 150, left = 380),
      ),
  )
