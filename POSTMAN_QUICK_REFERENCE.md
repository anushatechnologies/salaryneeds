# POSTMAN QUICK REFERENCE - Request & Response Examples

## BASE URL
```
http://localhost:8080
```

---

# CUSTOMER APIs

## 1️⃣ CREATE CUSTOMER
**POST** `http://localhost:8080/api/customers`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Hyderabad"
}
```

### Response (201 Created)
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

### Save this ID for next requests:
```
customerId = 550e8400-e29b-41d4-a716-446655440000
```

---

## 2️⃣ GET ALL CUSTOMERS
**GET** `http://localhost:8080/api/customers`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```
(empty)
```

### Response (200 OK)
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

## 3️⃣ GET CUSTOMER BY ID
**GET** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```
(empty)
```

### Response (200 OK)
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

---

## 4️⃣ UPDATE CUSTOMER
**PUT** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "name": "Pavan Kumar Updated",
  "email": "pavan.new@example.com",
  "phone": "9876543210",
  "defaultAddress": "Bangalore"
}
```

### Response (200 OK)
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

---

## 5️⃣ DELETE CUSTOMER
**DELETE** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```
(empty)
```

### Response (204 No Content)
```
(empty body)
```

---

# ADDRESS APIs

## 6️⃣ CREATE ADDRESS
**POST** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "label": "Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

### Response (201 Created)
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

### Save this ID for next requests:
```
addressId = 770e8400-e29b-41d4-a716-446655440222
```

---

## 7️⃣ CREATE SECOND ADDRESS
**POST** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "label": "Office",
  "addressLine": "45 IT Park Road, Building A",
  "pincode": "500032",
  "city": "Hyderabad",
  "isDefault": false
}
```

### Response (201 Created)
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440333",
  "label": "Office",
  "addressLine": "45 IT Park Road, Building A",
  "pincode": "500032",
  "city": "Hyderabad",
  "isDefault": false
}
```

### Save this ID:
```
addressId2 = 880e8400-e29b-41d4-a716-446655440333
```

---

## 8️⃣ GET ALL ADDRESSES
**GET** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```
(empty)
```

### Response (200 OK)
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

## 9️⃣ GET SINGLE ADDRESS
**GET** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```
(empty)
```

### Response (200 OK)
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

---

## 🔟 UPDATE ADDRESS
**PUT** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "label": "Home Sweet Home",
  "addressLine": "12-34 Main Road, Apartment 5C",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": false
}
```

### Response (200 OK)
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

## 1️⃣1️⃣ DELETE ADDRESS
**DELETE** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```
(empty)
```

### Response (204 No Content)
```
(empty body)
```

---

## 1️⃣2️⃣ SET DEFAULT ADDRESS
**PATCH** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/880e8400-e29b-41d4-a716-446655440333/default`

### Request Headers
```
Content-Type: application/json
```

### Request Body
```
(empty)
```

### Response (204 No Content)
```
(empty body)
```

**Result:** Address 880e8400... (Office) becomes default, Address 770e8400... (Home) is no longer default

---

# ERROR RESPONSES

## ❌ DUPLICATE EMAIL (409 Conflict)
### Request:
**POST** `http://localhost:8080/api/customers`
```json
{
  "name": "Another Person",
  "email": "pavan@example.com",
  "phone": "1234567890",
  "password": "Password@123",
  "defaultAddress": "Mumbai"
}
```

### Response (409 Conflict)
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 409,
  "error": "Duplicate Email",
  "message": "Email already exists: pavan@example.com"
}
```

---

## ❌ DUPLICATE PHONE (409 Conflict)
### Request:
**POST** `http://localhost:8080/api/customers`
```json
{
  "name": "Someone Else",
  "email": "someone@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Delhi"
}
```

### Response (409 Conflict)
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 409,
  "error": "Duplicate Phone",
  "message": "Phone already exists: 9876543210"
}
```

---

## ❌ CUSTOMER NOT FOUND (404)
### Request:
**GET** `http://localhost:8080/api/customers/99999999-9999-9999-9999-999999999999`

