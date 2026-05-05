package ai.opencyvis.config

import android.content.Context
import android.content.SharedPreferences

class ConfigRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "opencyvis_config"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_MAX_STEPS = "max_steps"
        private const val KEY_API_PROVIDER = "api_provider"
        private const val KEY_DEBUG_MODE = "debug_mode"
        private const val KEY_WIRE_API = "wire_api"
        private const val KEY_REASONING_EFFORT = "reasoning_effort"

        const val PROVIDER_OPENAI = "openai"
        const val PROVIDER_ANTHROPIC = "anthropic"
        const val PROVIDER_OLLAMA = "ollama"

        // LLM wire protocol
        const val WIRE_API_AUTO = "auto"          // try /responses first, fall back to /chat/completions
        const val WIRE_API_RESPONSES = "responses"
        const val WIRE_API_CHAT_COMPLETIONS = "chat_completions"
        const val DEFAULT_MODEL = "gpt-5.5"
        const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
        const val DEFAULT_ANTHROPIC_MODEL = "claude-sonnet-4-7-20250415"
        const val DEFAULT_ANTHROPIC_BASE_URL = "https://api.anthropic.com"
        const val DEFAULT_OLLAMA_MODEL = "gemma4:26b-a4b-it-q4_K_M"
        const val DEFAULT_OLLAMA_BASE_URL = "https://localhost:11434"
        const val DEFAULT_MAX_STEPS = 100

        /** Returns true if model or url matches any known provider default.
         *  SettingsActivity uses this to decide whether to auto-fill defaults when
         *  the provider spinner changes. */
        fun isDefaultModelOrUrl(model: String, url: String): Boolean =
            model in listOf(DEFAULT_MODEL, DEFAULT_ANTHROPIC_MODEL, DEFAULT_OLLAMA_MODEL) ||
            url in listOf(DEFAULT_BASE_URL, DEFAULT_ANTHROPIC_BASE_URL, DEFAULT_OLLAMA_BASE_URL)
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var apiProvider: String
        get() = prefs.getString(KEY_API_PROVIDER, PROVIDER_OPENAI) ?: PROVIDER_OPENAI
        set(value) = prefs.edit().putString(KEY_API_PROVIDER, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var model: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var maxSteps: Int
        get() = prefs.getInt(KEY_MAX_STEPS, DEFAULT_MAX_STEPS)
        set(value) = prefs.edit().putInt(KEY_MAX_STEPS, value).apply()

    var debugMode: Boolean
        get() = prefs.getBoolean(KEY_DEBUG_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DEBUG_MODE, value).apply()

    /**
     * Which OpenAI-compatible wire API to call.
     * - auto: try /responses, fall back to /chat/completions
     * - responses: force /responses
     * - chat_completions: force /chat/completions
     */
    var wireApi: String
        get() = prefs.getString(KEY_WIRE_API, WIRE_API_AUTO) ?: WIRE_API_AUTO
        set(value) = prefs.edit().putString(KEY_WIRE_API, value).apply()

    /**
     * Optional reasoning effort hint for models that support it (e.g. GPT via Responses API).
     * Expected values: low/medium/high/xhigh (provider-specific).
     */
    var reasoningEffort: String
        get() = prefs.getString(KEY_REASONING_EFFORT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_REASONING_EFFORT, value).apply()
}
