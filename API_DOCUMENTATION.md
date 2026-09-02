# Salary Needs - Customer & Address APIs
## Complete API Flow with Sample Requests & Responses

---

## Database Configuration
Before testing, ensure your MySQL database is running and configured:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/salaryneeds?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

---

## 1. CUSTOMER APIs

### 1.1 Create Customer
**POST** `/api/customers`

**Description:** Register a new customer with email and phone uniqueness validation

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Hyderabad"
}
```

**Success Response (201 Created):**
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

**Error Response - Duplicate Email (409 Conflict):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 409,
  "error": "Duplicate Email",
  "message": "Email already exists: pavan@example.com"
}
```

**Error Response - Duplicate Phone (409 Conflict):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 409,
  "error": "Duplicate Phone",
  "message": "Phone already exists: 9876543210"
}
```

**Error Response - Validation Failed (400 Bad Request):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": {
    "name": "Name is required",
    "email": "Email must be valid",
    "phone": "Phone is required",
    "password": "Password is required"
  }
}
```

---

### 1.2 Get All Customers
**GET** `/api/customers`

**Description:** Retrieve all customers in the system

**Response (200 OK):**
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

### 1.3 Get Customer by ID
**GET** `/api/customers/{customerId}`

**Description:** Retrieve a specific customer by UUID

**Example URL:**
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
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

**Error Response - Not Found (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 1.4 Update Customer
**PUT** `/api/customers/{customerId}`

**Description:** Update customer profile (name, email, phone, defaultAddress only)

**Example URL:**
```
PUT /api/customers/550e8400-e29b-41d4-a716-446655440000
```

**Request Body:**
```json
{
  "name": "Pavan Kumar Updated",
  "email": "pavan.new@example.com",
  "phone": "9876543210",
  "defaultAddress": "Bangalore"
}
```

**Response (200 OK):**
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

**Note:** The following fields CANNOT be updated via this API:
- `passwordHash`
- `emailVerified`
- `phoneVerified`
- `accountStatus`

---

### 1.5 Delete Customer
**DELETE** `/api/customers/{customerId}`

**Description:** Delete a customer and all associated addresses (cascade delete)

**Example URL:**
```
DELETE /api/customers/550e8400-e29b-41d4-a716-446655440000
```

**Response (204 No Content):**
```
(Empty body - just status code 204)
```

**Error Response - Not Found (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 2. ADDRESS APIs
All Address APIs are nested under Customer to represent the One-to-Many relationship.

**Base URL:** `/api/customers/{customerId}/addresses`

### 2.1 Create Address for a Customer
**POST** `/api/customers/{customerId}/addresses`

**Description:** Create a new address for a specific customer. Customer ID comes from URL, not body.

**Example URL:**
```
POST /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
```

**Request Body:**
```json
{
  "label": "Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

**Success Response (201 Created):**
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

**Behavior:**
- If `isDefault` is `true`, any existing default address for this customer will be set to `false`
- If `isDefault` is `false` or not provided, new address will not be default

**Error Response - Customer Not Found (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 2.2 Get All Addresses for a Customer
**GET** `/api/customers/{customerId}/addresses`

**Description:** Retrieve all addresses belonging to a specific customer

**Example URL:**
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
```

**Response (200 OK):**
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
  },
  {
    "id": "990e8400-e29b-41d4-a716-446655440444",
    "label": "Parents",
    "addressLine": "123 Garden Lane",
    "pincode": "500050",
    "city": "Hyderabad",
    "isDefault": false
  }
]
```

**Error Response - Customer Not Found (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 2.3 Get Single Address
**GET** `/api/customers/{customerId}/addresses/{addressId}`

**Description:** Retrieve a specific address. The address MUST belong to the specified customer.

**Example URL:**
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222
```

**Response (200 OK):**
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

