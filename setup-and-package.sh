#!/bin/bash

set -e


# Carrega SDKMAN! para uso no script
source "$HOME/.sdkman/bin/sdkman-init.sh"

echo "🔧 Instalando Zulu JDK 17 completo via SDKMAN..."

# Instala Zulu JDK 17 (completo)
sdk install java 17.0.8-zulu

echo "✅ Zulu JDK instalado."

# Usa Zulu JDK 17 como padrão
sdk use java 17.0.8-zulu

# Define JAVA_HOME
export JAVA_HOME="$SDKMAN_CANDIDATES_DIR/java/17.0.8-zulu"
export PATH="$JAVA_HOME/bin:$PATH"

echo "🔍 Verificando módulos obrigatórios..."

required_modules=("java.sql" "java.naming" "java.scripting")
missing_modules=()

for module in "${required_modules[@]}"; do
  if ! "$JAVA_HOME/bin/java" --list-modules | grep -q "$module"; then
    missing_modules+=("$module")
  fi
done

if [ ${#missing_modules[@]} -gt 0 ]; then
  echo "❌ Faltam os seguintes módulos no JDK:"
  printf ' - %s\n' "${missing_modules[@]}"
  echo "Instala um JDK completo que inclua todos os módulos padrão."
  exit 1
fi

echo "✅ Todos os módulos estão presentes."

echo "🚀 Compilando projeto..."
./gradlew clean build

echo "📦 Gerando instalador com jpackage..."
./gradlew jpackage

echo "🎉 Instalador gerado com sucesso!"
