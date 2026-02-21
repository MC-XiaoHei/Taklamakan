package cn.xor7.xiaohei.taklamakan

import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class OfflineReviveData(
    val location: Location,
    val helperName: String
)

class OfflineRevivalRepository(private val dataFile: File) {
    private val pendingRevivals = ConcurrentHashMap<UUID, OfflineReviveData>()

    fun load() {
        if (!dataFile.exists()) return
        val config = YamlConfiguration.loadConfiguration(dataFile)
        pendingRevivals.clear()

        config.getKeys(false).forEach { key ->
            parseAndStoreRevivalData(config, key)
        }
    }

    fun save() {
        dataFile.parentFile.mkdirs()
        val config = YamlConfiguration()

        pendingRevivals.forEach { (uuid, data) ->
            writeRevivalDataToConfig(config, uuid.toString(), data)
        }
        config.save(dataFile)
    }

    fun addAndSave(playerId: UUID, data: OfflineReviveData) {
        pendingRevivals[playerId] = data
        save()
    }

    fun consumeAndSave(playerId: UUID): OfflineReviveData? {
        val data = pendingRevivals.remove(playerId)
        if (data != null) save()
        return data
    }

    private fun parseAndStoreRevivalData(config: YamlConfiguration, key: String) {
        val uuid = UUID.fromString(key)
        val location = config.getLocation("$key.location")
        val helperName = config.getString("$key.helperName")

        if (location != null && helperName != null) {
            pendingRevivals[uuid] = OfflineReviveData(location, helperName)
        }
    }

    private fun writeRevivalDataToConfig(config: YamlConfiguration, key: String, data: OfflineReviveData) {
        config.set("$key.location", data.location)
        config.set("$key.helperName", data.helperName)
    }
}