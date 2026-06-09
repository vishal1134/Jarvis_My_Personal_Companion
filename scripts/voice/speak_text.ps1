param(
    [Parameter(Mandatory = $true)]
    [string]$Text,

    [int]$Rate = -1,

    [int]$Volume = 100,

    [switch]$KeepCommas
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Speech
$speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer
$speaker.Rate = $Rate
$speaker.Volume = $Volume

$speechText = $Text
if (-not $KeepCommas) {
    $speechText = $speechText `
        -replace ", sir\b", " sir" `
        -replace ", mam\b", " mam" `
        -replace ", madam\b", " madam"
}

$speaker.Speak($speechText)
