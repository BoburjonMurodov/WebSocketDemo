package com.boboor.websocketdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.boboor.websocketdemo.data.model.MessageData
import com.boboor.websocketdemo.data.model.MessageType
import com.boboor.websocketdemo.data.socket.SocketService
import com.boboor.websocketdemo.ui.theme.WebSocketDemoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val service = SocketService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            val listState = rememberLazyListState()

            val messagesList = remember { mutableStateListOf<MessageData>() }
            LaunchedEffect(Unit) {
                service.receiveMessage()
                    .collect { message ->
                        messagesList.add(message)
                        listState.animateScrollToItem(messagesList.size, scrollOffset = 100)
                    }
            }
            LaunchedEffect(Unit) {
                service.errorFlow.collect {
                    messagesList.add(it)
                    listState.animateScrollToItem(messagesList.size, scrollOffset = 100)
                }
            }

            WebSocketDemoTheme {
                Scaffold(
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .statusBarsPadding()
                                .padding(16.dp),
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.weight(1f))

                            Text(
                                text = "WebSocket Demo",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(Modifier.weight(1f))

                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null
                            )
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(it)
                            .imePadding()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f),
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messagesList.size, key = { messagesList[it].id }) { index ->
                                Row(modifier = Modifier.animateItem()) {
                                    if (messagesList[index].type == MessageType.INCOMING) {

                                        Box(
                                            modifier = Modifier
                                                .widthIn(20.dp, 300.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                messagesList[index].message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))

                                        Box(
                                            modifier = Modifier
                                                .widthIn(20.dp, 300.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                                .padding(horizontal = 16.dp, vertical = 8.dp)

                                        ) {
                                            Text(
                                                messagesList[index].message,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )

                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val textMessage = remember { mutableStateOf("") }
                            TextField(
                                placeholder = {
                                    Text(
                                        "send Message",
                                        color = Color.Gray
                                    )
                                },
                                value = textMessage.value,
                                onValueChange = {
                                    textMessage.value = it
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .heightIn(56.dp, 200.dp)
                                    .weight(1f)
                                    .onKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                                            scope.launch {
                                                val text = textMessage.value.trim()
                                                if (text.isNotEmpty()) {
                                                    service.sendMessage(text)
                                                    messagesList.add(MessageData(message = text, type = MessageType.OUTGOING))
                                                    textMessage.value = ""
                                                }
                                            }
                                            true
                                        } else false
                                    }
                            )

                            Spacer(Modifier.width(16.dp))

                            Button(onClick = {
                                scope.launch {
                                    val text = textMessage.value.trim()
                                    if (text.isNotEmpty()) {
                                        service.sendMessage(text)
                                        messagesList.add(MessageData(message = text, type = MessageType.OUTGOING))
                                        textMessage.value = ""
                                    }
                                }
                            }) {
                                Text("Send")
                            }
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                }
            }
        }
    }


}
