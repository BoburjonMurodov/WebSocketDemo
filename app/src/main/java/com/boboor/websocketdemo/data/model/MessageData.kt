package com.boboor.websocketdemo.data.model


/*
    Created by Boburjon Murodov 27/02/25 at 11:42
*/

private var privateId = 0

data class MessageData(
    val id: Int = privateId++,
    val message: String,
    val type: MessageType
)


enum class MessageType {
    INCOMING, OUTGOING
}