### Response (404 Not Found)
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with id: 99999999-9999-9999-9999-999999999999"
}
```

---

## ❌ ADDRESS NOT FOUND (404)
### Request:
**GET** `http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/99999999-9999-9999-9999-999999999999`

### Response (404 Not Found)
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Address Not Found",
  "message": "Address not found with id: 99999999-9999-9999-9999-999999999999 for customer: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

## ❌ VALIDATION ERROR (400 Bad Request)
### Request:
**POST** `http://localhost:8080/api/customers`
```json
{
  "name": "Pavan"
}
```

### Response (400 Bad Request)
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": {
    "email": "Email must be valid",
    "phone": "Phone is required",
    "password": "Password is required"
  }
}
```

---

# TESTING SEQUENCE FOR POSTMAN

Copy and paste this sequence to test all APIs in order:

### Step 1: Create Customer
```
POST http://localhost:8080/api/customers
{
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Hyderabad"
}
```
✅ Expected: 201 Created
📌 **Save the returned `id` as customerId**

---

### Step 2: Get All Customers
```
GET http://localhost:8080/api/customers
```
✅ Expected: 200 OK

---

### Step 3: Get Customer by ID
```
GET http://localhost:8080/api/customers/{customerId}
```
✅ Expected: 200 OK

---

### Step 4: Create First Address (Home)
```
POST http://localhost:8080/api/customers/{customerId}/addresses
{
  "label": "Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```
✅ Expected: 201 Created
📌 **Save the returned `id` as addressId**

---

### Step 5: Create Second Address (Office)
```
POST http://localhost:8080/api/customers/{customerId}/addresses
{
  "label": "Office",
  "addressLine": "45 IT Park Road, Building A",
  "pincode": "500032",
  "city": "Hyderabad",
  "isDefault": false
}
```
✅ Expected: 201 Created
📌 **Save the returned `id` as addressId2**

---

### Step 6: Get All Addresses
```
GET http://localhost:8080/api/customers/{customerId}/addresses
```
✅ Expected: 200 OK with 2 addresses

---

### Step 7: Get Single Address
```
GET http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
```
✅ Expected: 200 OK

---

### Step 8: Update Address
```
PUT http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
{
  "label": "Home Sweet Home",
  "addressLine": "12-34 Main Road, Apartment 5C",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": false
}
```
✅ Expected: 200 OK

---

### Step 9: Set Office as Default
```
PATCH http://localhost:8080/api/customers/{customerId}/addresses/{addressId2}/default
```
✅ Expected: 204 No Content

---

### Step 10: Verify Default Changed
```
GET http://localhost:8080/api/customers/{customerId}/addresses
```
✅ Expected: 200 OK with Office as default (isDefault: true), Home not default

---

### Step 11: Update Customer
```
PUT http://localhost:8080/api/customers/{customerId}
{
  "name": "Pavan Kumar Updated",
  "email": "pavan.updated@example.com",
  "phone": "9876543210",
  "defaultAddress": "Bangalore"
}
```
✅ Expected: 200 OK

---

### Step 12: Delete Address
```
DELETE http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
```
✅ Expected: 204 No Content

---

### Step 13: Delete Customer
```
DELETE http://localhost:8080/api/customers/{customerId}
```
✅ Expected: 204 No Content

---

# POSTMAN ENVIRONMENT VARIABLES SETUP

Create these in Postman Environment:

```
baseUrl    = http://localhost:8080
customerId = (copy from Step 1 response)
addressId  = (copy from Step 4 response)
addressId2 = (copy from Step 5 response)
```

Then use in URLs:
- `{{baseUrl}}/api/customers`
- `{{baseUrl}}/api/customers/{{customerId}}`
- `{{baseUrl}}/api/customers/{{customerId}}/addresses/{{addressId}}`

---

# TIPS FOR POSTMAN TESTING

✅ **Set Content-Type Header** for all requests (except GET and DELETE which can be empty body)

✅ **Use Variables** to avoid copy-pasting IDs

✅ **Chain Requests** using Tests tab to extract IDs:
```javascript
var jsonData = pm.response.json();
pm.environment.set("customerId", jsonData.id);
```

✅ **Check Response Status** in Tests tab:
```javascript
pm.test("Status code is 201", function() {
    pm.response.to.have.status(201);
});
```

✅ **Pretty Print Responses** - Postman does this automatically

