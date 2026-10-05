$ErrorActionPreference = 'Stop'

$env:JAVA_HOME = "C:\Users\saitama\projects\softwares\jdk-21"
$env:Path = "C:\Users\saitama\projects\softwares\jdk-21\bin;C:\Users\saitama\projects\softwares\pgsql\bin;" + $env:Path

Set-Location "C:\Users\saitama\projects\lekhai\LekhaiBackend"

Write-Host "Checking PostgreSQL service..."
$pgReady = & "C:\Users\saitama\projects\softwares\pgsql\bin\pg_isready.exe" -h 127.0.0.1 -p 5432
if ($LASTEXITCODE -ne 0) {
    Write-Host "Starting PostgreSQL database..."
    cmd.exe /c "C:\Users\saitama\projects\softwares\pgsql\bin\pg_ctl.exe -D C:\Users\saitama\projects\softwares\pgsql\data -l C:\Users\saitama\projects\softwares\pgsql\server.log start"
    Start-Sleep -Seconds 3
}

Write-Host "Running Flyway migrations..."
cmd.exe /c "gradlew.bat flywayMigrate -Duser.timezone=UTC"

Write-Host "Starting Spring Boot application on port 8080..."
cmd.exe /c "gradlew.bat bootRun"
