package com.boboor.websocketdemo.data.socket

import android.util.Log
import com.boboor.websocketdemo.data.model.MessageData
import com.boboor.websocketdemo.data.model.MessageType
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


/*
    Created by Boburjon Murodov 27/02/25 at 11:19
*/

class SocketService {
    private val client = HttpClient(
        {
            install(WebSockets)
        }
    )

    private lateinit var service: WebSocketSession
    val errorFlow = MutableSharedFlow<MessageData>()

    fun receiveMessage() = flow {
        service = client.webSocketSession {
            url("wss://echo.websocket.org")
        }

        service.incoming.consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .collect {
                Log.d("TTT", "incoming message: ${it.readText()}")
                emit(MessageData(message = it.readText(), type = MessageType.INCOMING))
            }
    }.flowOn(Dispatchers.IO).catch {
        errorFlow.emit(MessageData(message = it.message ?: "Socket Error", type = MessageType.INCOMING))
    }

    suspend fun sendMessage(message: String) {
        try {
            Log.d("TTT", "outgoing message: $message")
            service.send(Frame.Text(message))

        } catch (e: Exception) {
            Log.d("TTT", "error: ${e.message}")
            errorFlow.emit(MessageData(message = e.message ?: "Socket Error", type = MessageType.INCOMING))
        }
    }
}

