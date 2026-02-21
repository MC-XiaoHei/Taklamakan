package cn.xor7.xiaohei.taklamakan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val offlineRepository: OfflineRevivalRepository) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val reviveData = offlineRepository.consumeAndSave(player.uniqueId) ?: return

        applyRevivalEffects(player, reviveData)
    }

    private fun applyRevivalEffects(player: Player, data: OfflineReviveData) {
        player.teleport(data.location)
        player.gameMode = GameMode.SURVIVAL
        player.health = Constants.REVIVED_HEALTH
        player.foodLevel = Constants.REVIVED_FOOD
        player.sendMessage(Component.text("${data.helperName} 透支了生命将你拉回现实。", NamedTextColor.GREEN))
    }
}