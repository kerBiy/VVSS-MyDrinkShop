$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$env:JENKINS_HOME = Join-Path $root '.jenkins'
$env:TMP = Join-Path $root 'tmp'
$env:TEMP = Join-Path $root 'tmp'

New-Item -ItemType Directory -Force -Path $env:JENKINS_HOME, $env:TMP | Out-Null

& java '-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT' -jar (Join-Path $root 'tools\jenkins.war') --httpPort=8081
