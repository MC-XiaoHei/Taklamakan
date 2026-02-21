package cn.xor7.xiaohei.taklamakan

import cn.xor7.xiaohei.taklamakan.Constants.MSG_CANNOT_SLEEP
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent

class SleepListener : Listener {
    @EventHandler
    fun onPlayerSleep(event: PlayerBedEnterEvent) {
        event.isCancelled = true
        event.player.sendMessage(MSG_CANNOT_SLEEP)
    }
}