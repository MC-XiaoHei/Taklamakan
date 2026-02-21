package cn.xor7.xiaohei.taklamakan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.Skull
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent

class DeathListener(
    private val plugin: TaklamakanPlugin,
    private val graveRepository: GraveRepository
) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (player.gameMode != GameMode.SURVIVAL) return
        applyDeathPenalty(player)

        if (isPermanentlyDead(player)) {
            handlePermanentDeath(player)
            return
        }
        createCorpse(player)
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val grave = graveRepository.findGraveByPlayer(player.uniqueId)

        if (grave != null) {
            event.respawnLocation = calculateRespawnLocation(grave.location)
            enforceSpectatorMode(player)
            return
        }

        if (isPermanentlyDead(player)) {
            enforceSpectatorMode(player)
        }
    }

    private fun applyDeathPenalty(player: Player) {
        val maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH) ?: return
        val newMax = maxHealthAttr.baseValue - Constants.HEALTH_PENALTY_ON_DEATH
        maxHealthAttr.baseValue = newMax

        if (newMax > 0) player.sendMessage(Constants.MSG_DEATH_PENALTY)
    }

    private fun isPermanentlyDead(player: Player): Boolean {
        val maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH) ?: return true
        return maxHealthAttr.baseValue <= 0
    }

    private fun handlePermanentDeath(player: Player) {
        player.gameMode = GameMode.SPECTATOR
        player.sendMessage(Constants.MSG_PERMA_DEATH)
    }

    private fun createCorpse(player: Player) {
        val deathLoc = player.location
        spawnPlayerHead(deathLoc, player)
        val label = spawnHologram(deathLoc, player)

        graveRepository.addGrave(player.uniqueId, deathLoc.block.location, label)

        player.gameMode = GameMode.SPECTATOR
        player.teleport(deathLoc)
    }

    private fun spawnPlayerHead(location: Location, owner: Player) {
        val block = location.block
        block.type = Material.PLAYER_HEAD
        val skull = block.state as Skull

        @Suppress("DEPRECATION")
        skull.setOwningPlayer(owner)
        skull.update()
    }

    private fun spawnHologram(location: Location, owner: Player): TextDisplay {
        val textLoc = location.clone().add(0.5, 1.2, 0.5)
        val label = textLoc.world.spawnEntity(textLoc, EntityType.TEXT_DISPLAY) as TextDisplay

        label.text(getInitialLabelComponent(owner.name))
        label.billboard = Display.Billboard.CENTER
        label.isDefaultBackground = false
        return label
    }

    private fun getInitialLabelComponent(playerName: String): Component {
        return Component.text("☠ ", NamedTextColor.DARK_RED)
            .append(Component.text("$playerName 的残躯", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("长按右键头颅 10秒 开启灵魂转移", NamedTextColor.YELLOW))
    }

    private fun calculateRespawnLocation(graveLocation: Location): Location {
        return graveLocation.clone().add(0.5, 0.0, 0.5)
    }

    private fun enforceSpectatorMode(player: Player) {
        plugin.server.scheduler.runTask(plugin, Runnable {
            player.gameMode = GameMode.SPECTATOR
        })
    }
}