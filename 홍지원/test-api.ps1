$base = 'http://localhost:8080'

$authorHeaders = @{ 'X-User-Id' = '1' }
$otherHeaders = @{ 'X-User-Id' = '2' }

function Invoke-PostApi {
    param(
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        $Body
    )

    $request = @{
        Method = $Method
        Uri = "$base$Path"
        Headers = $Headers
        ErrorAction = 'Stop'
    }

    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 10 -Compress
        $request.ContentType = 'application/json; charset=utf-8'
        $request.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }

    try {
        Invoke-RestMethod @request
    } catch {
        $response = $_.Exception.Response
        if ($null -eq $response) { throw }

        Write-Host ('HTTP ' + [int]$response.StatusCode)

        $errorBody = $_.ErrorDetails.Message

        if ([string]::IsNullOrWhiteSpace($errorBody)) {
            if ($response -is [System.Net.HttpWebResponse]) {
                $reader = New-Object System.IO.StreamReader(
                    $response.GetResponseStream()
                )
                try {
                    $errorBody = $reader.ReadToEnd()
                } finally {
                    $reader.Dispose()
                }
            } else {
                $errorBody = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
            }
        }

        Write-Host $errorBody
    }
}
