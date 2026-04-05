# ============================================================
# build-installer.ps1  —  Genera instalador Windows de POS SellControl
# Requisitos: JDK 23+ con jpackage, Maven
# Uso: .\build-installer.ps1
# ============================================================

$ErrorActionPreference = "Stop"

# --- Rutas configurables ---
$JDK_HOME    = "C:\Program Files\Java\jdk-23"
$MAVEN_HOME  = "C:\Users\Mauricio\tools\apache-maven-3.9.6"
$PROJECT_DIR = $PSScriptRoot  # directorio donde está este script
$APP_NAME    = "POS SellControl"
$APP_VERSION = "1.0.0"
$MAIN_JAR    = "pos-sellcontrol-$APP_VERSION.jar"
$ICON_PATH   = "$PROJECT_DIR\src\main\resources\com\sellcontrol\icon.png"
$OUTPUT_DIR  = "$PROJECT_DIR\target\installer"

# --- Herramientas ---
$MVN      = "$MAVEN_HOME\bin\mvn.cmd"
$JPACKAGE = "$JDK_HOME\bin\jpackage.exe"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Generando instalador: $APP_NAME $APP_VERSION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# --- Paso 1: Compilar y empaquetar ---
Write-Host "[1/5] Compilando proyecto con Maven..." -ForegroundColor Yellow
Set-Location $PROJECT_DIR
& $MVN clean package -q -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo la compilacion Maven." -ForegroundColor Red
    exit 1
}
Write-Host "      Compilacion exitosa." -ForegroundColor Green

# --- Paso 2: Preparar input para jpackage ---
Write-Host "[2/5] Preparando archivos..." -ForegroundColor Yellow
$TARGET_DIR    = "$PROJECT_DIR\target"
$JAR_PATH      = "$TARGET_DIR\$MAIN_JAR"
$LIBS_DIR      = "$TARGET_DIR\libs"
$JPACKAGE_INPUT = "$TARGET_DIR\jpackage-input"

if (-not (Test-Path $JAR_PATH)) {
    Write-Host "ERROR: No se encontro $JAR_PATH" -ForegroundColor Red
    exit 1
}

# Crear directorio unificado con todas las dependencias
if (Test-Path $JPACKAGE_INPUT) { Remove-Item -Recurse -Force $JPACKAGE_INPUT }
New-Item -ItemType Directory -Path $JPACKAGE_INPUT -Force | Out-Null
Copy-Item "$JAR_PATH" "$JPACKAGE_INPUT\"
Copy-Item "$LIBS_DIR\*" "$JPACKAGE_INPUT\"

$libCount = (Get-ChildItem $JPACKAGE_INPUT -Filter "*.jar").Count
Write-Host "      JARs preparados: $libCount" -ForegroundColor Green

# --- Paso 3: Limpiar output anterior ---
Write-Host "[3/5] Preparando directorio de salida..." -ForegroundColor Yellow
if (Test-Path $OUTPUT_DIR) {
    Remove-Item -Recurse -Force $OUTPUT_DIR
}
New-Item -ItemType Directory -Path $OUTPUT_DIR -Force | Out-Null

# --- Paso 4: Generar app-image con jpackage ---
Write-Host "[4/5] Generando app-image con jpackage..." -ForegroundColor Yellow
Write-Host "      Esto puede tardar 1-3 minutos..." -ForegroundColor DarkGray

# Usar classpath mode (--input + --main-jar) con --add-modules para
# cargar JavaFX correctamente. Sin estas opciones, el JRE embebido
# no incluye los modulos de JavaFX y la app no arranca.
$jpackageArgs = @(
    "--type", "app-image"
    "--name", $APP_NAME
    "--app-version", $APP_VERSION
    "--input", $JPACKAGE_INPUT
    "--main-jar", $MAIN_JAR
    "--main-class", "com.sellcontrol.App"
    "--dest", $OUTPUT_DIR
    "--java-options", "-Dapp.dir=`"`$APPDIR\..`""
    "--java-options", "-Dfile.encoding=UTF-8"
    "--java-options", "-p `"`$APPDIR`""
    "--java-options", "--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql,java.desktop"
    "--vendor", "SellControl"
    "--description", "Sistema de Punto de Venta para verduleria"
    "--copyright", "Copyright 2026 SellControl"
)

& $JPACKAGE @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: jpackage fallo." -ForegroundColor Red
    exit 1
}
Write-Host "      App-image generada exitosamente." -ForegroundColor Green

# --- Paso 5: Copiar datos iniciales ---
Write-Host "[5/5] Creando estructura de datos..." -ForegroundColor Yellow
$APP_DIR = "$OUTPUT_DIR\$APP_NAME"

# Crear estructura de carpetas de datos
$dataDirs = @("data", "config", "reportes", "respaldos", "logs")
foreach ($dir in $dataDirs) {
    New-Item -ItemType Directory -Path "$APP_DIR\$dir" -Force | Out-Null
}

# Copiar ticket.properties existente a config/
$ticketSrc = "$PROJECT_DIR\config\ticket.properties"
if (-not (Test-Path $ticketSrc)) { $ticketSrc = "$PROJECT_DIR\ticket.properties" }
if (Test-Path $ticketSrc) {
    Copy-Item $ticketSrc "$APP_DIR\config\ticket.properties"
    Write-Host "      ticket.properties copiado a config/" -ForegroundColor Green
}

# Copiar base de datos existente (si hay una con datos reales)
$dbSrc = "$PROJECT_DIR\data\sellcontrol.db"
if (-not (Test-Path $dbSrc)) { $dbSrc = "$PROJECT_DIR\sellcontrol.db" }
if (Test-Path $dbSrc) {
    Copy-Item $dbSrc "$APP_DIR\data\sellcontrol.db"
    Write-Host "      sellcontrol.db copiada a data/" -ForegroundColor Green
}

# --- Calcular tamaño ---
$totalSize = [math]::Round(((Get-ChildItem "$APP_DIR" -Recurse -File | Measure-Object -Property Length -Sum).Sum / 1MB), 1)

# --- Resumen ---
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " INSTALADOR GENERADO EXITOSAMENTE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Ubicacion: $APP_DIR" -ForegroundColor White
Write-Host "Tamano total: $totalSize MB" -ForegroundColor White
Write-Host ""
Write-Host "Para distribuir: copie toda la carpeta '$APP_NAME' a la PC destino." -ForegroundColor Cyan
Write-Host "El usuario solo necesita ejecutar '$APP_NAME.exe'." -ForegroundColor Cyan
Write-Host ""
