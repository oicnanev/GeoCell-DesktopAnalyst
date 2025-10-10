package com.geocell.desktopanalyst.service

import com.geocell.desktopanalyst.extensions.toCell // Importa as extensions
import com.geocell.desktopanalyst.extensions.toCellPolygon
import com.geocell.desktopanalyst.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseService(
    url: String,
    user: String,
    password: String
) {
    init {
        Database.connect(url, driver = "org.postgresql.Driver", user = user, password = password)
        transaction {
            exec("CREATE EXTENSION IF NOT EXISTS postgis")
        }
    }

    fun getCellByCgi(cgi: String): Cell? = transaction {
        (CellTable leftJoin LocationTable leftJoin CountyTable
            leftJoin MccMncTable leftJoin BandTable)
            .select { CellTable.cgi eq cgi }
            .singleOrNull()
            ?.toCell()
    }

    fun getCellPolygons(cellId: Long): List<CellPolygon> = transaction {
        CellPolygonTable
            .select { CellPolygonTable.cellId eq cellId }
            .map { it.toCellPolygon() }
    }

    fun getCellsByCgiList(cgis: List<String>): Map<String, Cell> = transaction {
        addLogger(StdOutSqlLogger)
        (CellTable leftJoin LocationTable leftJoin CountyTable
                leftJoin MccMncTable leftJoin BandTable)
            .select { CellTable.cgi inList cgis }
            .associate { row ->
                row[CellTable.cgi] to row.toCell()
            }.toNotNullKeyMap() //as Map<String, Cell>
    }
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K?, V>.toNotNullKeyMap(): Map<K, V> = this as Map<K, V>