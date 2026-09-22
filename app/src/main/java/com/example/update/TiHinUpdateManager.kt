package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

private const val LATEST_RELEASE_URL =
    "https://api.github.com/repos/murmusuvash2-wq/Android-app/releases/latest"

internal data class TiHinUpdateInfo(
    val versionName: String,
    val apkUrl: String,
    val releaseUrl: String,
    val releaseNotes: String?
)

internal object TiHinUpdateManager {
    private val client = OkHttpClient()

    suspend fun checkForUpdate(currentVersionName: String): TiHinUpdateInfo? =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(LATEST_RELEASE_URL)
                    .header("Accept", "application/vnd.github+json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val body = response.body?.string() ?: return@use null
                    val json = JSONObject(body)

                    if (json.optBoolean("draft", false) || json.optBoolean("prerelease", false)) {
                        return@use null
                    }

                    val versionName = json.optString("tag_name").removePrefix("v").trim()
                    val apkUrl = json.optJSONArray("assets")?.let { assets ->
                        (0 until assets.length())
                            .map { assets.getJSONObject(it) }
                            .firstOrNull {
                                it.optString("name").endsWith(".apk", ignoreCase = true)
                            }?.optString("browser_download_url")
                    }.orEmpty()

                    if (versionName.isBlank() || apkUrl.isBlank()) return@use null
                    if (compareVersions(versionName, currentVersionName) <= 0) return@use null

                    TiHinUpdateInfo(
                        versionName = versionName,
                        apkUrl = apkUrl,
                        releaseUrl = json.optString("html_url"),
                        releaseNotes = json.optString("body").takeIf { it.isNotBlank() }
                    )
                }
            }.getOrNull()
        }

    suspend fun downloadAndInstall(context: Context, update: TiHinUpdateInfo): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder().url(update.apkUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use false
                    val body = response.body ?: return@use false
                    val file = File(context.cacheDir, "tihin-update.apk")
                    file.outputStream().use { output ->
                        body.byteStream().use { input -> input.copyTo(output) }
                    }
                    installApk(context, file)
                    true
                }
            }.getOrDefault(false)
        }

    private fun installApk(context: Context, apk: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun compareVersions(remote: String, local: String): Int {
        fun parts(value: String) = value.split(".").map {
            it.takeWhile(Char::isDigit).toIntOrNull() ?: 0
        }
        val a = parts(remote)
        val b = parts(local)
        for (i in 0 until maxOf(a.size, b.size)) {
            val av = a.getOrElse(i) { 0 }
            val bv = b.getOrElse(i) { 0 }
            if (av != bv) return av.compareTo(bv)
        }
        return 0
    }
}

@Composable
fun TiHinUpdatePrompt(currentVersionName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var update by remember { mutableStateOf<TiHinUpdateInfo?>(null) }
    var checking by remember { mutableStateOf(true) }
    var downloading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(currentVersionName) {
        checking = true
        update = TiHinUpdateManager.checkForUpdate(currentVersionName)
        checking = false
    }

    if (checking || update == null) return
    val availableUpdate = update ?: return

    AlertDialog(
        onDismissRequest = { if (!downloading) update = null },
        title = { Text("Update available", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("A newer version of TiHin is ready.")
                Text(
                    "Version ${availableUpdate.versionName}",
                    style = MaterialTheme.typography.titleMedium
                )
                availableUpdate.releaseNotes?.let { notes ->
                    Spacer(Modifier.height(4.dp))
                    Text(notes.take(240), style = MaterialTheme.typography.bodySmall)
                }
                if (downloading) {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()
                    Text("Downloading update…")
                }
                if (error) {
                    Text(
                        "Update download failed. Please try again.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !downloading,
                onClick = {
                    error = false
                    downloading = true
                    scope.launch {
                        val success = TiHinUpdateManager.downloadAndInstall(context, availableUpdate)
                        downloading = false
                        if (!success) error = true
                    }
                }
            ) {
                Text(if (downloading) "Downloading…" else "Download & Install")
            }
        },
        dismissButton = {
            TextButton(enabled = !downloading, onClick = { update = null }) {
                Text("Later")
            }
        }
    )
}
