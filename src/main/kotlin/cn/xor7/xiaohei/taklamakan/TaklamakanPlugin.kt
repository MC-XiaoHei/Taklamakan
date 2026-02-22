package cn.xor7.xiaohei.taklamakan

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


@Suppress("unused")
class TaklamakanPlugin : JavaPlugin() {
    private lateinit var offlineRevivalRepository: OfflineRevivalRepository
    private lateinit var graveRepository: GraveRepository
    private lateinit var svcPlugin: VoiceChatPlugin

    override fun onEnable() {
        initializeRepositories()
        registerListeners()
        registerSVCPlugin()
    }

    override fun onDisable() {
        offlineRevivalRepository.save()
        graveRepository.save()
        unregisterSVCPlugin()
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

    private fun registerSVCPlugin() {
        val service = server.servicesManager.load(BukkitVoicechatService::class.java)
            ?: throw IllegalStateException("voice chat service not loaded")
        svcPlugin = VoiceChatPlugin()
        service.registerPlugin(svcPlugin)
    }

    private fun unregisterSVCPlugin() {
        if (this::svcPlugin.isInitialized) {
            server.servicesManager.unregister(svcPlugin)
        }
    }
}