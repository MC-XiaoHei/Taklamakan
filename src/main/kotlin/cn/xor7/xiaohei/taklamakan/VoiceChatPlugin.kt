package cn.xor7.xiaohei.taklamakan

import de.maxhenkel.voicechat.api.Group
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatServerApi
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent
import java.util.*

class VoiceChatPlugin : VoicechatPlugin {
    private val groupUUID = UUID.nameUUIDFromBytes("Taklamakan".toByteArray())
    private lateinit var serverApi: VoicechatServerApi
    private lateinit var group: Group

    override fun getPluginId(): String = "taklamakan"

    override fun initialize(api: VoicechatApi) {
        serverApi = api as? VoicechatServerApi
            ?: throw IllegalStateException("voice chat server api is not an instance of VoiceChatServerApi")
        group = serverApi.groupBuilder()
            .setPersistent(true)
            .setId(groupUUID)
            .setName("临时起意")
            .setType(Group.Type.NORMAL)
            .setHidden(false)
            .build()
    }

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(
            PlayerConnectedEvent::class.java,
            this::onPlayerConnect,
            100,
        )
    }

    private fun onPlayerConnect(event: PlayerConnectedEvent) {
        event.connection.group = group
    }
}