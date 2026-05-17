package com.pramod.chessmasteroffline.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pramod.chessmasteroffline.engine.ChessEngine
import com.pramod.chessmasteroffline.engine.Fen
import com.pramod.chessmasteroffline.engine.GameState
import com.pramod.chessmasteroffline.engine.Move
import com.pramod.chessmasteroffline.engine.MoveRecord
import com.pramod.chessmasteroffline.engine.Piece
import com.pramod.chessmasteroffline.engine.PieceColor
import com.pramod.chessmasteroffline.engine.PieceType
import com.pramod.chessmasteroffline.engine.Square
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.savedGameStore by preferencesDataStore(name = "saved_game")

data class SavedGameSnapshot(
    val state: GameState,
    val mode: GameMode,
    val aiDifficulty: AiDifficulty,
)

class SavedGameRepository(
    private val context: Context,
) {
    private object Keys {
        val hasSavedGame = booleanPreferencesKey("has_saved_game")
        val fen = stringPreferencesKey("fen")
        val history = stringPreferencesKey("history")
        val mode = stringPreferencesKey("mode")
        val aiDifficulty = stringPreferencesKey("ai_difficulty")
    }

    val hasSavedGame: Flow<Boolean> = context.savedGameStore.data.map { it[Keys.hasSavedGame] ?: false }

    suspend fun save(state: GameState, mode: GameMode, aiDifficulty: AiDifficulty) {
        context.savedGameStore.edit { preferences ->
            preferences[Keys.hasSavedGame] = true
            preferences[Keys.fen] = Fen.toFen(state)
            preferences[Keys.history] = state.history.joinToString("\n") { it.toStorageLine() }
            preferences[Keys.mode] = mode.name
            preferences[Keys.aiDifficulty] = aiDifficulty.name
        }
    }

    suspend fun load(): SavedGameSnapshot? {
        val preferences = context.savedGameStore.data.first()
        if (preferences[Keys.hasSavedGame] != true) return null
        val fen = preferences[Keys.fen] ?: return null
        val history = preferences[Keys.history]
            ?.lineSequence()
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { moveRecordFromStorageLine(it) }
            ?.toList()
            .orEmpty()
        val mode = preferences[Keys.mode]?.toEnumOrDefault(GameMode.LOCAL_PLAYER) ?: GameMode.LOCAL_PLAYER
        val difficulty = preferences[Keys.aiDifficulty]?.toEnumOrDefault(AiDifficulty.MEDIUM) ?: AiDifficulty.MEDIUM
        return SavedGameSnapshot(
            state = ChessEngine.stateFromFen(fen, history),
            mode = mode,
            aiDifficulty = difficulty,
        )
    }

    suspend fun clear() {
        context.savedGameStore.edit { it.clear() }
    }
}

private fun MoveRecord.toStorageLine(): String {
    return listOf(
        color.name,
        piece.type.name,
        piece.color.name,
        move.from.algebraic,
        move.to.algebraic,
        move.promotion?.name.orEmpty(),
        captured?.type?.name.orEmpty(),
        captured?.color?.name.orEmpty(),
        notation,
        fenBefore,
        fenAfter,
    ).joinToString("|") { it.urlEncode() }
}

private fun moveRecordFromStorageLine(line: String): MoveRecord? {
    val parts = line.split("|").map { it.urlDecode() }
    if (parts.size != 11) return null
    val color = parts[0].toEnumOrNull<PieceColor>() ?: return null
    val pieceType = parts[1].toEnumOrNull<PieceType>() ?: return null
    val pieceColor = parts[2].toEnumOrNull<PieceColor>() ?: return null
    val from = Square.fromAlgebraic(parts[3]) ?: return null
    val to = Square.fromAlgebraic(parts[4]) ?: return null
    val promotion = parts[5].takeIf { it.isNotBlank() }?.toEnumOrNull<PieceType>()
    val capturedType = parts[6].takeIf { it.isNotBlank() }?.toEnumOrNull<PieceType>()
    val capturedColor = parts[7].takeIf { it.isNotBlank() }?.toEnumOrNull<PieceColor>()
    val captured = if (capturedType != null && capturedColor != null) Piece(capturedType, capturedColor) else null
    return MoveRecord(
        color = color,
        move = Move(from, to, promotion),
        piece = Piece(pieceType, pieceColor),
        captured = captured,
        notation = parts[8],
        fenBefore = parts[9],
        fenAfter = parts[10],
    )
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private fun String.urlDecode(): String = URLDecoder.decode(this, Charsets.UTF_8.name())

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
    return enumValues<T>().firstOrNull { it.name == this }
}

private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T {
    return enumValues<T>().firstOrNull { it.name == this } ?: default
}
