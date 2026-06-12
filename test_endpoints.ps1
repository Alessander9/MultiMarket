$endpoints = @(
    @{ e="/auth/register"; m="POST" },
    @{ e="/auth/login"; m="POST" },
    @{ e="/auth/forgot-password"; m="POST" },
    @{ e="/auth/reset-password"; m="POST" },
    @{ e="/auth/change-password"; m="PUT" },
    @{ e="/auth/profile"; m="GET" },
    @{ e="/categorias"; m="GET" },
    @{ e="/categorias"; m="POST" },
    @{ e="/chat/conversaciones"; m="GET" },
    @{ e="/exportar"; m="POST" },
    @{ e="/importar"; m="POST" },
    @{ e="/logs"; m="GET" },
    @{ e="/notificaciones"; m="GET" },
    @{ e="/pagos"; m="POST" },
    @{ e="/pedidos"; m="GET" },
    @{ e="/productos"; m="GET" },
    @{ e="/vendedores"; m="POST" }
)

$results = @()
foreach ($req in $endpoints) {
    $url = "http://localhost:8080" + $req.e
    try {
        if ($req.m -eq "POST" -or $req.m -eq "PUT") {
            $res = Invoke-WebRequest -Uri $url -Method $req.m -Body "{}" -ContentType "application/json" -ErrorAction Stop
        } else {
            $res = Invoke-WebRequest -Uri $url -Method $req.m -ErrorAction Stop
        }
        $status = $res.StatusCode
    } catch {
        if ($_.Exception.Response) {
            $status = $_.Exception.Response.StatusCode.value__
        } else {
            $status = "CONNECTION_ERROR"
        }
    }
    $results += [PSCustomObject]@{ Endpoint = $req.e; Method = $req.m; Status = $status }
}

$results | ConvertTo-Json
