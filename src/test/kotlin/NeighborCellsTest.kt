import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.service.DatabaseService
import java.sql.DriverManager
import java.util.Properties
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object NeighborCellsTest {
    @JvmStatic
    fun main(args: Array<String>) {
        testConnectionWithDotEnv()
    }

    fun testConnectionWithDotEnv() {
        println("📖 Reading .env file configurations...")

        // Carregar variáveis do .env
        val dotenv = dotenv {
            directory = "./"  // Search on project root
            ignoreIfMissing = false  // Throw exception if  not found
        }

        // .env values
        val host = dotenv["DB_HOST"]
        val port = dotenv["DB_PORT"]
        val database = dotenv["DB_NAME"]
        val user = dotenv["DB_USER"]
        val password = dotenv["DB_PASSWORD"]

        println("🔧 Configuration loaded:")
        println("   Host: $host")
        println("   Port: $port")
        println("   Database: $database")
        println("   User: $user")

        val url = "jdbc:postgresql://$host:$port/$database"
        println("📡 Connection URL: $url")

        try {
            val connectionProps = Properties()
            connectionProps["user"] = user
            connectionProps["password"] = password

            println("\n🔗 Trying to connect...")
            val conn = DriverManager.getConnection(url, connectionProps)

            // Test connection
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("""
                SELECT 
                    current_database() as db,
                    current_user as user,
                    inet_server_addr() as server_ip,
                    inet_server_port() as server_port,
                    version() as pg_version
            """.trimIndent())

            if (rs.next()) {
                println("\n✅ CONNECT SUCCESSFULLY!")
                println("=================================")
                println("📦 Database: ${rs.getString("db")}")
                println("👤 User: ${rs.getString("user")}")
                println("🌐 Server: ${rs.getString("server_ip")}:${rs.getString("server_port")}")
                println("🐘 PostgreSQL: ${rs.getString("pg_version")}")
                println("=================================")
            }

            // 🔍 VERIFICAR SE POSTGIS ESTÁ INSTALADO
            println("\n" + "=".repeat(60))
            println("🗺️ CHECKING POSTGIS SUPPORT")
            println("=".repeat(60))

            val postgisCheck = """
                SELECT 
                    EXISTS(SELECT 1 FROM pg_extension WHERE extname = 'postgis') as has_postgis,
                    postgis_version() as postgis_version
            """.trimIndent()

            try {
                val pgStmt = conn.createStatement()
                val pgRs = pgStmt.executeQuery(postgisCheck)

                if (pgRs.next()) {
                    val hasPostgis = pgRs.getBoolean("has_postgis")
                    val postgisVersion = pgRs.getString("postgis_version")

                    if (hasPostgis) {
                        println("✅ PostGIS is installed: $postgisVersion")
                    } else {
                        println("❌ PostGIS is NOT installed")
                        println("💡 Spatial queries will not work correctly")
                    }
                }
                pgRs.close()
                pgStmt.close()
            } catch (e: Exception) {
                println("⚠️ Error checking PostGIS: ${e.message}")
            }

            // 🔍 QUERY DE TESTE COM OS PARÂMETROS ESPECIFICADOS
            println("\n" + "=".repeat(60))
            println("🔍 TEST QUERY: CGI = 268-03-1821524179")
            println("=".repeat(60))

            try {
                // Query 1: Buscar o CGI específico na tabela geocell_cell
                println("\n📌 Query 1: Basic cell information")
                println("-".repeat(40))

                val testQuery1 = """
                    SELECT 
                        c.id,
                        c.cgi,
                        c.paragon_cgi,
                        c.technology,
                        c.direction,
                        c.name,
                        c.band_id,
                        c.enb_gnb_id,
                        c.location_id,
                        c.mcc_mnc_id,
                        c.created,
                        c.modified,
                        l.coordinates,
                        l.address,
                        l.postal_designation
                    FROM geocell_cell c
                    LEFT JOIN geocell_location l ON c.location_id = l.id
                    WHERE c.cgi = '268-03-1821524179'
                    ORDER BY c.id
                """.trimIndent()

                println("📋 Query:")
                println(testQuery1)

                val testStmt1 = conn.createStatement()
                val testRs1 = testStmt1.executeQuery(testQuery1)

                var resultCount1 = 0
                val cells = mutableListOf<MutableMap<String, Any?>>()

                println("\n📊 Results:")
                println("ID | CGI | Technology | Name | Coordinates | Address")
                println("-".repeat(100))

                while (testRs1.next()) {
                    resultCount1++
                    val cellId = testRs1.getInt("id")
                    val cgi = testRs1.getString("cgi")
                    val technology = testRs1.getInt("technology")
                    val name = testRs1.getString("name")
                    val locationId = testRs1.getInt("location_id")
                    val coordinates = testRs1.getString("coordinates")
                    val address = testRs1.getString("address")
                    val postalDesignation = testRs1.getString("postal_designation")

                    // Extrair lat/lon da geometria POINT
                    var latitude: Double? = null
                    var longitude: Double? = null

                    if (coordinates != null && coordinates.startsWith("0101000020E6100000")) {
                        // É uma geometria WKB hex
                        println("   $cellId | $cgi | $technology | $name | GEOMETRY (WKB) | ${address ?: "N/A"}")

                        // Query para extrair coordenadas da geometria
                        val coordQuery = """
                            SELECT 
                                ST_X(coordinates::geometry) as longitude,
                                ST_Y(coordinates::geometry) as latitude
                            FROM geocell_location 
                            WHERE id = $locationId
                        """.trimIndent()

                        try {
                            val coordStmt = conn.createStatement()
                            val coordRs = coordStmt.executeQuery(coordQuery)

                            if (coordRs.next()) {
                                longitude = coordRs.getDouble("longitude")
                                latitude = coordRs.getDouble("latitude")
                                println("      Extracted coordinates: $latitude, $longitude")
                            }
                            coordRs.close()
                            coordStmt.close()
                        } catch (e: Exception) {
                            println("      ⚠️ Could not extract coordinates: ${e.message}")
                        }
                    } else {
                        println("   $cellId | $cgi | $technology | $name | ${coordinates ?: "NULL"} | ${address ?: "N/A"}")
                    }

                    // Guardar informações para usar depois
                    val cellInfo = mutableMapOf<String, Any?>(
                        "id" to cellId,
                        "cgi" to cgi,
                        "technology" to technology,
                        "name" to name,
                        "location_id" to locationId,
                        "latitude" to latitude,
                        "longitude" to longitude,
                        "address" to address,
                        "postal_designation" to postalDesignation,
                        "has_coordinates" to (latitude != null && longitude != null)
                    )
                    cells.add(cellInfo)
                }

                if (resultCount1 == 0) {
                    println("❌ No results found for CGI '268-03-1821524179'")

                    // Buscar CGIs similares
                    val similarQuery = """
                        SELECT DISTINCT cgi, technology, name
                        FROM geocell_cell 
                        WHERE cgi LIKE '268-03%'
                        LIMIT 5
                    """.trimIndent()

                    val similarStmt = conn.createStatement()
                    val similarRs = similarStmt.executeQuery(similarQuery)

                    println("\n🔍 Similar CGIs found:")
                    while (similarRs.next()) {
                        println("   - ${similarRs.getString("cgi")} (${similarRs.getString("name")})")
                    }
                    similarRs.close()
                    similarStmt.close()
                } else {
                    println("\n✅ Found $resultCount1 record(s)")

                    // Para cada célula com coordenadas, buscar células próximas
                    cells.filter { it["has_coordinates"] == true }.forEach { cell ->
                        val cellId = cell["id"] as Int
                        val latitude = cell["latitude"] as Double
                        val longitude = cell["longitude"] as Double
                        val cgi = cell["cgi"] as String

                        println("\n" + "=".repeat(60))
                        println("📍 CELL: $cgi (ID: $cellId)")
                        println("   Coordinates: $latitude, $longitude")
                        println("=".repeat(60))

                        // Query 2: Buscar células próximas (1km raio)
                        println("\n🔍 Searching for nearby cells (1km radius)...")

                        val radiusQuery = """
                            SELECT 
                                c.id,
                                c.cgi,
                                c.name,
                                c.technology,
                                l.address,
                                ST_Distance(
                                    l.coordinates::geography,
                                    ST_SetSRID(ST_MakePoint($longitude, $latitude), 4326)::geography
                                ) as distance_meters,
                                ST_X(l.coordinates::geometry) as longitude,
                                ST_Y(l.coordinates::geometry) as latitude
                            FROM geocell_cell c
                            JOIN geocell_location l ON c.location_id = l.id
                            WHERE c.id != $cellId
                            AND l.coordinates IS NOT NULL
                            AND ST_DWithin(
                                l.coordinates::geography,
                                ST_SetSRID(ST_MakePoint($longitude, $latitude), 4326)::geography,
                                1000  -- 1km em metros
                            )
                            ORDER BY distance_meters
                        """.trimIndent()

                        println("\n📋 Nearby cells query:")
                        println(radiusQuery)

                        try {
                            val radiusStmt = conn.createStatement()
                            val radiusRs = radiusStmt.executeQuery(radiusQuery)

                            var nearbyCount = 0
                            println("\n📊 Nearby cells found:")
                            while (radiusRs.next()) {
                                nearbyCount++
                                val nearbyId = radiusRs.getInt("id")
                                val nearbyCgi = radiusRs.getString("cgi")
                                val nearbyName = radiusRs.getString("name")
                                val nearbyTech = radiusRs.getInt("technology")
                                val distance = radiusRs.getDouble("distance_meters")
                                val nearbyAddress = radiusRs.getString("address")

                                println("   • $nearbyCgi (ID: $nearbyId)")
                                println("     Name: $nearbyName")
                                println("     Technology: $nearbyTech")
                                println("     Distance: ${String.format("%.1f", distance)}m")
                                println("     Address: ${nearbyAddress ?: "N/A"}")
                                println()
                            }

                            if (nearbyCount == 0) {
                                println("   No other cells found within 1km radius")
                            } else {
                                println("   Total nearby cells: $nearbyCount")
                            }

                            radiusRs.close()
                            radiusStmt.close()
                        } catch (e: Exception) {
                            println("⚠️ Error in spatial query: ${e.message}")
                            println("💡 Trying alternative query...")

                            // Tentativa alternativa
                            val altQuery = """
                                SELECT 
                                    c.id,
                                    c.cgi,
                                    c.name,
                                    c.technology,
                                    l.address
                                FROM geocell_cell c
                                JOIN geocell_location l ON c.location_id = l.id
                                WHERE c.id != $cellId
                                AND l.coordinates IS NOT NULL
                                LIMIT 5
                            """.trimIndent()

                            val altStmt = conn.createStatement()
                            val altRs = altStmt.executeQuery(altQuery)

                            println("\n📊 Alternative query results (showing any cells):")
                            while (altRs.next()) {
                                println("   - ${altRs.getString("cgi")}: ${altRs.getString("name")}")
                            }
                            altRs.close()
                            altStmt.close()
                        }
                    }

                    // Se alguma célula não tem coordenadas
                    val cellsWithoutCoords = cells.filter { it["has_coordinates"] != true }
                    if (cellsWithoutCoords.isNotEmpty()) {
                        println("\n" + "=".repeat(60))
                        println("⚠️ CELLS WITHOUT COORDINATES")
                        println("=".repeat(60))

                        cellsWithoutCoords.forEach { cell ->
                            println("   • ${cell["cgi"]} (ID: ${cell["id"]}): ${cell["name"]}")
                            println("     Location ID: ${cell["location_id"]}")
                        }
                    }
                }

                testRs1.close()
                testStmt1.close()

                // Query 3: Estatísticas gerais
                println("\n" + "=".repeat(60))
                println("📊 GENERAL STATISTICS")
                println("=".repeat(60))

                val statsQuery = """
                    SELECT 
                        -- Estatísticas gerais
                        COUNT(*) as total_cells,
                        COUNT(DISTINCT cgi) as unique_cgis,
                        COUNT(DISTINCT location_id) as unique_locations,
                        COUNT(DISTINCT technology) as technologies_count,
                        
                        -- Estatísticas de coordenadas
                        COUNT(CASE WHEN l.coordinates IS NOT NULL THEN 1 END) as cells_with_coordinates,
                        COUNT(CASE WHEN l.coordinates IS NULL THEN 1 END) as cells_without_coordinates,
                        
                        -- Datas
                        MIN(c.created) as oldest_record,
                        MAX(c.created) as newest_record,
                        
                        -- Tecnologias
                        STRING_AGG(DISTINCT CAST(c.technology AS TEXT), ', ') as technologies_list
                    FROM geocell_cell c
                    LEFT JOIN geocell_location l ON c.location_id = l.id
                """.trimIndent()

                val statsStmt = conn.createStatement()
                val statsRs = statsStmt.executeQuery(statsQuery)

                if (statsRs.next()) {
                    println("\n📈 Database statistics:")
                    println("   Total cells: ${statsRs.getInt("total_cells")}")
                    println("   Unique CGIs: ${statsRs.getInt("unique_cgis")}")
                    println("   Unique locations: ${statsRs.getInt("unique_locations")}")
                    println("   Technologies count: ${statsRs.getInt("technologies_count")}")
                    println("   Technologies: ${statsRs.getString("technologies_list")}")
                    println("   Cells with coordinates: ${statsRs.getInt("cells_with_coordinates")}")
                    println("   Cells without coordinates: ${statsRs.getInt("cells_without_coordinates")}")
                    println("   Coverage: ${String.format("%.1f", statsRs.getInt("cells_with_coordinates").toDouble() / statsRs.getInt("total_cells").toDouble() * 100)}%")
                    println("   Oldest record: ${statsRs.getDate("oldest_record")}")
                    println("   Newest record: ${statsRs.getDate("newest_record")}")
                }

                statsRs.close()
                statsStmt.close()

                // Query 4: Distribuição por tecnologia
                println("\n" + "-".repeat(40))
                println("📡 TECHNOLOGY DISTRIBUTION")
                println("-".repeat(40))

                val techQuery = """
                    SELECT 
                        technology,
                        COUNT(*) as count,
                        ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 1) as percentage
                    FROM geocell_cell
                    GROUP BY technology
                    ORDER BY count DESC
                """.trimIndent()

                val techStmt = conn.createStatement()
                val techRs = techStmt.executeQuery(techQuery)

                println("\nTechnology | Count | Percentage")
                println("-".repeat(30))
                while (techRs.next()) {
                    val tech = techRs.getInt("technology")
                    val count = techRs.getInt("count")
                    val percentage = techRs.getDouble("percentage")
                    println("   $tech       | $count   | ${percentage}%")
                }
                techRs.close()
                techStmt.close()

            } catch (e: Exception) {
                println("\n⚠️ Error during test query:")
                println("   ${e.message}")
                e.printStackTrace()
            }

            conn.close()
            println("\n" + "=".repeat(60))
            println("🎉 TEST COMPLETE!")
            println("=".repeat(60))

        } catch (e: Exception) {
            println("\n❌ CONNECTION ERROR:")
            println("   Message: ${e.message}")
            println("\n🔍 Troubleshooting:")
            println("   1. Check if PostgreSQL is running on $host:$port")
            println("   2. Check credentials in file .env are correct")
            println("   3. Check if user '$user' has access to database '$database'")
            println("   4. Check if the firewall allows connections on port $port")
            e.printStackTrace()
        }
    }
}