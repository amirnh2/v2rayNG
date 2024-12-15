package com.pix2ray.ang.fmt

import com.pix2ray.ang.dto.EConfigType
import com.pix2ray.ang.dto.ProfileItem
import com.pix2ray.ang.dto.V2rayConfig.OutboundBean
import com.pix2ray.ang.extension.isNotNullEmpty
import kotlin.text.orEmpty

object HttpFmt : FmtBase() {
    fun toOutbound(profileItem: ProfileItem): OutboundBean? {
        val outboundBean = OutboundBean.create(EConfigType.HTTP)

        outboundBean?.settings?.servers?.get(0)?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            if (profileItem.username.isNotNullEmpty()) {
                val socksUsersBean = OutboundBean.OutSettingsBean.ServersBean.SocksUsersBean()
                socksUsersBean.user = profileItem.username.orEmpty()
                socksUsersBean.pass = profileItem.password.orEmpty()
                server.users = listOf(socksUsersBean)
            }
        }

        return outboundBean
    }


}