package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CyberSynthEngine
import com.example.model.ArenaModifiers
import com.example.model.Cabinet
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private enum class EnemyKind { SEEKER, SNIPER, GUARDIAN }

private class EnemyNode(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var hp: Int = 3,
    var maxHp: Int = 3,
    var kind: EnemyKind = EnemyKind.SEEKER,
    var hitFlashTimer: Float = 0f,
    var telegraphTimer: Float = 0f,
    var aimAngle: Float = 0f,
    var active: Boolean = false
)

private class BulletPacket(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isPlayer: Boolean = true,
    var bouncesLeft: Int = 0,
    var active: Boolean = false
)

private class DataShard(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var life: Float = 0f,
    var active: Boolean = false
)

private class FxParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var color: Color = Color.Cyan,
    var life: Float = 0f,
    var maxLife: Float = 0.5f,
    var size: Float = 4f,
    var active: Boolean = false
)

@Composable
fun CyberBreachArena(
    cabinet: Cabinet,
    modifiers: ArenaModifiers,
    isPaused: Boolean,
    stepIndex: Int,
    onShardCollected: (Int) -> Unit,
    onFirewallBreached: () -> Unit,
    onPlayerDefeated: () -> Unit,
    onOpenTerminalModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnShard by rememberUpdatedState(onShardCollected)
    val currentOnBreached by rememberUpdatedState(onFirewallBreached)
    val currentOnDefeated by rememberUpdatedState(onPlayerDefeated)

    // HUD readouts updated without re-allocating simulation pools
    var shieldHp by remember(stepIndex, modifiers.shieldHp) { mutableIntStateOf(modifiers.shieldHp) }
    val maxShieldHp = modifiers.shieldHp
    var breachPercent by remember(stepIndex) { mutableIntStateOf(0) }
    var comboCount by remember(stepIndex) { mutableIntStateOf(1) }
    var waveScore by remember(stepIndex) { mutableIntStateOf(0) }
    var empCooldown by remember { mutableFloatStateOf(0f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    // Simulation state held inside a single remembered state holder so Canvas never recreates pools
    val sim = remember(stepIndex, modifiers) {
        object {
            var initialized = false
            var width = 800f
            var height = 500f

            var playerX = 400f
            var playerY = 350f
            var playerVx = 0f
            var playerVy = 0f
            var targetX: Float? = null
            var targetY: Float? = null

            // Squash & stretch + invulnerability
            var scaleX = 1f
            var scaleY = 1f
            var invulnTimer = 0f
            var dashTimer = 0f

            // Hitstop & screen shake
            var hitstopTimer = 0f
            var shakeMagnitude = 0f

            // Timers
            var fireCooldown = 0f
            var spawnTimer = 0.8f
            var comboDecayTimer = 0f
            var gridScroll = 0f

            // Object pools
            val enemies = Array(20) { EnemyNode() }
            val bullets = Array(60) { BulletPacket() }
            val shards = Array(24) { DataShard() }
            val particles = Array(90) { FxParticle() }

            fun spawnBurst(x: Float, y: Float, color: Color, count: Int, speed: Float = 220f) {
                var spawned = 0
                for (p in particles) {
                    if (!p.active) {
                        val angle = Random.nextFloat() * 6.28318f
                        val spd = (0.3f + Random.nextFloat() * 0.7f) * speed
                        p.x = x
                        p.y = y
                        p.vx = cos(angle) * spd
                        p.vy = sin(angle) * spd
                        p.color = color
                        p.maxLife = 0.28f + Random.nextFloat() * 0.25f
                        p.life = p.maxLife
                        p.size = 3.5f + Random.nextFloat() * 4.5f
                        p.active = true
                        spawned++
                        if (spawned >= count) break
                    }
                }
            }

            fun spawnEnemy() {
                val activeCount = enemies.count { it.active }
                if (activeCount >= modifiers.enemyCount) return
                val slot = enemies.firstOrNull { !it.active } ?: return

                val margin = 48f
                val side = Random.nextInt(3)
                when (side) {
                    0 -> {
                        slot.x = margin + Random.nextFloat() * (width - margin * 2).coerceAtLeast(100f)
                        slot.y = margin
                    }
                    1 -> {
                        slot.x = margin
                        slot.y = margin + Random.nextFloat() * (height * 0.5f)
                    }
                    else -> {
                        slot.x = (width - margin).coerceAtLeast(margin)
                        slot.y = margin + Random.nextFloat() * (height * 0.5f)
                    }
                }
                slot.vx = 0f
                slot.vy = 0f
                val roll = Random.nextFloat()
                slot.kind = when {
                    roll < 0.50f -> EnemyKind.SEEKER
                    roll < 0.82f -> EnemyKind.SNIPER
                    else -> EnemyKind.GUARDIAN
                }
                slot.maxHp = when (slot.kind) {
                    EnemyKind.SEEKER -> 2
                    EnemyKind.SNIPER -> 3
                    EnemyKind.GUARDIAN -> 6
                }
                slot.hp = slot.maxHp
                slot.hitFlashTimer = 0f
                slot.telegraphTimer = if (slot.kind == EnemyKind.SNIPER) 1.6f else 0f
                slot.active = true
            }

            fun triggerEmpDash() {
                if (empCooldown > 0f) return
                empCooldown = 3.2f
                dashTimer = 0.22f
                invulnTimer = 0.45f
                scaleX = 1.45f
                scaleY = 0.65f
                shakeMagnitude = 12f
                CyberSynthEngine.playEmpDash()
                spawnBurst(playerX, playerY, Color(0xFF4EE2FF), 26, 340f)

                // EMP damages nearby enemies and clears enemy bullets
                for (b in bullets) {
                    if (b.active && !b.isPlayer) {
                        val dist = hypot(b.x - playerX, b.y - playerY)
                        if (dist < 220f) {
                            b.active = false
                            spawnBurst(b.x, b.y, Color(0xFF5FFF8A), 4, 100f)
                        }
                    }
                }
                for (e in enemies) {
                    if (e.active) {
                        val dist = hypot(e.x - playerX, e.y - playerY)
                        if (dist < 190f) {
                            e.hp -= 2
                            e.hitFlashTimer = 0.12f
                            if (e.hp <= 0) {
                                e.active = false
                                hitstopTimer = 0.045f
                                spawnBurst(e.x, e.y, cabinet.primaryColor, 18, 260f)
                            }
                        }
                    }
                }
            }
        }
    }

    // 60fps simulation loop
    LaunchedEffect(isPaused, stepIndex, modifiers) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastNanos == 0L) {
                    lastNanos = frameNanos
                    return@withFrameNanos
                }
                // Clamp delta time to max 0.05s (50ms) per game craft rules
                val rawDt = (frameNanos - lastNanos) / 1_000_000_000f
                lastNanos = frameNanos
                if (isPaused || !sim.initialized) return@withFrameNanos

                val dt = min(rawDt, 0.05f)

                // Decay screen shake
                sim.shakeMagnitude = max(0f, sim.shakeMagnitude - dt * 36f)

                // Recover squash-and-stretch toward 1.0
                sim.scaleX += (1f - sim.scaleX) * min(1f, dt * 14f)
                sim.scaleY += (1f - sim.scaleY) * min(1f, dt * 14f)

                if (empCooldown > 0f) {
                    empCooldown = max(0f, empCooldown - dt)
                }
                if (sim.invulnTimer > 0f) {
                    sim.invulnTimer = max(0f, sim.invulnTimer - dt)
                }
                if (sim.dashTimer > 0f) {
                    sim.dashTimer = max(0f, sim.dashTimer - dt)
                }

                // Hitstop freeze for heavy impact feel (30-80ms)
                if (sim.hitstopTimer > 0f) {
                    sim.hitstopTimer = max(0f, sim.hitstopTimer - dt)
                    frameTick = frameNanos
                    return@withFrameNanos
                }

                sim.gridScroll = (sim.gridScroll + dt * 48f) % 48f

                // Combo decay
                if (comboCount > 1) {
                    sim.comboDecayTimer -= dt
                    if (sim.comboDecayTimer <= 0f) {
                        comboCount = max(1, comboCount - 1)
                        sim.comboDecayTimer = 3.5f
                    }
                }

                // Move player toward touch target smoothly
                val tx = sim.targetX
                val ty = sim.targetY
                if (tx != null && ty != null) {
                    val dx = tx - sim.playerX
                    val dy = ty - sim.playerY
                    val dist = hypot(dx, dy)
                    val maxSpeed = if (sim.dashTimer > 0f) 680f else 360f
                    if (dist > 4f) {
                        val moveStep = min(dist, maxSpeed * dt)
                        sim.playerVx = (dx / dist) * maxSpeed
                        sim.playerVy = (dy / dist) * maxSpeed
                        sim.playerX += (dx / dist) * moveStep
                        sim.playerY += (dy / dist) * moveStep
                    } else {
                        sim.playerVx = 0f
                        sim.playerVy = 0f
                    }
                }

                val pad = 26f
                sim.playerX = sim.playerX.coerceIn(pad, (sim.width - pad).coerceAtLeast(pad))
                sim.playerY = sim.playerY.coerceIn(pad, (sim.height - pad).coerceAtLeast(pad))

                // Spawn enemies up to modifiers.enemyCount
                sim.spawnTimer -= dt
                if (sim.spawnTimer <= 0f) {
                    sim.spawnEnemy()
                    sim.spawnTimer = 1.35f / modifiers.enemySpeedMultiplier.coerceAtLeast(0.5f)
                }

                // Auto-fire player pulse cannon toward nearest enemy (or straight up)
                sim.fireCooldown -= dt
                if (sim.fireCooldown <= 0f) {
                    val nearest = sim.enemies
                        .filter { it.active }
                        .minByOrNull { hypot(it.x - sim.playerX, it.y - sim.playerY) }

                    val angle = if (nearest != null) {
                        atan2(nearest.y - sim.playerY, nearest.x - sim.playerX)
                    } else {
                        -1.570796f // Straight up
                    }

                    val bSlot = sim.bullets.firstOrNull { !it.active }
                    if (bSlot != null) {
                        val speed = 580f
                        bSlot.x = sim.playerX + cos(angle) * 18f
                        bSlot.y = sim.playerY + sin(angle) * 18f
                        bSlot.vx = cos(angle) * speed
                        bSlot.vy = sin(angle) * speed
                        bSlot.isPlayer = true
                        bSlot.bouncesLeft = if (modifiers.ricochetShots) 1 else 0
                        bSlot.active = true
                        // Subtle recoil squash
                        sim.scaleX = 0.88f
                        sim.scaleY = 1.14f
                        CyberSynthEngine.playLaser()
                    }
                    sim.fireCooldown = (0.28f / modifiers.playerFireRateMultiplier).coerceIn(0.08f, 0.55f)
                }

                // Update enemies
                val eSpeedBase = 95f * modifiers.enemySpeedMultiplier
                for (e in sim.enemies) {
                    if (!e.active) continue
                    if (e.hitFlashTimer > 0f) e.hitFlashTimer = max(0f, e.hitFlashTimer - dt)

                    val dx = sim.playerX - e.x
                    val dy = sim.playerY - e.y
                    val dist = hypot(dx, dy).coerceAtLeast(1f)
                    val dirX = dx / dist
                    val dirY = dy / dist

                    when (e.kind) {
                        EnemyKind.SEEKER -> {
                            e.vx = dirX * eSpeedBase * 1.15f
                            e.vy = dirY * eSpeedBase * 1.15f
                            e.x += e.vx * dt
                            e.y += e.vy * dt
                        }
                        EnemyKind.SNIPER -> {
                            // Moves until in range, then winds up visible telegraph laser before firing
                            if (dist > 250f && e.telegraphTimer > 0.85f) {
                                e.x += dirX * eSpeedBase * 0.75f * dt
                                e.y += dirY * eSpeedBase * 0.75f * dt
                            }
                            e.aimAngle = atan2(dy, dx)
                            e.telegraphTimer -= dt
                            if (e.telegraphTimer <= 0f) {
                                // Fire sniper packet
                                val shot = sim.bullets.firstOrNull { !it.active }
                                if (shot != null) {
                                    val shotSpd = 340f * modifiers.enemySpeedMultiplier.coerceAtMost(1.6f)
                                    shot.x = e.x
                                    shot.y = e.y
                                    shot.vx = cos(e.aimAngle) * shotSpd
                                    shot.vy = sin(e.aimAngle) * shotSpd
                                    shot.isPlayer = false
                                    shot.bouncesLeft = 0
                                    shot.active = true
                                }
                                e.telegraphTimer = 2.4f
                            }
                        }
                        EnemyKind.GUARDIAN -> {
                            // Heavy armored node that orbits and fires radial pulses
                            val orbitAngle = atan2(dy, dx) + 0.9f
                            e.x += cos(orbitAngle) * eSpeedBase * 0.65f * dt
                            e.y += sin(orbitAngle) * eSpeedBase * 0.65f * dt
                            e.telegraphTimer -= dt
                            if (e.telegraphTimer <= 0f) {
                                for (k in 0 until 4) {
                                    val shot = sim.bullets.firstOrNull { !it.active } ?: break
                                    val a = (k * 1.570796f) + (frameNanos % 1000) * 0.001f
                                    shot.x = e.x
                                    shot.y = e.y
                                    shot.vx = cos(a) * 210f
                                    shot.vy = sin(a) * 210f
                                    shot.isPlayer = false
                                    shot.bouncesLeft = 0
                                    shot.active = true
                                }
                                e.telegraphTimer = 2.8f
                            }
                        }
                    }

                    e.x = e.x.coerceIn(24f, (sim.width - 24f).coerceAtLeast(24f))
                    e.y = e.y.coerceIn(24f, (sim.height - 24f).coerceAtLeast(24f))

                    // Check collision with player drone
                    if (sim.invulnTimer <= 0f && dist < 28f) {
                        sim.invulnTimer = 0.85f
                        sim.shakeMagnitude = 14f
                        sim.hitstopTimer = 0.055f
                        comboCount = 1
                        CyberSynthEngine.playHit()
                        sim.spawnBurst(sim.playerX, sim.playerY, Color(0xFFFF4D6D), 20, 260f)
                        shieldHp = max(0, shieldHp - 20)
                        if (shieldHp <= 0) {
                            currentOnDefeated()
                        }
                    }
                }

                // Update bullets
                for (b in sim.bullets) {
                    if (!b.active) continue
                    b.x += b.vx * dt
                    b.y += b.vy * dt

                    // Boundary check + optional ricochet
                    if (b.x < 8f || b.x > sim.width - 8f) {
                        if (b.bouncesLeft > 0) {
                            b.vx = -b.vx
                            b.bouncesLeft--
                        } else {
                            b.active = false
                            continue
                        }
                    }
                    if (b.y < 8f || b.y > sim.height - 8f) {
                        if (b.bouncesLeft > 0) {
                            b.vy = -b.vy
                            b.bouncesLeft--
                        } else {
                            b.active = false
                            continue
                        }
                    }

                    if (b.isPlayer) {
                        // Hit enemy check
                        for (e in sim.enemies) {
                            if (!e.active) continue
                            val hitRadius = if (e.kind == EnemyKind.GUARDIAN) 26f else 20f
                            if (hypot(b.x - e.x, b.y - e.y) < hitRadius) {
                                b.active = false
                                e.hp -= 1
                                e.hitFlashTimer = 0.09f
                                sim.spawnBurst(b.x, b.y, cabinet.accentColor, 6, 150f)

                                if (e.hp <= 0) {
                                    e.active = false
                                    // Heavy impact hitstop + camera shake + shard drop
                                    sim.hitstopTimer = if (e.kind == EnemyKind.GUARDIAN) 0.065f else 0.035f
                                    sim.shakeMagnitude = if (e.kind == EnemyKind.GUARDIAN) 11f else 6f
                                    CyberSynthEngine.playHit()
                                    sim.spawnBurst(e.x, e.y, cabinet.primaryColor, 22, 280f)

                                    comboCount = (comboCount + 1).coerceAtMost(8)
                                    sim.comboDecayTimer = 4.0f
                                    val points = (if (e.kind == EnemyKind.GUARDIAN) 35 else 15) * comboCount
                                    waveScore += points

                                    val shard = sim.shards.firstOrNull { !it.active }
                                    if (shard != null) {
                                        shard.x = e.x
                                        shard.y = e.y
                                        shard.vx = (Random.nextFloat() - 0.5f) * 60f
                                        shard.vy = (Random.nextFloat() - 0.5f) * 60f
                                        shard.life = 8.0f
                                        shard.active = true
                                    }

                                    val breachGain = if (e.kind == EnemyKind.GUARDIAN) 28 else 16
                                    val newBreach = (breachPercent + breachGain).coerceAtMost(100)
                                    if (newBreach >= 100 && breachPercent < 100) {
                                        breachPercent = 100
                                        CyberSynthEngine.playAccessGranted()
                                        currentOnBreached()
                                    } else {
                                        breachPercent = newBreach
                                    }
                                }
                                break
                            }
                        }
                    } else {
                        // Enemy bullet hitting player
                        if (sim.invulnTimer <= 0f && hypot(b.x - sim.playerX, b.y - sim.playerY) < 18f) {
                            b.active = false
                            sim.invulnTimer = 0.75f
                            sim.shakeMagnitude = 12f
                            sim.hitstopTimer = 0.045f
                            comboCount = 1
                            CyberSynthEngine.playHit()
                            sim.spawnBurst(sim.playerX, sim.playerY, Color(0xFFFF4D6D), 16, 240f)
                            shieldHp = max(0, shieldHp - 15)
                            if (shieldHp <= 0) {
                                currentOnDefeated()
                            }
                        }
                    }
                }

                // Update data shards (gravitate toward player when close)
                for (s in sim.shards) {
                    if (!s.active) continue
                    s.life -= dt
                    if (s.life <= 0f) {
                        s.active = false
                        continue
                    }
                    val dx = sim.playerX - s.x
                    val dy = sim.playerY - s.y
                    val dist = hypot(dx, dy).coerceAtLeast(1f)
                    if (dist < 160f) {
                        s.vx += (dx / dist) * 620f * dt
                        s.vy += (dy / dist) * 620f * dt
                    }
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.vx *= 0.95f
                    s.vy *= 0.95f

                    if (dist < 24f) {
                        s.active = false
                        val xpBonus = 10 * comboCount
                        waveScore += xpBonus
                        CyberSynthEngine.playShardPickup()
                        sim.spawnBurst(s.x, s.y, Color(0xFFFFD23F), 10, 180f)
                        currentOnShard(xpBonus)
                    }
                }

                // Update particles
                for (p in sim.particles) {
                    if (!p.active) continue
                    p.life -= dt
                    if (p.life <= 0f) {
                        p.active = false
                        continue
                    }
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.vx *= 0.94f
                    p.vy *= 0.94f
                }

                frameTick = frameNanos
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.5.dp, cabinet.borderColor, RoundedCornerShape(6.dp))
            .background(Color(0xFF070410))
    ) {
        // 1. Isolated 60fps Playfield Canvas (>85% unobstructed)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stepIndex) {
                    detectTapGestures(
                        onPress = { offset ->
                            sim.targetX = offset.x
                            sim.targetY = offset.y
                        },
                        onDoubleTap = {
                            sim.triggerEmpDash()
                        }
                    )
                }
                .pointerInput(stepIndex) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            sim.targetX = offset.x
                            sim.targetY = offset.y
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            sim.targetX = change.position.x
                            sim.targetY = change.position.y
                        }
                    )
                }
                .testTag("cyber_breach_canvas")
        ) {
            // Read frameTick to subscribe draw scope to 60fps frame updates
            val unusedTick = frameTick
            if (!sim.initialized || sim.width != size.width || sim.height != size.height) {
                sim.width = size.width
                sim.height = size.height
                if (!sim.initialized) {
                    sim.playerX = size.width * 0.5f
                    sim.playerY = size.height * 0.72f
                    sim.initialized = true
                }
            }

            val shakeX = if (sim.shakeMagnitude > 0f) (Random.nextFloat() - 0.5f) * 2f * sim.shakeMagnitude else 0f
            val shakeY = if (sim.shakeMagnitude > 0f) (Random.nextFloat() - 0.5f) * 2f * sim.shakeMagnitude else 0f

            withTransform({
                translate(left = shakeX, top = shakeY)
            }) {
                // Cyber grid background
                val gridStep = 48f
                val gridColor = cabinet.borderColor.copy(alpha = 0.16f)
                var gx = 0f
                while (gx < size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(gx, 0f),
                        end = Offset(gx, size.height),
                        strokeWidth = 1f
                    )
                    gx += gridStep
                }
                var gy = sim.gridScroll
                while (gy < size.height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, gy),
                        end = Offset(size.width, gy),
                        strokeWidth = 1f
                    )
                    gy += gridStep
                }

                // Draw Data Shards (collectible golden diamonds)
                for (s in sim.shards) {
                    if (!s.active) continue
                    val r = 8f
                    val shardPath = Path().apply {
                        moveTo(s.x, s.y - r)
                        lineTo(s.x + r, s.y)
                        lineTo(s.x, s.y + r)
                        lineTo(s.x - r, s.y)
                        close()
                    }
                    drawPath(shardPath, color = Color(0xFFFFD23F))
                    drawPath(shardPath, color = Color.White.copy(alpha = 0.7f), style = Stroke(width = 1.5f))
                }

                // Draw Enemies + Sniper Telegraph Lasers
                for (e in sim.enemies) {
                    if (!e.active) continue

                    // Sniper visible windup telegraph line
                    if (e.kind == EnemyKind.SNIPER && e.telegraphTimer in 0.05f..0.85f) {
                        val flashAlpha = if ((e.telegraphTimer * 20).toInt() % 2 == 0) 0.85f else 0.35f
                        val endX = e.x + cos(e.aimAngle) * 900f
                        val endY = e.y + sin(e.aimAngle) * 900f
                        drawLine(
                            color = Color(0xFFFF3EA0).copy(alpha = flashAlpha),
                            start = Offset(e.x, e.y),
                            end = Offset(endX, endY),
                            strokeWidth = if (e.telegraphTimer < 0.3f) 3.5f else 1.5f
                        )
                    }

                    val baseColor = when {
                        e.hitFlashTimer > 0f -> Color.White
                        e.kind == EnemyKind.SEEKER -> Color(0xFFFF3EA0)
                        e.kind == EnemyKind.SNIPER -> Color(0xFFFF9347)
                        EnemyKind.GUARDIAN == e.kind -> Color(0xFFFFD23F)
                        else -> cabinet.accentColor
                    }

                    val radius = if (e.kind == EnemyKind.GUARDIAN) 22f else 15f
                    drawCircle(
                        color = baseColor.copy(alpha = 0.25f),
                        radius = radius * 1.45f,
                        center = Offset(e.x, e.y)
                    )
                    drawRect(
                        color = baseColor,
                        topLeft = Offset(e.x - radius * 0.75f, e.y - radius * 0.75f),
                        size = Size(radius * 1.5f, radius * 1.5f),
                        style = Stroke(width = 3f)
                    )
                    drawCircle(
                        color = baseColor,
                        radius = radius * 0.4f,
                        center = Offset(e.x, e.y)
                    )
                }

                // Draw Bullets / Packets
                for (b in sim.bullets) {
                    if (!b.active) continue
                    val bColor = if (b.isPlayer) Color(0xFF5FFF8A) else Color(0xFFFF4D6D)
                    drawCircle(
                        color = bColor,
                        radius = if (b.isPlayer) 5f else 6f,
                        center = Offset(b.x, b.y)
                    )
                }

                // Draw Pooled Particles
                for (p in sim.particles) {
                    if (!p.active) continue
                    val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(p.x - p.size * 0.5f, p.y - p.size * 0.5f),
                        size = Size(p.size, p.size)
                    )
                }

                // Draw Player Cyber-Drone with Squash-and-Stretch
                val droneAlpha = if (sim.invulnTimer > 0f && (sim.invulnTimer * 20).toInt() % 2 == 0) 0.45f else 1f
                withTransform({
                    scale(
                        scaleX = sim.scaleX,
                        scaleY = sim.scaleY,
                        pivot = Offset(sim.playerX, sim.playerY)
                    )
                }) {
                    // Outer neon aura
                    drawCircle(
                        color = cabinet.primaryColor.copy(alpha = 0.28f * droneAlpha),
                        radius = 24f,
                        center = Offset(sim.playerX, sim.playerY)
                    )
                    // Cyber-Drone delta chevron
                    val dronePath = Path().apply {
                        moveTo(sim.playerX, sim.playerY - 18f)
                        lineTo(sim.playerX + 15f, sim.playerY + 14f)
                        lineTo(sim.playerX, sim.playerY + 7f)
                        lineTo(sim.playerX - 15f, sim.playerY + 14f)
                        close()
                    }
                    drawPath(
                        path = dronePath,
                        color = Color(0xFF4EE2FF).copy(alpha = droneAlpha)
                    )
                    drawPath(
                        path = dronePath,
                        color = Color.White.copy(alpha = droneAlpha),
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 2. Low-Occlusion Perimeter Status Cluster (Top) — tabular-nums integer readouts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Perimeter Status: SHIELD HP + BREACH %
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PerimeterStatPill(
                    label = "SHIELD",
                    value = String.format("%03d/%03d", shieldHp, maxShieldHp),
                    valueColor = if (shieldHp > 35) Color(0xFF5FFF8A) else Color(0xFFFF4D6D),
                    borderColor = cabinet.borderColor
                )
                PerimeterStatPill(
                    label = "BREACH",
                    value = String.format("%03d%%", breachPercent),
                    valueColor = Color(0xFF4EE2FF),
                    borderColor = cabinet.borderColor
                )
            }

            // Right Perimeter Status: COMBO + SCORE
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PerimeterStatPill(
                    label = "COMBO",
                    value = "x$comboCount",
                    valueColor = Color(0xFFFFD23F),
                    borderColor = cabinet.borderColor
                )
                PerimeterStatPill(
                    label = "SHARDS",
                    value = String.format("%05d", waveScore),
                    valueColor = cabinet.primaryColor,
                    borderColor = cabinet.borderColor
                )
            }
        }

        // 3. Low-Occlusion Bottom Perimeter Action Strip (EMP Dash + Terminal Hack Trigger)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { sim.triggerEmpDash() },
                enabled = empCooldown <= 0f,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xCC1A1030),
                    contentColor = Color(0xFF4EE2FF),
                    disabledContainerColor = Color(0x77120A20),
                    disabledContentColor = Color(0xFF5B4D80)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .border(1.dp, Color(0xFF4EE2FF).copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .testTag("emp_dash_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "EMP Dash",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (empCooldown > 0f) String.format("EMP %.1fs", empCooldown) else "EMP BLAST",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFeatureSettings = "tnum"
                    )
                )
            }

            Text(
                text = "DRAG TO MOVE · AUTO-PULSE ACTIVE",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color(0xAA8A78B3)
            )

            Button(
                onClick = onOpenTerminalModal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = cabinet.primaryColor,
                    contentColor = Color(0xFF1A0420)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("open_terminal_contract_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal Contract",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "HACK NODE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PerimeterStatPill(
    label: String,
    value: String,
    valueColor: Color,
    borderColor: Color
) {
    Column(
        modifier = Modifier
            .background(Color(0xBF0E081C), RoundedCornerShape(3.dp))
            .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = Color(0xFF8A78B3)
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = valueColor,
                fontFeatureSettings = "tnum"
            )
        )
    }
}
