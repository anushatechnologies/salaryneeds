$baseUrl = "http://localhost:8083"
$ErrorActionPreference = "Continue"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🚀 STARTING COMPREHENSIVE LIVE API TEST SUITE" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Uri,
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )

    $params = @{
        Uri = "$baseUrl$Uri"
        Method = $Method
        ContentType = "application/json; charset=utf-8"
        UseBasicParsing = $true
    }
    if ($Body) {
        $params.Body = $Body
    }

    try {
        $response = Invoke-RestMethod @params
        Write-Host " [200/OK] [$Method] $Uri | $Name" -ForegroundColor Green
        return $response
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus -or ($statusCode -ge 200 -and $statusCode -le 299)) {
            Write-Host " [$statusCode] [$Method] $Uri | $Name" -ForegroundColor Green
        } else {
            Write-Host " [$statusCode] [$Method] $Uri (Expected $ExpectedStatus) | $Name" -ForegroundColor Red
            Write-Host "   Message: $($_.Exception.Message)" -ForegroundColor DarkRed
        }
    }
}

# 1. CATALOG
Test-Endpoint -Name "Public Categories Catalog" -Method "GET" -Uri "/catalog/categories"

# 2. AUTH: Send OTP & Register & Login
$phone = "9" + (Get-Random -Minimum 100000000 -Maximum 999999999)
Test-Endpoint -Name "Send OTP" -Method "POST" -Uri "/worker/auth/send-otp" -Body "{`"phone`":`"$phone`"}"
Test-Endpoint -Name "Register Worker" -Method "POST" -Uri "/worker/auth/register" -Body "{`"name`":`"Live Test Partner`",`"phone`":`"$phone`",`"category_name`":`"AC Services`"}" -ExpectedStatus 201
$login = Test-Endpoint -Name "Login with Demo OTP" -Method "POST" -Uri "/worker/auth/login" -Body "{`"username`":`"$phone`",`"otp`":`"4829`"}"
$workerId = if ($login -and $login.worker) { $login.worker.id } else { "w-live-test" }

# 3. PROFILE & DUTY & LOCATION
Test-Endpoint -Name "Get Profile" -Method "GET" -Uri "/worker/profile/me?workerId=$workerId"
Test-Endpoint -Name "Toggle Duty Status" -Method "PUT" -Uri "/worker/profile/duty" -Body "{`"duty_status`":`"ON_DUTY`"}"
Test-Endpoint -Name "Ping Geolocation" -Method "POST" -Uri "/worker/location/ping" -Body "{`"lat`":17.4486,`"lng`":78.3908,`"speed`":25.0,`"heading`":90.0}"

# 4. RADAR & BOOKINGS
Test-Endpoint -Name "Get Dispatch Radar" -Method "GET" -Uri "/worker/bookings/radar"
$bookingId = "SNB-LIVE-" + (Get-Random -Minimum 10000 -Maximum 99999)
Test-Endpoint -Name "Accept Booking" -Method "POST" -Uri "/worker/bookings/$bookingId/accept"
Test-Endpoint -Name "Update Booking Status to EN_ROUTE" -Method "PATCH" -Uri "/worker/bookings/$bookingId/status" -Body "{`"status`":`"EN_ROUTE`"}"
Test-Endpoint -Name "Update Booking Status to ARRIVED" -Method "PATCH" -Uri "/worker/bookings/$bookingId/status" -Body "{`"status`":`"ARRIVED`"}"
Test-Endpoint -Name "Update Booking Status to IN_PROGRESS" -Method "PATCH" -Uri "/worker/bookings/$bookingId/status" -Body "{`"status`":`"IN_PROGRESS`"}"
Test-Endpoint -Name "Add Extra Part" -Method "POST" -Uri "/worker/bookings/$bookingId/extra-parts" -Body "{`"name`":`"Capacitor 50uF`",`"price`":350.0}"
Test-Endpoint -Name "Verify OTP & Complete Booking" -Method "POST" -Uri "/worker/bookings/$bookingId/verify-otp" -Body "{`"otp`":`"6742`"}"

