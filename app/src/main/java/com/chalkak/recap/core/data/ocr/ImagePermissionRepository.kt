package com.chalkak.recap.core.data.ocr

import com.chalkak.recap.core.model.ImageAccessLevel

interface ImagePermissionRepository {
    fun imagePermissionRequest(): Array<String>

    fun currentImageAccessLevel(): ImageAccessLevel
}
