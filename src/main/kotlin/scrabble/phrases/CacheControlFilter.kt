package scrabble.phrases

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider
import java.util.regex.Pattern

@Provider
class CacheControlFilter : ContainerResponseFilter {

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        val uriInfo = requestContext.uriInfo ?: return
        val path = normalizePath(uriInfo.path) ?: return
        if (path.startsWith("api/") && responseContext.status in 200..299) {
            if (isNoCacheEndpoint(path)) {
                applyNoCacheHeaders(responseContext)
            } else {
                responseContext.headers.putSingle(
                    "Cache-Control", "public, max-age=$MAX_AGE_SECONDS, must-revalidate"
                )
            }
        }
    }

    private fun normalizePath(raw: String): String? {
        val withoutQuery = raw.substringBefore('?', "")
        return if (withoutQuery.isBlank()) null else withoutQuery.trimEnd('/')
    }

    private fun applyNoCacheHeaders(context: ContainerResponseContext) {
        context.headers.putSingle("Cache-Control", "no-cache, no-store")
    }

    /** Explicit allowlist of paths that must never be cached. */
    private fun isNoCacheEndpoint(path: String): Boolean = path in NO_CACHE_PATHS

    companion object {
        private const val MAX_AGE_SECONDS = 180

        private val NO_CACHE_PATHS = setOf(
            "/api/health",
            "/api/ping",
            "/api/ready",
            "/api/live"
        )
    }
}
