package moe.bitt.plugin.sentry

class SentryConfig {
    var dsn: String? = null
    var isDevelopment: Boolean = false

    fun setDSN(dsn: String) {
        this.dsn = dsn
    }

    fun setIsDevelopment(isDevelopment: Boolean) {
        this.isDevelopment = isDevelopment
    }
}
