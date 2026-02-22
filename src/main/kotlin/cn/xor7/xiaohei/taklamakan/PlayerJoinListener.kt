package cn.xor7.xiaohei.taklamakan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val offlineRepository: OfflineRevivalRepository) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (isNewPlayer(player)) {
            teleportToDesertStartingPoint(player)
            return
        }

        val reviveData = offlineRepository.consumeAndSave(player.uniqueId) ?: return
        applyRevivalEffects(player, reviveData)
    }

    private fun isNewPlayer(player: Player): Boolean = !player.hasPlayedBefore()

    private fun teleportToDesertStartingPoint(player: Player) {
        val spawnLocation = Location(player.world, 0.5, 68.0, 62000.5)
        player.teleport(spawnLocation)
    }

    private fun applyRevivalEffects(player: Player, data: OfflineReviveData) {
        player.teleport(data.location)
        player.gameMode = GameMode.SURVIVAL
        player.health = Constants.REVIVED_HEALTH
        player.foodLevel = Constants.REVIVED_FOOD
        player.sendMessage(Component.text("${data.helperName} 透支了生命将你拉回现实。", NamedTextColor.GREEN))
    }
}