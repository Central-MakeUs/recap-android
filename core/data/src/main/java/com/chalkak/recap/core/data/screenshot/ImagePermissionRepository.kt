package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.ImageAccessLevel

interface ImagePermissionRepository {
    fun imagePermissionRequest(): Array<String>

    fun currentImageAccessLevel(): ImageAccessLevel
}
