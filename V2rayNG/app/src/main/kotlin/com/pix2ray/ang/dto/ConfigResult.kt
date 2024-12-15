package com.pix2ray.ang.dto

data class ConfigResult(
    var status: Boolean,
    var guid: String? = null,
    var content: String = "",
    var domainPort: String? = null,
)

