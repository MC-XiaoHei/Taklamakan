package cn.xor7.xiaohei.taklamakan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class ReviveSession(
    val deadPlayerId: UUID,
    val startTimeMs: Long,
    var lastInteractTimeMs: Long
)

class ReviveListener(
    private val plugin: TaklamakanPlugin,
    private val offlineRepository: OfflineRevivalRepository,
    private val graveRepository: GraveRepository
) : Listener {
    private val activeRevivers = ConcurrentHashMap<UUID, ReviveSession>()

    init {
        startWatchdog()
    }

    @EventHandler
    fun onCorpseInteract(event: PlayerInteractEvent) {
        if (!isValidCorpseInteraction(event)) return

        val block = event.clickedBlock ?: return
        val deadPlayerId = graveRepository.getGraveOwner(block.location) ?: return

        processRevivalInteraction(event.player, deadPlayerId, block)
    }

    private fun isValidCorpseInteraction(event: PlayerInteractEvent): Boolean {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return false
        if (event.hand != EquipmentSlot.HAND) return false
        if (event.player.gameMode == GameMode.SPECTATOR) return false
        val block = event.clickedBlock ?: return false
        return block.type == Material.PLAYER_HEAD
    }

    private fun processRevivalInteraction(helper: Player, deadPlayerId: UUID, corpseBlock: Block) {
        val now = System.currentTimeMillis()
        val helperId = helper.uniqueId
        val currentSession = activeRevivers[helperId]

        if (currentSession == null || currentSession.deadPlayerId != deadPlayerId) {
            startNewRevival(helper, deadPlayerId, now)
        } else {
            continueRevival(helper, deadPlayerId, corpseBlock, currentSession, now)
        }
    }

    private fun startNewRevival(helper: Player, deadPlayerId: UUID, currentTimeMs: Long) {
        if (helper.health <= Constants.HELPER_HEALTH_COST || helper.foodLevel < Constants.HELPER_FOOD_COST) {
            helper.sendMessage(Constants.MSG_TOO_WEAK)
            return
        }

        activeRevivers[helper.uniqueId] = ReviveSession(deadPlayerId, currentTimeMs, currentTimeMs)
        helper.sendMessage(Constants.MSG_REVIVE_START)
    }

    private fun continueRevival(helper: Player, deadPlayerId: UUID, corpseBlock: Block, session: ReviveSession, currentTimeMs: Long) {
        session.lastInteractTimeMs = currentTimeMs
        val elapsedMs = currentTimeMs - session.startTimeMs

        updateProgressHologram(deadPlayerId, elapsedMs)

        if (elapsedMs >= Constants.REVIVE_DURATION_MS) {
            completeRevival(helper, deadPlayerId, corpseBlock)
        }
    }

    private fun updateProgressHologram(deadPlayerId: UUID, elapsedMs: Long) {
        val label = graveRepository.getLabelEntity(deadPlayerId) ?: return
        if (!label.isValid) return

        val secondsLeft = (Constants.REVIVE_DURATION_MS - elapsedMs) / 1000
        val progressText = Component.text("正在重塑灵魂... ${maxOf(0, secondsLeft)}秒", NamedTextColor.AQUA)
            .append(Component.newline())
            .append(Component.text("不要松开右键！", NamedTextColor.GRAY))

        label.text(progressText)
    }

    private fun completeRevival(helper: Player, deadPlayerId: UUID, corpseBlock: Block) {
        applyHelperCost(helper)

        val reviveLocation = calculateReviveLocation(corpseBlock)
        val deadPlayer = plugin.server.getPlayer(deadPlayerId)

        if (isPlayerOnline(deadPlayer)) {
            reviveOnlinePlayer(deadPlayer!!, helper.name, reviveLocation)
        } else {
            queueOfflineRevival(deadPlayerId, helper.name, reviveLocation)
        }

        playReviveSuccessEffects(helper)
        cleanupCorpse(deadPlayerId, corpseBlock)
        activeRevivers.remove(helper.uniqueId)
    }

    private fun applyHelperCost(helper: Player) {
        helper.health = maxOf(0.1, helper.health - Constants.HELPER_HEALTH_COST)
        helper.foodLevel = maxOf(0, helper.foodLevel - Constants.HELPER_FOOD_COST)
    }

    private fun calculateReviveLocation(corpseBlock: Block): Location {
        return corpseBlock.location.clone().add(0.5, 0.0, 0.5)
    }

    private fun isPlayerOnline(player: Player?): Boolean {
        return player != null && player.isOnline
    }

    private fun reviveOnlinePlayer(player: Player, helperName: String, location: Location) {
        player.teleport(location)
        player.gameMode = GameMode.SURVIVAL
        player.health = Constants.REVIVED_HEALTH
        player.foodLevel = Constants.REVIVED_FOOD
        player.sendMessage(Component.text("$helperName 透支了生命将你拉回现实。", NamedTextColor.GREEN))
    }

    private fun queueOfflineRevival(deadPlayerId: UUID, helperName: String, location: Location) {
        val data = OfflineReviveData(location, helperName)
        offlineRepository.addAndSave(deadPlayerId, data)
    }

    private fun playReviveSuccessEffects(helper: Player) {
        helper.sendMessage(Constants.MSG_REVIVE_SUCCESS)
        helper.playSound(helper.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f)
    }

    private fun cleanupCorpse(deadPlayerId: UUID, corpseBlock: Block) {
        corpseBlock.type = Material.AIR
        graveRepository.getLabelEntity(deadPlayerId)?.remove()
        graveRepository.removeGrave(deadPlayerId)
    }

    private fun startWatchdog() {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            val now = System.currentTimeMillis()
            val iterator = activeRevivers.entries.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()
                val helperId = entry.key
                val session = entry.value

                if (now - session.lastInteractTimeMs > Constants.INTERACT_TIMEOUT_MS) {
                    failRevival(helperId, session.deadPlayerId)
                    iterator.remove()
                }
            }
        }, 0L, Constants.WATCHDOG_INTERVAL_TICKS)
    }

    private fun failRevival(helperId: UUID, deadPlayerId: UUID) {
        val helper = plugin.server.getPlayer(helperId)
        if (helper != null) {
            helper.sendMessage(Constants.MSG_REVIVE_INTERRUPT)
            helper.playSound(helper.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f)
        }
        resetHologram(deadPlayerId)
    }

    private fun resetHologram(deadPlayerId: UUID) {
        val label = graveRepository.getLabelEntity(deadPlayerId) ?: return
        val deadPlayer = plugin.server.getPlayer(deadPlayerId)
        val name = deadPlayer?.name ?: "未知"

        val text = Component.text("☠ ", NamedTextColor.DARK_RED)
            .append(Component.text("$name 的残躯", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("长按右键头颅 10秒 开启灵魂转移", NamedTextColor.YELLOW))

        if (label.isValid) label.text(text)
    }
}