# ALL APIS - QUICK TABLE FORMAT

## Summary Table

| # | Method | Endpoint | Description | Status |
|---|--------|----------|-------------|--------|
| 1 | POST | `/api/customers` | Create customer | 201 |
| 2 | GET | `/api/customers` | Get all customers | 200 |
| 3 | GET | `/api/customers/{customerId}` | Get customer by ID | 200 |
| 4 | PUT | `/api/customers/{customerId}` | Update customer | 200 |
| 5 | DELETE | `/api/customers/{customerId}` | Delete customer | 204 |
| 6 | POST | `/api/customers/{customerId}/addresses` | Create address | 201 |
| 7 | GET | `/api/customers/{customerId}/addresses` | Get all addresses | 200 |
| 8 | GET | `/api/customers/{customerId}/addresses/{addressId}` | Get single address | 200 |
| 9 | PUT | `/api/customers/{customerId}/addresses/{addressId}` | Update address | 200 |
| 10 | DELETE | `/api/customers/{customerId}/addresses/{addressId}` | Delete address | 204 |
| 11 | PATCH | `/api/customers/{customerId}/addresses/{addressId}/default` | Set default address | 204 |

---

## Detailed API Reference

### API 1: CREATE CUSTOMER
```
POST /api/customers
```

**Request:**
```json
{
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Hyderabad"
}
```

**Success Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "defaultAddress": "Hyderabad",
  "emailVerified": false,
  "phoneVerified": false,
  "accountStatus": "ACTIVE"
}
```

**Error (409) - Duplicate Email:**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 409,
  "error": "Duplicate Email",
  "message": "Email already exists: pavan@example.com"
}
```

---

### API 2: GET ALL CUSTOMERS
```
GET /api/customers
```

**Request:**
```
(no body)
```

**Success Response (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Pavan Kumar",
    "email": "pavan@example.com",
    "phone": "9876543210",
    "defaultAddress": "Hyderabad",
    "emailVerified": false,
    "phoneVerified": false,
    "accountStatus": "ACTIVE"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440111",
    "name": "Rajesh Singh",
    "email": "rajesh@example.com",
    "phone": "8765432109",
    "defaultAddress": "Delhi",
    "emailVerified": true,
    "phoneVerified": true,
    "accountStatus": "ACTIVE"
  }
]
```

---

### API 3: GET CUSTOMER BY ID
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000
```

**Request:**
```
(no body)
```

**Success Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "defaultAddress": "Hyderabad",
  "emailVerified": false,
  "phoneVerified": false,
  "accountStatus": "ACTIVE"
}
```

**Error (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### API 4: UPDATE CUSTOMER
```
PUT /api/customers/550e8400-e29b-41d4-a716-446655440000
```

**Request:**
```json
{
  "name": "Pavan Kumar Updated",
  "email": "pavan.new@example.com",
  "phone": "9876543210",
  "defaultAddress": "Bangalore"
}
```

**Success Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Pavan Kumar Updated",
  "email": "pavan.new@example.com",
  "phone": "9876543210",
  "defaultAddress": "Bangalore",
  "emailVerified": false,
  "phoneVerified": false,
  "accountStatus": "ACTIVE"
}
```

**Note:** Cannot update: passwordHash, emailVerified, phoneVerified, accountStatus

---

### API 5: DELETE CUSTOMER
```
DELETE /api/customers/550e8400-e29b-41d4-a716-446655440000
```

**Request:**
```
(no body)
```

**Success Response (204):**
```
(empty body)
```

---

### API 6: CREATE ADDRESS
```
POST /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
```

**Request:**
```json
{
  "label": "Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

**Success Response (201):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440222",
  "label": "Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

**Behavior:** If isDefault is true, other default addresses are automatically set to false

---

### API 7: GET ALL ADDRESSES
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
```

**Request:**
```
(no body)
```

