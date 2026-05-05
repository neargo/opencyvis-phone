package ai.opencyvis.config

object ConfigDeepLink {
    data class ImportedConfig(
        val provider: String,
        val apiKey: String,
        val model: String,
        val baseUrl: String,
        val maxSteps: Int?,
        val wireApi: String,
        val reasoningEffort: String
    )

    fun parse(params: Map<String, String?>): Result<ImportedConfig> {
        val provider = params["provider"]?.trim()?.lowercase().orEmpty()
            .ifEmpty { ConfigRepository.PROVIDER_OPENAI }

        if (provider !in supportedProviders) {
            return Result.failure(IllegalArgumentException("Unsupported provider: $provider"))
        }

        val maxSteps = params["max_steps"]?.trim()?.takeIf { it.isNotEmpty() }?.let { raw ->
            raw.toIntOrNull()?.takeIf { it >= 1 }
                ?: return Result.failure(IllegalArgumentException("max_steps must be a positive integer"))
        }

        val wireApi = firstParam(params, "wire_api", "wire")?.trim()?.lowercase().orEmpty()
            .ifEmpty { ConfigRepository.WIRE_API_AUTO }
        if (wireApi !in setOf(
                ConfigRepository.WIRE_API_AUTO,
                ConfigRepository.WIRE_API_RESPONSES,
                ConfigRepository.WIRE_API_CHAT_COMPLETIONS
            )
        ) {
            return Result.failure(IllegalArgumentException("Unsupported wire_api: $wireApi"))
        }

        val reasoningEffort = firstParam(params, "reasoning_effort", "effort")?.trim()?.lowercase().orEmpty()

        val (defaultModel, defaultBaseUrl) = defaultsFor(provider)
        val baseUrl = firstParam(params, "base_url", "url")?.trim()?.ifEmpty { null } ?: defaultBaseUrl
        val apiKey = firstParam(params, "api_key", "key")?.trim().orEmpty()

        // Key is mandatory for Anthropic and for public OpenAI default endpoint.
        // For internal OpenAI-compatible proxies, allow empty and omit Authorization header.
        if (provider == ConfigRepository.PROVIDER_ANTHROPIC && apiKey.isEmpty()) {
            return Result.failure(IllegalArgumentException("API key is required for $provider"))
        }
        if (provider == ConfigRepository.PROVIDER_OPENAI) {
            val isLikelyPublicOpenAI = baseUrl.contains("openai.com", ignoreCase = true)
            if (isLikelyPublicOpenAI && apiKey.isEmpty()) {
                return Result.failure(IllegalArgumentException("API key is required for $provider"))
            }
        }

        return Result.success(
            ImportedConfig(
                provider = provider,
                apiKey = apiKey,
                model = params["model"]?.trim()?.ifEmpty { null } ?: defaultModel,
                baseUrl = baseUrl,
                maxSteps = maxSteps,
                wireApi = wireApi,
                reasoningEffort = reasoningEffort
            )
        )
    }

    fun redactedApiKey(apiKey: String): String {
        if (apiKey.isBlank()) return "(empty)"
        if (apiKey.length <= 8) return "..."
        return "${apiKey.take(3)}...${apiKey.takeLast(4)}"
    }

    private fun defaultsFor(provider: String): Pair<String, String> = when (provider) {
        ConfigRepository.PROVIDER_ANTHROPIC ->
            ConfigRepository.DEFAULT_ANTHROPIC_MODEL to ConfigRepository.DEFAULT_ANTHROPIC_BASE_URL
        ConfigRepository.PROVIDER_OLLAMA ->
            ConfigRepository.DEFAULT_OLLAMA_MODEL to ConfigRepository.DEFAULT_OLLAMA_BASE_URL
        else -> ConfigRepository.DEFAULT_MODEL to ConfigRepository.DEFAULT_BASE_URL
    }

    private fun firstParam(params: Map<String, String?>, vararg names: String): String? =
        names.firstNotNullOfOrNull { params[it] }

    private val supportedProviders = setOf(
        ConfigRepository.PROVIDER_OPENAI,
        ConfigRepository.PROVIDER_ANTHROPIC,
        ConfigRepository.PROVIDER_OLLAMA
    )
}
