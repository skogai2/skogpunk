package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiPersona
import com.example.data.ChatMessage
import com.example.data.GeminiModelOption
import com.example.engine.ScriptParseResult
import com.example.engine.SkogScriptParser
import com.example.model.Quest
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFg2
import com.example.ui.theme.SkFgDim
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonAcid
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonGreen
import com.example.ui.theme.SkNeonMagenta
import com.example.ui.theme.SkNeonRed
import com.example.ui.theme.SkNeonViolet
import com.example.ui.theme.SkNeonYellow

@Composable
fun AiCompanionScreen(
    selectedPersona: AiPersona,
    selectedModel: GeminiModelOption,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    onSelectPersona: (AiPersona) -> Unit,
    onSelectModel: (GeminiModelOption) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onInstallGeneratedModAndPlay: (Quest) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Header & Model Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SKOG-AI NEURAL TERMINAL",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SkFg
                )
                Text(
                    text = "MULTI-TURN GEMINI COMPANION & MOD ARCHITECT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = SkNeonMagenta
                )
            }

            IconButton(
                onClick = onClearChat,
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, Color(0xFF4A3580), RoundedCornerShape(4.dp))
                    .testTag("clear_ai_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat History",
                    tint = SkFgMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 2. Role / Persona Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AiPersona.entries.forEach { persona ->
                val isSelected = selectedPersona == persona
                val accent = when (persona) {
                    AiPersona.MOD_ARCHITECT -> SkNeonMagenta
                    AiPersona.SKOGIX_BUDDY -> SkNeonCyan
                    AiPersona.ROOT_SENSEI -> SkNeonGreen
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (isSelected) accent else Color(0xFF3A2860),
                            RoundedCornerShape(4.dp)
                        )
                        .background(
                            if (isSelected) SkCard2 else SkCard,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectPersona(persona) }
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                        .testTag("persona_chip_${persona.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = persona.displayName,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (isSelected) accent else SkFg2
                        )
                        Text(
                            text = persona.badge,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = SkFgMuted
                        )
                    }
                }
            }
        }

        // 3. Gemini Model Tier Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MODEL:",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = SkFgMuted
            )
            GeminiModelOption.entries.forEach { modelOpt ->
                val active = selectedModel == modelOpt
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (active) SkNeonYellow else Color(0xFF332354),
                            RoundedCornerShape(3.dp)
                        )
                        .background(
                            if (active) Color(0xFF281D46) else Color(0xFF140C26),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable { onSelectModel(modelOpt) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("model_chip_${modelOpt.name.lowercase()}")
                ) {
                    Text(
                        text = "${modelOpt.label} (${modelOpt.roleTag})",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 9.sp,
                        color = if (active) SkNeonYellow else SkFgMuted
                    )
                }
            }
        }

        // 4. Scrollable Multi-Turn Conversation Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color(0xFF352458), RoundedCornerShape(4.dp))
                .background(Color(0xFF0A0614), RoundedCornerShape(4.dp))
                .padding(10.dp)
                .testTag("ai_chat_message_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onInstallModAndPlay = onInstallGeneratedModAndPlay
                )
            }
        }

        // 5. Quick Prompt Suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickPrompts = listOf(
                "⚡ Generate a Docker & SSH quest mod" to "Create a new 2-step @quest mod in SkogScript about Docker containers and SSH keys.",
                "💡 Hint for current contract" to "Give me a friendly hint for my current active contract step without spoiling the exact answer.",
                "🔥 Boss SQL Injection Quest" to "Build an intense 3-step @quest mod in SkogScript for Honkers & Hackers about SQL security and curl.",
                "📖 Explain bash for-loops" to "Explain how bash parameter expansion like \${f%.jpeg}.jpg works in plain terms."
            )
            quickPrompts.forEach { (chipLabel, fullPrompt) ->
                Box(
                    modifier = Modifier
                        .border(1.dp, SkNeonCyan.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                        .background(SkCard, RoundedCornerShape(3.dp))
                        .clickable(enabled = !isGenerating) {
                            onSendMessage(fullPrompt)
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = chipLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SkNeonCyan
                    )
                }
            }
        }

        // 6. Message Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = {
                    Text(
                        text = "Ask ${selectedPersona.displayName} or describe a mod to script...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SkFgDim
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputPrompt.isNotBlank() && !isGenerating) {
                            onSendMessage(inputPrompt.trim())
                            inputPrompt = ""
                        }
                    }
                ),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = SkFg
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkNeonMagenta,
                    unfocusedBorderColor = Color(0xFF4A3580),
                    focusedContainerColor = Color(0xFF140D28),
                    unfocusedContainerColor = Color(0xFF140D28)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (inputPrompt.isNotBlank() && !isGenerating) {
                        onSendMessage(inputPrompt.trim())
                        inputPrompt = ""
                    }
                },
                enabled = inputPrompt.isNotBlank() && !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkNeonMagenta,
                    contentColor = Color(0xFF1A0420)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("ai_chat_send_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = SkNeonCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onInstallModAndPlay: (Quest) -> Unit
) {
    val borderColor = when {
        message.isError -> SkNeonRed
        message.isUser -> SkNeonViolet
        else -> SkNeonCyan
    }
    val bgColor = when {
        message.isError -> Color(0xFF2E1019)
        message.isUser -> Color(0xFF21143E)
        else -> Color(0xFF121A30)
    }

    val extractedScript = remember(message.text) {
        if (!message.isUser) SkogScriptParser.extractScriptFromText(message.text) else null
    }
    val parsedQuest = remember(extractedScript) {
        extractedScript?.let {
            (SkogScriptParser.parse(it, "AI_ARCHITECT") as? ScriptParseResult.Success)?.quest
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (message.isUser) Icons.Default.AutoAwesome else Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = message.authorName.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = borderColor
                )
            }
            if (message.isStreaming) {
                Text(
                    text = "STREAMING…",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = SkNeonYellow
                )
            }
        }

        Text(
            text = message.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = if (message.isError) SkNeonRed else SkFg
        )

        // If message contains a valid playable @quest SkogScript, offer 1-Click Install to SQL & Play!
        if (parsedQuest != null && !message.isStreaming) {
            Button(
                onClick = { onInstallModAndPlay(parsedQuest) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkNeonAcid,
                    contentColor = Color(0xFF061A10)
                ),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("install_ai_mod_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "INSTALL '${parsedQuest.title.uppercase()}' TO SQL & PLAY (${parsedQuest.stepCount} STEPS)",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
