package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object CyberSynthEngine {

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private const val SAMPLE_RATE = 22050

    @Volatile
    var isMuted: Boolean = false

    fun playLaser() {
        if (isMuted) return
        audioScope.launch {
            playToneSweep(startFreq = 880.0, endFreq = 220.0, durationMs = 75, volume = 0.25f)
        }
    }

    fun playHit() {
        if (isMuted) return
        audioScope.launch {
            playNoiseBurst(durationMs = 95, baseFreq = 140.0, volume = 0.35f)
        }
    }

    fun playEmpDash() {
        if (isMuted) return
        audioScope.launch {
            playToneSweep(startFreq = 180.0, endFreq = 920.0, durationMs = 140, volume = 0.28f)
        }
    }

    fun playShardPickup() {
        if (isMuted) return
        audioScope.launch {
            playArpeggio(freqs = doubleArrayOf(587.33, 880.0, 1174.66), noteMs = 45, volume = 0.25f)
        }
    }

    fun playAccessGranted() {
        if (isMuted) return
        audioScope.launch {
            playArpeggio(freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50), noteMs = 70, volume = 0.32f)
        }
    }

    fun playAccessDenied() {
        if (isMuted) return
        audioScope.launch {
            playArpeggio(freqs = doubleArrayOf(196.0, 146.83), noteMs = 130, volume = 0.32f)
        }
    }

    private fun playToneSweep(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float) {
        try {
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            if (numSamples <= 0) return
            val pcm = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = startFreq + (endFreq - startFreq) * progress
                val env = (1.0 - progress).coerceIn(0.0, 1.0)
                phase += (2.0 * PI * freq) / SAMPLE_RATE
                // Square-ish synth harmonic
                val raw = if (sin(phase) >= 0.0) 0.8 else -0.8
                pcm[i] = (raw * env * volume * Short.MAX_VALUE).toInt().toShort()
            }
            writeAudioTrack(pcm)
        } catch (_: Exception) {
        }
    }

    private fun playNoiseBurst(durationMs: Int, baseFreq: Double, volume: Float) {
        try {
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            if (numSamples <= 0) return
            val pcm = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val env = (1.0 - progress) * (1.0 - progress)
                phase += (2.0 * PI * baseFreq) / SAMPLE_RATE
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.6
                val sub = sin(phase) * 0.4
                pcm[i] = ((noise + sub) * env * volume * Short.MAX_VALUE).toInt().toShort()
            }
            writeAudioTrack(pcm)
        } catch (_: Exception) {
        }
    }

    private fun playArpeggio(freqs: DoubleArray, noteMs: Int, volume: Float) {
        try {
            val samplesPerNote = (SAMPLE_RATE * noteMs) / 1000
            val totalSamples = samplesPerNote * freqs.size
            if (totalSamples <= 0) return
            val pcm = ShortArray(totalSamples)
            var phase = 0.0
            for (n in freqs.indices) {
                val freq = freqs[n]
                val offset = n * samplesPerNote
                for (i in 0 until samplesPerNote) {
                    val progress = i.toDouble() / samplesPerNote
                    val env = (1.0 - progress * 0.65).coerceIn(0.0, 1.0)
                    phase += (2.0 * PI * freq) / SAMPLE_RATE
                    val sample = (sin(phase) * 0.7 + (if (sin(phase * 2) >= 0) 0.3 else -0.3))
                    pcm[offset + i] = (sample * env * volume * Short.MAX_VALUE).toInt().toShort()
                }
            }
            writeAudioTrack(pcm)
        } catch (_: Exception) {
        }
    }

    private fun writeAudioTrack(pcm: ShortArray) {
        val byteSize = pcm.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteSize.coerceAtLeast(1024))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        track.play()
        Thread.sleep(((pcm.size * 1000L) / SAMPLE_RATE) + 30L)
        track.release()
    }
}
