package cn.xor7.xiaohei.taklamakan

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.TextDisplay
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class GraveData(
    val location: Location,
    val labelId: UUID
)

class GraveRepository(private val dataFile: File) {
    private val gravesByLocation = ConcurrentHashMap<Location, UUID>()
    private val gravesByPlayer = ConcurrentHashMap<UUID, GraveData>()

    fun load() {
        if (!dataFile.exists()) return
        val config = YamlConfiguration.loadConfiguration(dataFile)
        gravesByPlayer.clear()
        gravesByLocation.clear()

        config.getKeys(false).forEach { key ->
            parseAndStoreGrave(config, key)
        }
    }

    fun save() {
        dataFile.parentFile.mkdirs()
        val config = YamlConfiguration()

        gravesByPlayer.forEach { (uuid, data) ->
            writeGraveToConfig(config, uuid.toString(), data)
        }
        config.save(dataFile)
    }

    fun addGrave(playerId: UUID, location: Location, labelEntity: TextDisplay) {
        val data = GraveData(location, labelEntity.uniqueId)
        gravesByPlayer[playerId] = data
        gravesByLocation[location] = playerId
        save()
    }

    fun removeGrave(playerId: UUID) {
        val data = gravesByPlayer.remove(playerId)
        if (data != null) {
            gravesByLocation.remove(data.location)
            save()
        }
    }

    fun isGraveBlock(location: Location): Boolean {
        return gravesByLocation.containsKey(location)
    }

    fun getGraveOwner(location: Location): UUID? {
        return gravesByLocation[location]
    }

    fun findGraveByPlayer(playerId: UUID): GraveData? {
        return gravesByPlayer[playerId]
    }

    fun getLabelEntity(playerId: UUID): TextDisplay? {
        val labelId = gravesByPlayer[playerId]?.labelId ?: return null
        return Bukkit.getEntity(labelId) as? TextDisplay
    }

    private fun parseAndStoreGrave(config: YamlConfiguration, key: String) {
        try {
            val playerId = UUID.fromString(key)
            val location = config.getLocation("$key.location")
            val labelIdStr = config.getString("$key.labelId")

            if (location != null && labelIdStr != null) {
                val labelId = UUID.fromString(labelIdStr)
                val data = GraveData(location, labelId)
                gravesByPlayer[playerId] = data
                gravesByLocation[location] = playerId
            }
        } catch (_: Exception) {
        }
    }

    private fun writeGraveToConfig(config: YamlConfiguration, key: String, data: GraveData) {
        config.set("$key.location", data.location)
        config.set("$key.labelId", data.labelId.toString())
    }
}