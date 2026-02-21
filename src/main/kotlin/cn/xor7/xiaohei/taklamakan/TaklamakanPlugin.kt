package cn.xor7.xiaohei.taklamakan

import org.bukkit.plugin.java.JavaPlugin
import java.io.File

@Suppress("unused")
class TaklamakanPlugin : JavaPlugin() {
    private lateinit var offlineRevivalRepository: OfflineRevivalRepository
    private lateinit var graveRepository: GraveRepository

    override fun onEnable() {
        initializeRepositories()
        registerListeners()
    }

    override fun onDisable() {
        offlineRevivalRepository.save()
        graveRepository.save()
    }

    private fun initializeRepositories() {
        val offlineDataFile = File(dataFolder, "offline_revivals.yml")
        offlineRevivalRepository = OfflineRevivalRepository(offlineDataFile)
        offlineRevivalRepository.load()

        val graveDataFile = File(dataFolder, "graves.yml")
        graveRepository = GraveRepository(graveDataFile)
        graveRepository.load()
    }

    private fun registerListeners() {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(DeathListener(this, graveRepository), this)
        pluginManager.registerEvents(ReviveListener(this, offlineRevivalRepository, graveRepository), this)
        pluginManager.registerEvents(GraveProtectionListener(graveRepository), this)
        pluginManager.registerEvents(SleepListener(), this)
        pluginManager.registerEvents(PlayerJoinListener(offlineRevivalRepository), this)
    }
}