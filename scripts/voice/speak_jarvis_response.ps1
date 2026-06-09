param(
    [Parameter(Mandatory = $true)]
    [string]$Text,

    [string]$ServerUrl = "http://127.0.0.1:8000",

    [int]$Rate = -1,

    [int]$Volume = 100,

    [switch]$KeepCommas
)

$ErrorActionPreference = "Stop"

$payload = @{
    text = $Text
} | ConvertTo-Json

$response = Invoke-RestMethod `
    -Uri "$ServerUrl/commands/handle" `
    -Method Post `
    -ContentType "application/json" `
    -Body $payload

$spokenResponse = [string]$response.spoken_response
$speechText = $spokenResponse

if (-not $KeepCommas) {
    $speechText = $speechText `
        -replace ", sir\b", " sir" `
        -replace ", mam\b", " mam" `
        -replace ", madam\b", " madam"
}

Add-Type -AssemblyName System.Speech
$speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer
$speaker.Rate = $Rate
$speaker.Volume = $Volume
$speaker.Speak($speechText)

$spokenResponse
