package hero

data class Canvas(val width: Int, val height: Int)

data class Position(
  val top: Int? = null,
  val bottom: Int? = null,
  val left: Int? = null,
  val right: Int? = null,
)

data class Device(
  val id: String,
  val type: String,
  val theme: String,
  val screenWidth: Int? = null,
  val bodyWidth: Int? = null,
  val position: Position,
  val rotate: Int,
  val zIndex: Int,
)

data class HeroConfig(val canvas: Canvas, val devices: List<Device>)

val heroConfig = HeroConfig(
  canvas = Canvas(width = 1400, height = 800),
  devices = listOf(
    Device(
      id = "desktop-1", type = "macbook", theme = "light", screenWidth = 380,
      position = Position(top = 30, left = 60), rotate = -4, zIndex = 1,
    ),
    Device(
      id = "desktop-2", type = "macbook", theme = "dark", screenWidth = 400,
      position = Position(top = 40, right = 90), rotate = 5, zIndex = 1,
    ),
    Device(
      id = "desktop-3", type = "macbook", theme = "dark", screenWidth = 460,
      position = Position(top = 80, left = 420), rotate = 2, zIndex = 2,
    ),
    Device(
      id = "desktop-5", type = "macbook", theme = "light", screenWidth = 420,
      position = Position(bottom = 20, left = 170), rotate = 6, zIndex = 3,
    ),
    Device(
      id = "desktop-4", type = "macbook", theme = "dark", screenWidth = 580,
      position = Position(top = 190, left = 280), rotate = -2, zIndex = 4,
    ),
    Device(
      id = "phone-1", type = "iphone", theme = "dark", bodyWidth = 155,
      position = Position(top = 100, left = 50), rotate = -12, zIndex = 6,
    ),
    Device(
      id = "phone-2", type = "iphone", theme = "light", bodyWidth = 148,
      position = Position(top = 330, left = 130), rotate = 7, zIndex = 7,
    ),
    Device(
      id = "phone-3", type = "iphone", theme = "dark", bodyWidth = 140,
      position = Position(top = 110, left = 620), rotate = -5, zIndex = 6,
    ),
    Device(
      id = "phone-4", type = "iphone", theme = "light", bodyWidth = 155,
      position = Position(top = 290, right = 150), rotate = 10, zIndex = 7,
    ),
    Device(
      id = "phone-5", type = "iphone", theme = "dark", bodyWidth = 150,
      position = Position(top = 80, right = 50), rotate = 8, zIndex = 6,
    ),
    Device(
      id = "phone-6", type = "iphone", theme = "light", bodyWidth = 145,
      position = Position(bottom = 30, right = 80), rotate = -9, zIndex = 5,
    ),
  ),
)
