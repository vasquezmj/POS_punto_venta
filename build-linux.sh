#!/bin/bash
# ============================================================
# build-linux.sh  —  Genera instalador RPM de POS SellControl para Fedora
# Requisitos: JDK 21+ con jpackage, Maven, rpm-build
# Uso: chmod +x build-linux.sh && ./build-linux.sh
# ============================================================

set -e

# --- Configuración ---
APP_NAME="POS-SellControl"
APP_VERSION="1.0.0"
MAIN_JAR="pos-sellcontrol-${APP_VERSION}.jar"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT_DIR="${PROJECT_DIR}/target/installer"

echo ""
echo "========================================"
echo " Generando instalador RPM: ${APP_NAME} ${APP_VERSION}"
echo "========================================"
echo ""

# --- Verificar herramientas ---
echo "[0/6] Verificando herramientas..."

if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven no está instalado."
    echo "  Instalar: sudo dnf install maven"
    exit 1
fi

if ! command -v jpackage &> /dev/null; then
    echo "ERROR: jpackage no encontrado."
    echo "  Instalar: sudo dnf install java-21-openjdk-devel"
    exit 1
fi

if ! command -v rpmbuild &> /dev/null; then
    echo "AVISO: rpmbuild no encontrado. Instalando..."
    sudo dnf install -y rpm-build
fi

echo "      Maven: $(mvn --version | head -1)"
echo "      jpackage: $(jpackage --version)"

# --- Verificar dependencias del sistema para JavaFX ---
echo "[1/6] Verificando dependencias del sistema..."

DEPS_NEEDED=""

# GTK3 — requerido por JavaFX para renderizar la UI
if ! rpm -q gtk3 &> /dev/null; then
    DEPS_NEEDED="${DEPS_NEEDED} gtk3"
fi

# Mesa OpenGL — requerido por JavaFX para gráficos
if ! rpm -q mesa-libGL &> /dev/null; then
    DEPS_NEEDED="${DEPS_NEEDED} mesa-libGL"
fi

# CUPS — requerido para impresión (impresora térmica)
if ! rpm -q cups &> /dev/null; then
    DEPS_NEEDED="${DEPS_NEEDED} cups"
fi
if ! rpm -q cups-libs &> /dev/null; then
    DEPS_NEEDED="${DEPS_NEEDED} cups-libs"
fi

# Fuentes — requeridas por JavaFX para renderizar texto
if ! rpm -q fontconfig &> /dev/null; then
    DEPS_NEEDED="${DEPS_NEEDED} fontconfig"
fi
if ! rpm -q freetype &> /dev/null; then
    DEPS_NEEDED="${DEPS_NEEDED} freetype"
fi

# libasound — requerido por JavaFX media (puede ser necesario)
if ! rpm -q alsa-lib &> /dev/null; then
    DEPS_NEEDED="${DEPS_NEEDED} alsa-lib"
fi

if [ -n "${DEPS_NEEDED}" ]; then
    echo "      Instalando dependencias del sistema:${DEPS_NEEDED}"
    sudo dnf install -y ${DEPS_NEEDED}
else
    echo "      Todas las dependencias del sistema están instaladas."
fi

# --- Paso 2: Compilar ---
echo "[2/6] Compilando proyecto con Maven..."
cd "${PROJECT_DIR}"
mvn clean package -q -DskipTests
echo "      Compilación exitosa."

# --- Paso 3: Preparar input ---
echo "[3/6] Preparando archivos..."
TARGET_DIR="${PROJECT_DIR}/target"
JAR_PATH="${TARGET_DIR}/${MAIN_JAR}"
LIBS_DIR="${TARGET_DIR}/libs"
JPACKAGE_INPUT="${TARGET_DIR}/jpackage-input"

if [ ! -f "${JAR_PATH}" ]; then
    echo "ERROR: No se encontró ${JAR_PATH}"
    exit 1
fi

