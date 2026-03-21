param(
    [string]$OutputDir = "Certificates/signature",
    [string]$Alias = "license-signing",
    [string]$StorePassword,
    [string]$KeyPassword,
    [string]$DName = "CN=ZI-Labs License Signature, OU=Security, O=ZI-Labs, L=Moscow, C=RU",
    [int]$ValidityDays = 3650,
    [string]$KeytoolPath = ""
)

if ([string]::IsNullOrWhiteSpace($StorePassword)) {
    throw "StorePassword is required."
}

if ([string]::IsNullOrWhiteSpace($KeyPassword)) {
    $KeyPassword = $StorePassword
}

if ([string]::IsNullOrWhiteSpace($KeytoolPath)) {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin/keytool.exe"))) {
        $KeytoolPath = Join-Path $env:JAVA_HOME "bin/keytool.exe"
    } else {
        $keytoolCommand = Get-Command keytool.exe -ErrorAction SilentlyContinue
        if ($null -ne $keytoolCommand) {
            $KeytoolPath = $keytoolCommand.Source
        } else {
            $commonPaths = @(
                "C:\\Program Files\\Java\\latest\\jdk-25\\bin\\keytool.exe",
                "C:\\Program Files\\Java\\jdk-25.0.2\\bin\\keytool.exe",
                "C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.3.3\\jbr\\bin\\keytool.exe"
            )
            $KeytoolPath = $commonPaths | Where-Object { Test-Path $_ } | Select-Object -First 1
        }
    }
}

if ([string]::IsNullOrWhiteSpace($KeytoolPath) -or -not (Test-Path $KeytoolPath)) {
    throw "keytool.exe not found. Provide -KeytoolPath explicitly."
}

New-Item -Path $OutputDir -ItemType Directory -Force | Out-Null

$keystorePath = Join-Path $OutputDir "signing-keystore.p12"
$certDerPath = Join-Path $OutputDir "signing-public.cer"
$certPemPath = Join-Path $OutputDir "signing-public.pem"
$base64Path = Join-Path $OutputDir "signing-keystore.base64.txt"

if (Test-Path $keystorePath) {
    Remove-Item $keystorePath -Force
}

& $KeytoolPath -genkeypair `
    -alias $Alias `
    -keyalg RSA `
    -keysize 2048 `
    -sigalg SHA256withRSA `
    -storetype PKCS12 `
    -keystore $keystorePath `
    -storepass $StorePassword `
    -keypass $KeyPassword `
    -dname $DName `
    -validity $ValidityDays

& $KeytoolPath -exportcert `
    -alias $Alias `
    -keystore $keystorePath `
    -storepass $StorePassword `
    -file $certDerPath

& $KeytoolPath -exportcert `
    -alias $Alias `
    -keystore $keystorePath `
    -storepass $StorePassword `
    -rfc `
    -file $certPemPath

[Convert]::ToBase64String([IO.File]::ReadAllBytes($keystorePath)) | Set-Content -Path $base64Path -NoNewline

Write-Output "Keystore: $keystorePath"
Write-Output "Public cert (DER): $certDerPath"
Write-Output "Public cert (PEM): $certPemPath"
Write-Output "Base64 for GitHub secret SIGNATURE_KEYSTORE_BASE64: $base64Path"
