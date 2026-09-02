# cURL Examples for Testing APIs

## Customer Endpoints

### 1. Create Customer
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pavan Kumar",
    "email": "pavan@example.com",
    "phone": "9876543210",
    "password": "Password@123",
    "defaultAddress": "Hyderabad"
  }'
```

---

### 2. Get All Customers
```bash
curl -X GET http://localhost:8080/api/customers \
  -H "Content-Type: application/json"
```

---

### 3. Get Customer by ID
```bash
curl -X GET http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json"
```

---

### 4. Update Customer
```bash
curl -X PUT http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pavan Kumar Updated",
    "email": "pavan.new@example.com",
    "phone": "9876543210",
    "defaultAddress": "Bangalore"
  }'
```

---

### 5. Delete Customer
```bash
curl -X DELETE http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json"
```

---

## Address Endpoints

### 1. Create Address
```bash
curl -X POST http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home",
    "addressLine": "12-34 Main Road, Apartment 5B",
    "pincode": "500001",
    "city": "Hyderabad",
    "isDefault": true
  }'
```

---

### 2. Get All Addresses
```bash
curl -X GET http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses \
  -H "Content-Type: application/json"
```

---

### 3. Get Single Address
```bash
curl -X GET http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222 \
  -H "Content-Type: application/json"
```

---

### 4. Update Address
```bash
curl -X PUT http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222 \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home Sweet Home",
    "addressLine": "12-34 Main Road, Apartment 5C",
    "pincode": "500001",
    "city": "Hyderabad",
    "isDefault": false
  }'
```

---

### 5. Delete Address
```bash
curl -X DELETE http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/770e8400-e29b-41d4-a716-446655440222 \
  -H "Content-Type: application/json"
```

---

### 6. Set Default Address
```bash
curl -X PATCH http://localhost:8080/api/customers/550e8400-e29b-41d4-a716-446655440000/addresses/880e8400-e29b-41d4-a716-446655440333/default \
  -H "Content-Type: application/json"
```

---

## Error Testing

### Test Duplicate Email
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Another Person",
    "email": "pavan@example.com",
    "phone": "1234567890",
    "password": "Password@123",
    "defaultAddress": "Mumbai"
  }'
```

---

### Test Customer Not Found
```bash
curl -X GET http://localhost:8080/api/customers/99999999-9999-9999-9999-999999999999 \
  -H "Content-Type: application/json"
```

---

### Test Validation Error
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pavan"
  }'
```

---

## Using cURL with jq (Pretty Print JSON)

If you have `jq` installed, you can prettify responses:

```bash
curl -s -X GET http://localhost:8080/api/customers | jq .
```

---

## Saving Response to Variable

```bash
# Create customer and save response
RESPONSE=$(curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pavan Kumar",
    "email": "pavan@example.com",
    "phone": "9876543210",
    "password": "Password@123",
    "defaultAddress": "Hyderabad"
  }')

# Extract customer ID
CUSTOMER_ID=$(echo $RESPONSE | jq -r '.id')
echo "Customer ID: $CUSTOMER_ID"

# Use customer ID in next request
curl -s -X GET http://localhost:8080/api/customers/$CUSTOMER_ID | jq .
```

---

## Bash Script for Complete Workflow

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

# 1. Create customer
echo "Creating customer..."
CUSTOMER=$(curl -s -X POST $BASE_URL/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pavan Kumar",
    "email": "pavan@example.com",
    "phone": "9876543210",
    "password": "Password@123",
    "defaultAddress": "Hyderabad"
  }')

CUSTOMER_ID=$(echo $CUSTOMER | jq -r '.id')
echo "Customer created: $CUSTOMER_ID"

# 2. Create first address
echo "Creating first address..."
ADDRESS1=$(curl -s -X POST $BASE_URL/api/customers/$CUSTOMER_ID/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home",
    "addressLine": "12-34 Main Road",
    "pincode": "500001",
    "city": "Hyderabad",
    "isDefault": true
  }')

ADDRESS1_ID=$(echo $ADDRESS1 | jq -r '.id')
echo "Address 1 created: $ADDRESS1_ID"

# 3. Create second address
echo "Creating second address..."
ADDRESS2=$(curl -s -X POST $BASE_URL/api/customers/$CUSTOMER_ID/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Office",
    "addressLine": "45 IT Park Road",
    "pincode": "500032",
    "city": "Hyderabad",
    "isDefault": false
  }')

ADDRESS2_ID=$(echo $ADDRESS2 | jq -r '.id')
echo "Address 2 created: $ADDRESS2_ID"

# 4. Get all addresses
echo "Getting all addresses..."
curl -s -X GET $BASE_URL/api/customers/$CUSTOMER_ID/addresses | jq .

# 5. Set second address as default
echo "Setting address 2 as default..."
curl -s -X PATCH $BASE_URL/api/customers/$CUSTOMER_ID/addresses/$ADDRESS2_ID/default

# 6. Get customer details
echo "Getting customer details..."
curl -s -X GET $BASE_URL/api/customers/$CUSTOMER_ID | jq .

# 7. Get all addresses again to verify default changed
echo "Getting all addresses again..."
curl -s -X GET $BASE_URL/api/customers/$CUSTOMER_ID/addresses | jq .
```

Save this as `test.sh` and run:
```bash
chmod +x test.sh
./test.sh
```