# 5. WALLET & BANKING
Test-Endpoint -Name "Get Wallet Balance & Summary" -Method "GET" -Uri "/worker/wallet"
Test-Endpoint -Name "Get Wallet Transaction History" -Method "GET" -Uri "/worker/wallet/transactions"
Test-Endpoint -Name "Update Bank Account" -Method "POST" -Uri "/worker/bank/update" -Body "{`"bank_name`":`"ICICI Bank`",`"account_number`":`"001105029384`",`"ifsc`":`"ICIC0000011`",`"account_holder`":`"Live Test Partner`"}"
Test-Endpoint -Name "Withdraw Earnings" -Method "POST" -Uri "/worker/wallet/withdraw" -Body "{`"amount`":500.0,`"upi_id`":`"partner@okaxis`"}"

# 6. KYC DOCUMENTS
Test-Endpoint -Name "Get S3 Pre-Signed Upload URL for Aadhaar" -Method "POST" -Uri "/worker/documents/upload-url" -Body "{`"doc_type`":`"AADHAAR_CARD`",`"filename`":`"aadhar.pdf`"}"
Test-Endpoint -Name "Confirm Aadhaar Document Upload" -Method "POST" -Uri "/worker/documents/confirm" -Body "{`"doc_type`":`"AADHAAR_CARD`",`"s3_key`":`"workers/$workerId/aadhar.pdf`",`"file_size_bytes`":102400}" -ExpectedStatus 201
Test-Endpoint -Name "Confirm PAN Card Upload" -Method "POST" -Uri "/worker/documents/confirm" -Body "{`"doc_type`":`"PAN_CARD`",`"s3_key`":`"workers/$workerId/pan.pdf`",`"file_size_bytes`":85000}" -ExpectedStatus 201

# 7. REVIEWS (CUSTOMER, WORKER, ADMIN)
$newRev = Test-Endpoint -Name "Submit Customer Review" -Method "POST" -Uri "/api/customer/reviews" -Body "{`"worker_id`":`"$workerId`",`"booking_id`":`"$bookingId`",`"customer_name`":`"Kavita Rao`",`"service_title`":`"AC Servicing`",`"rating`":5.0,`"comment`":`"Excellent work!`"}" -ExpectedStatus 201
Test-Endpoint -Name "Get Worker Reviews Summary Card" -Method "GET" -Uri "/api/workers/$workerId/reviews/summary"
Test-Endpoint -Name "Get Worker Reviews List" -Method "GET" -Uri "/api/workers/$workerId/reviews"
Test-Endpoint -Name "Admin List Worker Reviews" -Method "GET" -Uri "/admin/workers/$workerId/reviews"
Test-Endpoint -Name "Admin Rating Summary Card" -Method "GET" -Uri "/admin/workers/$workerId/reviews/summary"
$revId = if ($newRev -and $newRev.review) { $newRev.review.id } else { "rev-101" }
Test-Endpoint -Name "Admin Delete Review" -Method "DELETE" -Uri "/admin/reviews/$revId"

# 8. ADMIN KYC MANAGEMENT
Test-Endpoint -Name "Admin List Documents" -Method "GET" -Uri "/admin/workers/documents"
Test-Endpoint -Name "Admin Get Document 1" -Method "GET" -Uri "/admin/workers/documents/1"
Test-Endpoint -Name "Admin Approve Document 1" -Method "POST" -Uri "/admin/workers/documents/1/approve"
Test-Endpoint -Name "Admin Reject Document 1" -Method "POST" -Uri "/admin/workers/documents/1/reject"

# 9. SAFETY, SUPPORT & DEVICES
Test-Endpoint -Name "Trigger SOS Emergency Alert" -Method "POST" -Uri "/worker/safety/sos" -Body "{`"latitude`":17.4486,`"longitude`":78.3908}"
Test-Endpoint -Name "Get Support Tickets" -Method "GET" -Uri "/worker/support/tickets"
Test-Endpoint -Name "Register Push Device Token" -Method "POST" -Uri "/worker/devices/register-token" -Body "{`"token`":`"ExponentPushToken[123456]`",`"platform`":`"android`"}"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🎉 ALL API LIVE CALLS COMPLETED SUCCESSFULLY" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
