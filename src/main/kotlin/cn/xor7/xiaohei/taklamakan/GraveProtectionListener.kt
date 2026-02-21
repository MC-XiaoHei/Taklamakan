package cn.xor7.xiaohei.taklamakan

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityExplodeEvent

class GraveProtectionListener(private val graveRepository: GraveRepository) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (graveRepository.isGraveBlock(event.block.location)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        event.blockList().removeIf { graveRepository.isGraveBlock(it.location) }
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { graveRepository.isGraveBlock(it.location) }
    }

    @EventHandler
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        if (event.blocks.any { graveRepository.isGraveBlock(it.location) }) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        if (event.blocks.any { graveRepository.isGraveBlock(it.location) }) {
            event.isCancelled = true
        }
    }
}