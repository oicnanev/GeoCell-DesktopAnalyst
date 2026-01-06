import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.service.DatabaseService
import java.sql.DriverManager
import java.util.Properties
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object PostgreSQLConnectionTest {
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

            // Listar tabelas (opcional)
            println("\n📊 Looking for tables...")
            val tables = conn.metaData.getTables(null, null, "%", arrayOf("TABLE"))
            var tableCount = 0
            while (tables.next()) {
                val tableName = tables.getString("TABLE_NAME")
                val tableSchema = tables.getString("TABLE_SCHEM")
                println("   - $tableSchema.$tableName")
                tableCount++
            }
            println("   Total: $tableCount tabl(s)")

            // 🔍 QUERY DE TESTE COM OS PARÂMETROS ESPECIFICADOS
            println("\n🔍 Executing test query...")
            println("   CGI: 268-03-1821524179")
            println("   Radius: 1.0 km")
            println("   No filters (operator, date, technology)")

            try {
                // Primeiro, verificar se a tabela 'cell' existe
                val cellTables = conn.metaData.getTables(null, null, "geocell_cell", arrayOf("TABLE"))
                if (cellTables.next()) {
                    println("✅ Table 'cell' found!")

                    // Query de teste - ajuste conforme a estrutura real da sua tabela
                    val testQuery = """
                        SELECT *
                        FROM geocell_cell 
                        WHERE cgi = '268-03-1821524179'
                        LIMIT 10
                    """.trimIndent()

                    println("\n📋 Query to execute:")
                    println(testQuery)

                    val testStmt = conn.createStatement()
                    val testRs = testStmt.executeQuery(testQuery)

                    var resultCount = 0
                    println("\n📊 Query results:")
                    println("=================================")

                    val metaData = testRs.metaData
                    val columnCount = metaData.columnCount

                    // Imprimir cabeçalho das colunas
                    for (i in 1..columnCount) {
                        print("${metaData.getColumnName(i)} | ")
                    }
                    println("\n" + "-".repeat(columnCount * 20))

                    // Imprimir resultados
                    while (testRs.next()) {
                        resultCount++
                        for (i in 1..columnCount) {
                            val value = testRs.getString(i) ?: "NULL"
                            print("$value | ")
                        }
                        println()
                    }

                    if (resultCount == 0) {
                        println("❌ No results found for CGI '268-03-1821524179'")
                        println("\n💡 Trying to find similar CGIs...")

                        // Buscar CGIs similares para debugging
                        val similarQuery = """
                            SELECT DISTINCT cgi, operator, technology 
                            FROM cell 
                            WHERE cgi LIKE '%268-03%' 
                            LIMIT 5
                        """.trimIndent()

                        val similarStmt = conn.createStatement()
                        val similarRs = similarStmt.executeQuery(similarQuery)

                        println("\n🔍 Similar CGIs found:")
                        while (similarRs.next()) {
                            println("   - ${similarRs.getString("cgi")} (${similarRs.getString("operator")} - ${similarRs.getString("technology")})")
                        }
                        similarRs.close()
                        similarStmt.close()
                    } else {
                        println("=================================")
                        println("✅ Found $resultCount record(s)")
                    }

                    testRs.close()
                    testStmt.close()

                    // Se você tiver uma função de busca por raio, pode testar aqui
                    println("\n📍 Testing radius search (if applicable)...")
                    // Esta parte depende da estrutura do seu banco de dados
                    // Você pode precisar de uma função específica para buscar por raio

                } else {
                    println("❌ Table 'cell' not found!")
                    println("\n💡 Available tables that might be relevant:")

                    // Procurar por tabelas que possam conter dados de células
                    val allTables = conn.metaData.getTables(null, null, "%", arrayOf("TABLE"))
                    val relevantTables = mutableListOf<String>()

                    while (allTables.next()) {
                        val tableName = allTables.getString("TABLE_NAME").lowercase()
                        if (tableName.contains("cell") || tableName.contains("tower") ||
                            tableName.contains("site") || tableName.contains("base")) {
                            relevantTables.add(allTables.getString("TABLE_SCHEM") + "." +
                                    allTables.getString("TABLE_NAME"))
                        }
                    }

                    if (relevantTables.isNotEmpty()) {
                        println("   Possible relevant tables:")
                        relevantTables.forEach { println("   - $it") }
                    } else {
                        println("   No obvious cell-related tables found")
                    }
                }
                cellTables.close()

            } catch (e: Exception) {
                println("\n⚠️ Error during test query:")
                println("   ${e.message}")
                println("\n💡 Check if:")
                println("   1. Table 'cell' exists")
                println("   2. Column names are correct")
                println("   3. You have SELECT permissions")
            }

            conn.close()
            println("\n🎉 Test complete! Connection is working.")

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