**Error Response - Customer Not Found (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Error Response - Address Not Found or Doesn't Belong to Customer (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Address Not Found",
  "message": "Address not found with id: 770e8400-e29b-41d4-a716-446655440222 for customer: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Important:** If the address belongs to a different customer, it will return 404.

---

### 2.4 Update Address
**PUT** `/api/customers/{customerId}/addresses/{addressId}`

**Description:** Update an address for a customer

**Example URL:**
```
PUT /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222
```

**Request Body:**
```json
{
  "label": "Home Sweet Home",
  "addressLine": "12-34 Main Road, Apartment 5C",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

**Response (200 OK):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440222",
  "label": "Home Sweet Home",
  "addressLine": "12-34 Main Road, Apartment 5C",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

**Behavior:**
- You can update any of the following fields: `label`, `addressLine`, `pincode`, `city`, `isDefault`
- If you set `isDefault: true`, other default addresses for this customer will automatically be set to `false`

---

### 2.5 Delete Address
**DELETE** `/api/customers/{customerId}/addresses/{addressId}`

**Description:** Delete a specific address for a customer

**Example URL:**
```
DELETE /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222
```

**Response (204 No Content):**
```
(Empty body - just status code 204)
```

**Error Response - Address Not Found (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Address Not Found",
  "message": "Address not found with id: 770e8400-e29b-41d4-a716-446655440222 for customer: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 2.6 Set Default Address
**PATCH** `/api/customers/{customerId}/addresses/{addressId}/default`

**Description:** Set a specific address as the default address for a customer. Only one default address per customer is allowed.

**Example URL:**
```
PATCH /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/880e8400-e29b-41d4-a716-446655440333/default
```

**Request Body:**
```
(Empty body)
```

**Response (204 No Content):**
```
(Empty body - just status code 204)
```

**Behavior Before Request:**
```
Customer ID: 550e8400-e29b-41d4-a716-446655440000

Addresses:
├── Home (id: 770e8400-e29b-41d4-a716-446655440222) → isDefault: true
├── Office (id: 880e8400-e29b-41d4-a716-446655440333) → isDefault: false
└── Parents (id: 990e8400-e29b-41d4-a716-446655440444) → isDefault: false
```

**Behavior After Request:**
```
Customer ID: 550e8400-e29b-41d4-a716-446655440000

Addresses:
├── Home (id: 770e8400-e29b-41d4-a716-446655440222) → isDefault: false
├── Office (id: 880e8400-e29b-41d4-a716-446655440333) → isDefault: true
└── Parents (id: 990e8400-e29b-41d4-a716-446655440444) → isDefault: false
```

**Error Response - Address Not Found (404):**
```json
{
  "timestamp": "2026-09-01T10:30:45.123456",
  "status": 404,
  "error": "Address Not Found",
  "message": "Address not found with id: 880e8400-e29b-41d4-a716-446655440333 for customer: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 3. COMPLETE WORKFLOW EXAMPLE

### Step 1: Create a Customer
```
POST /api/customers
Content-Type: application/json

{
  "name": "Pavan Kumar",
  "email": "pavan@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Hyderabad"
}
```

**Response:** Customer created with `id = 550e8400-e29b-41d4-a716-446655440000`

---

### Step 2: Create First Address (Home)
```
POST /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
Content-Type: application/json

{
  "label": "Home",
  "addressLine": "12-34 Main Road",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

**Response:** Address created with `id = 770e8400-e29b-41d4-a716-446655440222` and `isDefault: true`

---

### Step 3: Create Second Address (Office)
```
POST /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
Content-Type: application/json

{
  "label": "Office",
  "addressLine": "45 IT Park Road",
  "pincode": "500032",
  "city": "Hyderabad",
  "isDefault": false
}
```

**Response:** Address created with `id = 880e8400-e29b-41d4-a716-446655440333` and `isDefault: false`

---

### Step 4: Get All Addresses
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
```

**Response:** Returns both addresses with Home as default

---

### Step 5: Set Office as Default
```
PATCH /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/880e8400-e29b-41d4-a716-446655440333/default
```

**Result:** Home is no longer default, Office becomes default

---

### Step 6: Verify Changes
```
GET /api/customers/550e8400-e29b-41d4-a716-446655440000/addresses
```

**Response:** Now Office is `isDefault: true` and Home is `isDefault: false`

---

## 4. HTTP STATUS CODES REFERENCE

| Status | Meaning | Scenario |
|--------|---------|----------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST request |
| 204 | No Content | Successful DELETE, PATCH request |
| 400 | Bad Request | Validation errors |
| 404 | Not Found | Resource doesn't exist or doesn't belong to customer |
| 409 | Conflict | Duplicate email or phone |
| 500 | Internal Server Error | Server-side error |

---

## 5. POSTMAN COLLECTION SETUP

### Import Base URL
```
http://localhost:8080
```

### Environment Variables (Optional)
```
customerId = 550e8400-e29b-41d4-a716-446655440000
addressId = 770e8400-e29b-41d4-a716-446655440222
```

### Headers (for all requests)
```
Content-Type: application/json
Accept: application/json
```

---

## 6. IMPORTANT NOTES

✅ **Password Handling:**
- Passwords are hashed using BCrypt during customer creation
- Password hash is NEVER returned in any response
- Password cannot be updated via the update customer endpoint

✅ **Email & Phone Uniqueness:**
- Email must be unique across all customers
- Phone must be unique across all customers
- Updating a customer's email/phone will validate uniqueness again

✅ **Default Address Logic:**
- Only one address per customer can be default
- Setting a new default automatically unsets the previous one
- This is handled within a transaction for data consistency

✅ **Customer Deletion:**
- Deleting a customer automatically deletes all associated addresses (CASCADE)
- This is handled using orphan removal

✅ **Error Handling:**
- All errors follow a standard response format
- Validation errors return detailed field-level messages
- Resources not found return specific entity names

✅ **Address Ownership Validation:**
- An address MUST belong to the specified customer
- If you try to access an address from a different customer's URL, it returns 404
- This prevents cross-customer data leakage

