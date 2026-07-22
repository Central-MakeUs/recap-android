package com.chalkak.recap.core.data.screenshot.permission

import com.chalkak.recap.core.model.ImageAccessLevel

interface ImagePermissionRepository {
    fun imagePermissionRequest(): Array<String>

    fun currentImageAccessLevel(): ImageAccessLevel
}