# Crear directorio unificado
rm -rf "${JPACKAGE_INPUT}"
mkdir -p "${JPACKAGE_INPUT}"
cp "${JAR_PATH}" "${JPACKAGE_INPUT}/"
cp "${LIBS_DIR}"/*.jar "${JPACKAGE_INPUT}/"

LIB_COUNT=$(ls -1 "${JPACKAGE_INPUT}"/*.jar 2>/dev/null | wc -l)
echo "      JARs preparados: ${LIB_COUNT}"

# Verificar que hay JARs de JavaFX para Linux
if ls "${JPACKAGE_INPUT}"/javafx-*-linux.jar 1>/dev/null 2>&1; then
    echo "      JavaFX Linux JARs: OK"
else
    echo "ERROR: No se encontraron los JARs de JavaFX para Linux."
    echo "       Verifica que Maven descargó javafx-*-linux.jar"
    exit 1
fi

# --- Paso 4: Limpiar ---
echo "[4/6] Preparando directorio de salida..."
rm -rf "${OUTPUT_DIR}"
mkdir -p "${OUTPUT_DIR}"

# --- Paso 5: Generar RPM ---
echo "[5/6] Generando RPM con jpackage..."
echo "      Esto puede tardar 2-5 minutos..."

ICON_PATH="${PROJECT_DIR}/src/main/resources/com/sellcontrol/icon.png"
ICON_ARGS=""
if [ -f "${ICON_PATH}" ]; then
    ICON_ARGS="--icon ${ICON_PATH}"
fi

# Usar classpath mode con --add-modules para que JavaFX se cargue correctamente.
# La clave es:
#   -p $APPDIR     → agrega el directorio de JARs al module-path del JVM
#   --add-modules  → carga explícitamente los módulos de JavaFX
jpackage \
    --type rpm \
    --name "${APP_NAME}" \
    --app-version "${APP_VERSION}" \
    --input "${JPACKAGE_INPUT}" \
    --main-jar "${MAIN_JAR}" \
    --main-class "com.sellcontrol.App" \
    --dest "${OUTPUT_DIR}" \
    --java-options "-Dapp.dir=\$APPDIR/.." \
    --java-options "-Dfile.encoding=UTF-8" \
    --java-options "-p \$APPDIR" \
    --java-options "--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql,java.desktop" \
    --vendor "SellControl" \
    --description "Sistema de Punto de Venta para verdulería" \
    --copyright "Copyright 2026 SellControl" \
    --linux-shortcut \
    --linux-menu-group "Office" \
    --linux-rpm-license-type "Proprietary" \
    --linux-package-deps "gtk3" \
    --linux-package-deps "cups" \
    --linux-package-deps "mesa-libGL" \
    --linux-package-deps "fontconfig" \
    ${ICON_ARGS}

echo "      RPM generado exitosamente."

# --- Paso 6: Mostrar resultado ---
echo "[6/6] Resultado:"
RPM_FILE=$(ls -1 "${OUTPUT_DIR}"/*.rpm 2>/dev/null | head -1)

if [ -n "${RPM_FILE}" ]; then
    echo ""
    echo "========================================"
    echo " INSTALADOR RPM GENERADO EXITOSAMENTE"
    echo "========================================"
    echo ""
    echo "Archivo: ${RPM_FILE}"
    echo "Tamaño:  $(du -h "${RPM_FILE}" | cut -f1)"
    echo ""
    echo "=== INSTRUCCIONES DE INSTALACIÓN ==="
    echo ""
    echo "1. Instalar el RPM:"
    echo "   sudo dnf install ${RPM_FILE}"
    echo ""
    echo "2. Configurar impresora térmica (si aplica):"
    echo "   sudo systemctl enable --now cups"
    echo "   # Abrir http://localhost:631 para agregar la impresora"
    echo "   # O usar: sudo lpadmin -p HOP-H58 -E -v usb://... -P /ruta/ppd"
    echo ""
    echo "3. Ejecutar la aplicación:"
    echo "   Desde el menú de aplicaciones, buscar 'POS-SellControl'"
    echo "   O desde terminal: /opt/${APP_NAME}/bin/${APP_NAME}"
    echo ""
    echo "4. Para desinstalar:"
    echo "   sudo dnf remove ${APP_NAME}"
    echo ""
    echo "Dependencias del sistema incluidas en el RPM:"
    echo "  - gtk3 (interfaz gráfica JavaFX)"
    echo "  - cups (impresión - impresora térmica)"
    echo "  - mesa-libGL (gráficos OpenGL)"
    echo "  - fontconfig (renderizado de fuentes)"
    echo ""
else
    echo "ERROR: No se encontró archivo RPM en ${OUTPUT_DIR}"
    exit 1
fi
