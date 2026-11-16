package org.ucb.appp1.feature.history.data.datasource

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.ucb.appp1.feature.history.domain.model.AnalysisHistory

/** Historial persistente en SQLite (se conserva al cerrar la app). */
class SqliteHistoryDataSource(context: Context) : HistoryDataSource {

    private val helper = object : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE $TABLE (id INTEGER PRIMARY KEY AUTOINCREMENT, source TEXT NOT NULL, " +
                    "labels TEXT NOT NULL, texts TEXT NOT NULL, timestamp INTEGER NOT NULL)"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE")
            onCreate(db)
        }
    }

    private val items = MutableStateFlow<List<AnalysisHistory>?>(null)

    override fun observeAll(): Flow<List<AnalysisHistory>> = kotlinx.coroutines.flow.flow {
        if (items.value == null) refresh()
        items.asStateFlow().collect { list -> if (list != null) emit(list) }
    }

    override suspend fun insert(item: AnalysisHistory) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("source", item.source)
            put("labels", item.labels.joinToString(SEP))
            put("texts", item.texts.joinToString(SEP))
            put("timestamp", item.timestamp)
        }
        helper.writableDatabase.insert(TABLE, null, values)
        refresh()
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete(TABLE, "id = ?", arrayOf(id.toString()))
        refresh()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete(TABLE, null, null)
        refresh()
    }

    private suspend fun refresh() = withContext(Dispatchers.IO) {
        val result = mutableListOf<AnalysisHistory>()
        helper.readableDatabase.query(TABLE, null, null, null, null, null, "timestamp DESC, id DESC").use { c ->
            while (c.moveToNext()) {
                result += AnalysisHistory(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    source = c.getString(c.getColumnIndexOrThrow("source")),
                    labels = split(c.getString(c.getColumnIndexOrThrow("labels"))),
                    texts = split(c.getString(c.getColumnIndexOrThrow("texts"))),
                    timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
                )
            }
        }
        items.value = result
    }

    private fun split(raw: String): List<String> = if (raw.isEmpty()) emptyList() else raw.split(SEP)

    private companion object {
        const val DB_NAME = "vision_history.db"
        const val DB_VERSION = 1
        const val TABLE = "analysis_history"
        const val SEP = "\u001F"
    }
}
