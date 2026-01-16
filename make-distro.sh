#!/bin/bash
# Script para criar distribuição

set -e

echo "=== Criando Distribuição KMZ Exporter ==="
echo ""

# Verificar se templates existem
if [ ! -d "distribution-templates" ]; then
    echo "Criando templates..."
    mkdir -p distribution-templates

    # Criar template Linux
    cat > distribution-templates/run-linux.sh.template << 'EOF'
#!/bin/bash
# Script de execução para Linux

echo "=== KMZ Exporter ==="
echo "Versão: {{VERSION}}"
echo ""

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado!"
    echo "Por favor, instale Java 17 ou superior."
    echo "Visite: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo "✓ Java encontrado: versão $JAVA_VERSION"

# Verificar versão do Java
MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f1)
if [ "$MAJOR_VERSION" -lt 17 ]; then
    echo "❌ Java muito antigo! Necessário Java 17+."
    echo "Versão atual: $JAVA_VERSION"
    exit 1
fi

echo "✓ Java 17+ detectado"
echo "Iniciando aplicação..."
echo ""

# Executar aplicação
java \
  --module-path=javafx/lib \
  --add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
  --add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED \
  --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  -Xmx2048m \
  -jar KMZ-Exporter.jar
EOF

    # Criar template Windows
    cat > distribution-templates/run-windows.bat.template << 'EOF'
@echo off
echo === KMZ Exporter ===
echo Versão: {{VERSION}}
echo.

REM Verificar Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Java não encontrado!
    echo Por favor, instale Java 17 ou superior.
    echo Visite: https://adoptium.net/
    pause
    exit /b 1
)

REM Executar aplicação
echo ✓ Java encontrado
echo Iniciando aplicação...
echo.

java ^
  --module-path=javafx\lib ^
  --add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base ^
  --add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED ^
  --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED ^
  --add-opens=java.base/java.lang=ALL-UNNAMED ^
  -Xmx2048m ^
  -jar KMZ-Exporter.jar

echo.
pause
EOF

    # Criar template README
    cat > distribution-templates/README.txt.template << 'EOF'
KMZ Exporter - Distribuição Portável
======================================

Versão: {{VERSION}}

COMO EXECUTAR:
---------------

1. LINUX / macOS:
   - Abra terminal na pasta
   - Execute: ./run-linux.sh
   - Se necessário: chmod +x run-linux.sh

2. WINDOWS:
   - Clique duas vezes em: run-windows.bat
   - OU abra cmd/powershell e execute: run-windows.bat

REQUISITOS:
-----------
- Java 17 ou superior
- Pelo menos 2GB RAM

PARA DISTRIBUIR:
----------------
Compacte toda esta pasta em um arquivo ZIP.
EOF

    echo "✓ Templates criados"
fi

# Executar build
echo "🔨 Executando build..."
./gradlew clean compileKotlin createPortableDistribution

echo ""
echo "🎉 DISTRIBUIÇÃO CRIADA COM SUCESSO!"
echo ""
echo "📦 Arquivo ZIP: build/distributions/KMZ-Exporter-v*-portable.zip"
echo "📁 Pasta: build/distributions/portable/"
echo ""
echo "Para testar:"
echo "  cd build/distributions/portable"
echo "  ./run-linux.sh"