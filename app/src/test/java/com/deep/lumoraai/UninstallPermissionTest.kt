package com.deep.lumoraai

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class UninstallPermissionTest {
    @Test fun modernAndroidUninstallerReceivesRequiredPermission() {
        val manifest = listOf(File("src/main/AndroidManifest.xml"), File("app/src/main/AndroidManifest.xml")).first { it.exists() }
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val permissions = factory.newDocumentBuilder().parse(manifest).getElementsByTagName("uses-permission")
        assertTrue("Android's uninstaller rejects callers targeting API 28+ without REQUEST_DELETE_PACKAGES",
            (0 until permissions.length).any {
                permissions.item(it).attributes.getNamedItemNS("http://schemas.android.com/apk/res/android", "name")?.nodeValue ==
                    "android.permission.REQUEST_DELETE_PACKAGES"
            })
    }
}
