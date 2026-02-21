package cn.xor7.xiaohei.taklamakan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object Constants {
    const val REVIVE_DURATION_MS = 10000L
    const val INTERACT_TIMEOUT_MS = 600L
    const val HEALTH_PENALTY_ON_DEATH = 2.0
    const val HELPER_HEALTH_COST = 2.0
    const val HELPER_FOOD_COST = 6
    const val REVIVED_HEALTH = 2.0
    const val REVIVED_FOOD = 6
    const val WATCHDOG_INTERVAL_TICKS = 5L

    val MSG_DEATH_PENALTY: Component = Component.text("死亡抽走了你的生命力。最大生命值永久减少 1 颗心。", NamedTextColor.RED)
    val MSG_PERMA_DEATH: Component = Component.text("灵魂彻底剥蚀。你将作为风沙的一部分永远游荡在此。", NamedTextColor.DARK_GRAY)
    val MSG_TOO_WEAK: Component = Component.text("你现在的状态太过虚弱，强行转移只会拉着你一起陪葬！", NamedTextColor.DARK_RED)
    val MSG_REVIVE_START: Component = Component.text("正在注入灵魂！死死按住右键，不要松手！", NamedTextColor.AQUA)
    val MSG_REVIVE_INTERRUPT: Component = Component.text("你松开了手，灵魂转移被打断！", NamedTextColor.RED)
    val MSG_REVIVE_SUCCESS: Component = Component.text("仪式完成，你成功唤醒了亡者。", NamedTextColor.GREEN)
    val MSG_CANNOT_SLEEP: Component = Component.text("在死亡沙漠中，你无法安然入睡，黑夜必须被熬过。", NamedTextColor.DARK_GRAY)
}