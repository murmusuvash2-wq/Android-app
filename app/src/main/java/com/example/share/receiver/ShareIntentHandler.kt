package com.example.share.receiver

import android.content.Intent
import android.net.Uri
import com.example.share.model.SharedProductInput

/**
 * Result of extracting shared content from an incoming Android Intent.
 */
sealed interface ShareIntentResult {
    /**
     * Successfully extracted a valid shared product link.
     */
    data class ValidTextShare(val input: SharedProductInput) : ShareIntentResult

    /**
     * Successfully extracted a shared image (preserves existing photo share flow).
     */
    data class ValidImageShare(val imageUri: Uri) : ShareIntentResult

    /**
     * Shared payload was present but did not contain a recognizable URL.
     */
    data class Malformed(val rawContent: String?) : ShareIntentResult

    /**
     * Shared payload was empty or null.
     */
    object Empty : ShareIntentResult

    /**
     * Intent was not an ACTION_SEND intent.
     */
    object NotShareIntent : ShareIntentResult
}

/**
 * Intent handler responsible for parsing incoming ACTION_SEND intents into domain models.
 * Isolates Android Intent extras and MIME type logic from the rest of the application.
 */
object ShareIntentHandler {

    private val URL_REGEX = """(https?://[^\s<>"{}|\\^`]+)""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Parses an incoming Intent into a strongly typed ShareIntentResult.
     */
    fun extract(intent: Intent?): ShareIntentResult {
        if (intent == null || intent.action != Intent.ACTION_SEND) {
            return ShareIntentResult.NotShareIntent
        }

        val mimeType = intent.type ?: return ShareIntentResult.Empty

        // Handle Image Share (preserves existing user photo share workflow)
        if (mimeType.startsWith("image/")) {
            @Suppress("DEPRECATION")
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                ?: intent.data
            return if (imageUri != null) {
                ShareIntentResult.ValidImageShare(imageUri)
            } else {
                ShareIntentResult.Empty
            }
        }

        // Handle Text / URL Share
        if (mimeType.startsWith("text/") || mimeType == "*/*") {
            val rawText = intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
                ?: intent.dataString

            if (rawText.isNullOrBlank()) {
                return ShareIntentResult.Empty
            }

            val trimmed = rawText.trim()
            val match = URL_REGEX.find(trimmed)
            if (match != null) {
                val extractedUrl = match.value.trimEnd('.', ',', ';', ')', ']', '>', '\'', '"')
                val host = try {
                    Uri.parse(extractedUrl).host
                } catch (_: Exception) {
                    null
                }

                if (!host.isNullOrBlank() && host.contains(".")) {
                    val callingPkg = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
                        ?: intent.`package`
                    return ShareIntentResult.ValidTextShare(
                        SharedProductInput(
                            sourceUrl = extractedUrl,
                            sourcePackageName = callingPkg,
                            sharedText = trimmed
                        )
                    )
                }
            }
            return ShareIntentResult.Malformed(rawContent = trimmed)
        }

        return ShareIntentResult.NotShareIntent
    }
}
