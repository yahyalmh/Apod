package com.yaya.apod.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table apod add column favorite INTEGER default 0")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create temp table
        database.execSQL(
            """
            create table apod_tmp (
                id INTEGER NOT NULL, copyright TEXT, date TEXT NOT NULL, explanation TEXT NOT NULL,
                hdurl TEXT, media_type TEXT NOT NULL, title TEXT NOT NULL, service_version TEXT,
                favorite INTEGER default false, url TEXT NOT NULL, PRIMARY KEY(id)  
            )
            
             """.trimIndent()
        )

        // Copy the data
        database.execSQL(
            """
            INSERT INTO apod_tmp (
            id, copyright, date, hdurl, media_type, title, service_version, explanation, url)
            SELECT id, copyright, date, hdurl, media_type, title, service_version, explanation, url
            FROM apod
             
            """.trimIndent()
        )

        // Remove the old table
        database.execSQL("DROP TABLE apod")

        // Change the table name to the correct one
        database.execSQL("ALTER TABLE apod_tmp RENAME TO apod")
    }
}

// Add other migrations to this list
val ALL_MIGRATION = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

