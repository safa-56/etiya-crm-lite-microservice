# 3 servisin veritabanini test seed'iyle doldurur (crm-postgres container'i icinden).
$ErrorActionPreference = 'Stop'
$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$container = if ($env:PG_CONTAINER) { $env:PG_CONTAINER } else { 'crm-postgres' }
$user = if ($env:PG_USER) { $env:PG_USER } else { 'postgres' }

function Load-Seed($file, $db) {
    Write-Host ">> $db <- $(Split-Path -Leaf $file)"
    Get-Content -Raw -Encoding UTF8 $file | docker exec -i $container psql -v ON_ERROR_STOP=1 -U $user -d $db
    if ($LASTEXITCODE -ne 0) { throw "Seed basarisiz: $file (db=$db)" }
}

Load-Seed (Join-Path $dir '01_customer_seed.sql') 'customerdb'
Load-Seed (Join-Path $dir '02_account_seed.sql')  'accountdb'
Load-Seed (Join-Path $dir '03_product_seed.sql')  'productdb'

Write-Host 'Seed tamamlandi.'
