package com.chalkak.recap.core.design.qa

import androidx.compose.ui.tooling.preview.Preview

/**
 * Design QA phone matrix: 320/360/412 × fontScale 1.0/1.3/1.5 (9 previews).
 * Spec: `docs/qa/GUIDE.md` §3.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS,
)
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "320x640-font100",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.0f,
    showBackground = true,
)
@Preview(
    name = "320x640-font130",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.3f,
    showBackground = true,
)
@Preview(
    name = "320x640-font150",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f,
    showBackground = true,
)
@Preview(
    name = "360x800-font100",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.0f,
    showBackground = true,
)
@Preview(
    name = "360x800-font130",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.3f,
    showBackground = true,
)
@Preview(
    name = "360x800-font150",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f,
    showBackground = true,
)
@Preview(
    name = "412x915-font100",
    widthDp = 412,
    heightDp = 915,
    fontScale = 1.0f,
    showBackground = true,
)
@Preview(
    name = "412x915-font130",
    widthDp = 412,
    heightDp = 915,
    fontScale = 1.3f,
    showBackground = true,
)
@Preview(
    name = "412x915-font150",
    widthDp = 412,
    heightDp = 915,
    fontScale = 1.5f,
    showBackground = true,
)
annotation class QaPhoneMatrix
