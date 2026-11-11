package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Cabinet
import com.example.model.GamePhase
import com.example.model.Step
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFg2
import com.example.ui.theme.SkFgDim
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonGreen
import com.example.ui.theme.SkNeonRed
import com.example.ui.theme.SkNeonYellow

@Composable
fun ContractDialogueCard(
    step: Step,
    stepIndex: Int,
    phase: GamePhase,
    cabinet: Cabinet,
    userAnswer: Any?,
    isCorrect: Boolean?,
    onAnswerSubmitted: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember(step) { mutableStateOf("") }
    var showHint by remember(step) { mutableStateOf(false) }

    val isRevealed = phase == GamePhase.REVEAL || phase == GamePhase.DONE

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cabinet.borderColor, RoundedCornerShape(4.dp))
            .background(SkCard, RoundedCornerShape(4.dp))
            .padding(14.dp)
    ) {
        // Contract header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(1.dp, cabinet.primaryColor, RoundedCornerShape(2.dp))
                    .background(SkCard2, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (step is Step.Text) Icons.Default.Code else Icons.Default.Key,
                    contentDescription = "Contract Icon",
                    tint = cabinet.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CONTRACT ${stepIndex + 1} · ${step.xp} XP",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = cabinet.primaryColor
                )
            }

            if (step is Step.Text && step.hint != null) {
                IconButton(
                    onClick = { showHint = !showHint },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Show Hint",
                        tint = if (showHint) SkNeonYellow else SkFgMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Contract Prompt
        Text(
            text = step.prompt,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = SkFg
        )

        // Optional Hint
        AnimatedVisibility(visible = showHint && step is Step.Text) {
            val hintText = (step as? Step.Text)?.hint ?: ""
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(Color(0xFF221A3A), RoundedCornerShape(2.dp))
                    .border(1.dp, SkNeonYellow.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "HINT: $hintText",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SkNeonYellow
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Choices or Text Terminal
        when (step) {
            is Step.Choice -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    step.choices.forEachIndexed { index, choiceText ->
                        val isSelected = userAnswer == index
                        val isCorrectAnswer = step.answerIndex == index

                        val itemBorderColor = when {
                            isRevealed && isCorrectAnswer -> SkNeonGreen
                            isRevealed && isSelected && !isCorrectAnswer -> SkNeonRed
                            isSelected -> cabinet.primaryColor
                            else -> Color(0xFF3B2961)
                        }

                        val itemBgColor = when {
                            isRevealed && isCorrectAnswer -> Color(0xFF133622)
                            isRevealed && isSelected && !isCorrectAnswer -> Color(0xFF38151D)
                            isSelected -> SkCard2
                            else -> Color(0xFF1E1438)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, itemBorderColor, RoundedCornerShape(3.dp))
                                .background(itemBgColor, RoundedCornerShape(3.dp))
                                .clickable(enabled = !isRevealed && userAnswer == null) {
                                    onAnswerSubmitted(index)
                                }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${index + 1}]",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isRevealed && isCorrectAnswer) SkNeonGreen else cabinet.accentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = choiceText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = SkFg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            is Step.Text -> {
                Column {
                    if (isRevealed) {
                        // Terminal command result
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isCorrect == true) SkNeonGreen else SkNeonRed,
                                    RoundedCornerShape(3.dp)
                                )
                                .background(Color(0xFF0F0B1E), RoundedCornerShape(3.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "$ ${userAnswer ?: "???"}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = if (isCorrect == true) SkNeonGreen else SkNeonRed
                                )
                                if (isCorrect != true && step.hint != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "EXPECTED: ${step.hint}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = SkNeonYellow
                                    )
                                }
                            }
                        }
                    } else {
                        // Command input line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = {
                                    Text(
                                        text = "$ command line",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = SkFgDim
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (textInput.isNotBlank()) onAnswerSubmitted(textInput)
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = cabinet.primaryColor,
                                    unfocusedBorderColor = Color(0xFF4A3580),
                                    focusedTextColor = SkFg,
                                    unfocusedTextColor = SkFg2,
                                    focusedContainerColor = Color(0xFF140D28),
                                    unfocusedContainerColor = Color(0xFF140D28)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("terminal_command_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (textInput.isNotBlank()) onAnswerSubmitted(textInput)
                                },
                                enabled = textInput.isNotBlank() && userAnswer == null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cabinet.primaryColor,
                                    contentColor = Color(0xFF1A0420)
                                ),
                                shape = RoundedCornerShape(3.dp),
                                modifier = Modifier.testTag("run_command_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "RUN",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
