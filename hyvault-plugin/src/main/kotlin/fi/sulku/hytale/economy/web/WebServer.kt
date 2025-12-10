package fi.sulku.hytale.economy.web

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.logging.Logger

class WebServer(port: Int, private val logger: Logger) {
    private val server = HttpServer.create(InetSocketAddress(port), 0).apply {
        createContext("/") { ex ->
            val path = "/web" + if (ex.requestURI.path == "/") "/index.html" else ex.requestURI.path
            WebServer::class.java.getResourceAsStream(path)?.use { input ->
                ex.responseHeaders.add("Content-Type", mimeType(path))
                ex.sendResponseHeaders(200, 0)
                input.copyTo(ex.responseBody)
            } ?: ex.sendResponseHeaders(404, -1)
            ex.close()
        }
    }

    fun start() = server.start().also { logger.info("Web server started on ${server.address.port}") }
    fun stop() = server.stop(0).also { logger.info("Web server stopped") }

    private fun mimeType(path: String) = when (path.substringAfterLast('.', "")) {
        "html" -> "text/html"; "js" -> "application/javascript"; "css" -> "text/css"
        "wasm" -> "application/wasm"; "png" -> "image/png"; "json" -> "application/json"
        else -> "application/octet-stream"
    }
}
