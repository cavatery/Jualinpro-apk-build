package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiContentRequest(
    val contents: List<ContentItem>,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    val role: String = "user",
    val parts: List<ContentPart>
)

@JsonClass(generateAdapter = true)
data class ContentPart(
    val text: String? = null,
    @property:Json(name = "inline_data") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @property:Json(name = "mime_type") val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 4096
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<CandidateItem>? = null,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    val content: CandidateContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class CandidateContent(
    val parts: List<CandidatePart>? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class CandidatePart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
