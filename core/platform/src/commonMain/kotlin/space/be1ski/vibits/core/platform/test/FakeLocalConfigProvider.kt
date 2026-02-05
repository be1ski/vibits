package space.be1ski.vibits.core.platform.test

import space.be1ski.vibits.core.platform.env.LocalConfigProvider

fun createFakeLocalConfigProvider(config: Map<String, String> = emptyMap()): LocalConfigProvider =
  LocalConfigProvider { key -> config[key] }
