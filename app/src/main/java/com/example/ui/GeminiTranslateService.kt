package com.example.ui

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiTranslateService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun translateText(text: String, sourceLang: String, targetLang: String): String = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error_No_Key"
        }

        val prompt = if (sourceLang == "Auto") {
            """
            Your task is to detect the language of the provided text and translate it to $targetLang.
            Ensure the translation is perfectly accurate, contextually natural, flowing, and preserves any formatting or line breaks.
            If the target is Arabic, utilize eloquent, premium Arabic syntax that is clean and visually comfortable.
            Only output the translated text. Do NOT add notes, explanations, intro, outro or warnings.
            
            Text:
            $text
            """.trimIndent()
        } else {
            """
            Your task is to translate the provided text from $sourceLang to $targetLang.
            Ensure the translation is perfectly accurate, contextually natural, flowing, and preserves any formatting or line breaks.
            If the target is Arabic, utilize eloquent, premium Arabic syntax that is clean and visually comfortable.
            Only output the translated text. Do NOT add notes, explanations, intro, outro or warnings.
            
            Text:
            $text
            """.trimIndent()
        }

        try {
            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            
            // Build request object using org.json.JSONObject to be absolutely clean and safe
            val requestBodyJson = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e("GeminiTranslate", "Error response: $errBody")
                    return@withContext "خطأ في الشبكة مبرهناً برمز الخطأ: ${response.code}"
                }
                
                val bodyString = response.body?.string() ?: ""
                val jsonObj = JSONObject(bodyString)
                val candidates = jsonObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    if (contentObj != null) {
                        val parts = contentObj.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val textResult = parts.getJSONObject(0).optString("text", "")
                            if (textResult.isNotEmpty()) {
                                return@withContext textResult.trim()
                            }
                        }
                    }
                }
                return@withContext "عذراً، لم تنجح الاستجابة، يرجى المحاولة لتأكيد النص المدخل."
            }
        } catch (e: Exception) {
            Log.e("GeminiTranslate", "Exception in translate", e)
            return@withContext "فشل الاتصال: ${e.localizedMessage ?: "حدث خطأ غير معروف"}"
        }
    }
}