**Success Response (200):**
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440222",
    "label": "Home",
    "addressLine": "12-34 Main Road, Apartment 5B",
    "pincode": "500001",
    "city": "Hyderabad",
    "isDefault": true
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440333",
    "label": "Office",
    "addressLine": "45 IT Park Road, Building A",
    "pincode": "500032",
    "city": "Hyderabad",
    "isDefault": false
  }
]
```

---

### API 8: GET SINGLE ADDRESS
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222
```

**Request:**
```
(no body)
```

**Success Response (200):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440222",
  "label": "Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

**Important:** Address must belong to the specified customer, otherwise 404

---

### API 9: UPDATE ADDRESS
```
PUT /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222
```

**Request:**
```json
{
  "label": "Home Sweet Home",
  "addressLine": "12-34 Main Road, Apartment 5C",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": false
}
```

**Success Response (200):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440222",
  "label": "Home Sweet Home",
  "addressLine": "12-34 Main Road, Apartment 5C",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": false
}
```

---

### API 10: DELETE ADDRESS
```
DELETE /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222
```

**Request:**
```
(no body)
```

**Success Response (204):**
```
(empty body)
```

---

### API 11: SET DEFAULT ADDRESS
```
PATCH /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/880e8400-e29b-41d4-a716-446655440333/default
```

**Request:**
```
(no body)
```

**Success Response (204):**
```
(empty body)
```

**Behavior:** Sets specified address as default, unsets all other default addresses for this customer

---

## Sample Test Data

### Customer 1
```json
{
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Hyderabad"
}
```

### Customer 2
```json
{
  "name": "Rajesh Singh",
  "email": "rajesh@example.com",
  "phone": "8765432109",
  "password": "SecurePass@456",
  "defaultAddress": "Delhi"
}
```

### Address 1 - Home
```json
{
  "label": "Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

### Address 2 - Office
```json
{
  "label": "Office",
  "addressLine": "45 IT Park Road, Building A",
  "pincode": "500032",
  "city": "Hyderabad",
  "isDefault": false
}
```

### Address 3 - Parents
```json
{
  "label": "Parents",
  "addressLine": "123 Garden Lane",
  "pincode": "500050",
  "city": "Hyderabad",
  "isDefault": false
}
```

---

## All Possible HTTP Status Codes

| Status | Meaning | Scenario |
|--------|---------|----------|
| 200 | OK | GET, PUT successful |
| 201 | Created | POST successful |
| 204 | No Content | DELETE, PATCH successful |
| 400 | Bad Request | Validation errors in request body |
| 404 | Not Found | Resource doesn't exist or doesn't belong to customer |
| 409 | Conflict | Duplicate email or phone |
| 500 | Internal Server Error | Server-side error |

---

## Copy-Paste Ready URLs

### Customer URLs
```
POST   http://localhost:8080/api/customers
GET    http://localhost:8080/api/customers
GET    http://localhost:8080/api/customers/{customerId}
PUT    http://localhost:8080/api/customers/{customerId}
DELETE http://localhost:8080/api/customers/{customerId}
```

### Address URLs
```
POST   http://localhost:8080/api/customers/{customerId}/addresses
GET    http://localhost:8080/api/customers/{customerId}/addresses
GET    http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
PUT    http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
DELETE http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
PATCH  http://localhost:8080/api/customers/{customerId}/addresses/{addressId}/default
```

---

## Required Request Headers

```
Content-Type: application/json
Accept: application/json
```

---

## Key Implementation Details

✅ **Password Hashing:** Passwords are BCrypt hashed, never returned in responses

✅ **UUID Handling:** All IDs are UUIDs (36-character strings with hyphens)

✅ **Default Address Logic:** Only one per customer, automatically managed

✅ **Address Ownership:** Cross-customer access returns 404 (security feature)

✅ **Cascade Delete:** Deleting customer auto-deletes all addresses

✅ **Uniqueness:** Email and phone must be unique across all customers

✅ **Lazy Loading:** Addresses loaded on demand, not in customer response

