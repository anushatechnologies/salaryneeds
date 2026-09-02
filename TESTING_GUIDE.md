# API Testing Quick Start Guide

## Prerequisites

1. **Start MySQL Server**
   - Ensure MySQL is running on `localhost:3306`
   - Database: `salaryneeds` (auto-created)
   - Username: `root`
   - Password: `root`

2. **Start Spring Boot Application**
   ```bash
   cd c:\Users\Pavan\OneDrive\Desktop\salaryneeds
   mvn spring-boot:run
   ```
   - Application will start on `http://localhost:8080`
   - Wait for "Started SalaryNeedsApplication" message

3. **Open Postman**
   - Import the `Postman_Collection.json` file
   - Set `baseUrl` environment variable to `http://localhost:8080`

---

## Quick Testing Workflow

### Test 1: Create First Customer
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
📝 **Save the returned `id` as `customerId`**

---

### Test 2: Get All Customers
```
GET http://localhost:8080/api/customers
```

✅ Expected: 200 OK with array of customers

---

### Test 3: Get Customer by ID
```
GET http://localhost:8080/api/customers/{customerId}
```

✅ Expected: 200 OK with customer details

---

### Test 4: Create First Address (Home)
```
POST http://localhost:8080/api/customers/{customerId}/addresses

{
  "label": "Home",
  "addressLine": "12-34 Main Road",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": true
}
```

✅ Expected: 201 Created
📝 **Save the returned `id` as `addressId`**

---

### Test 5: Create Second Address (Office)
```
POST http://localhost:8080/api/customers/{customerId}/addresses

{
  "label": "Office",
  "addressLine": "45 IT Park Road",
  "pincode": "500032",
  "city": "Hyderabad",
  "isDefault": false
}
```

✅ Expected: 201 Created

---

### Test 6: Get All Addresses
```
GET http://localhost:8080/api/customers/{customerId}/addresses
```

✅ Expected: 200 OK with array of 2 addresses

---

### Test 7: Get Single Address
```
GET http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
```

✅ Expected: 200 OK with address details

---

### Test 8: Update Address
```
PUT http://localhost:8080/api/customers/{customerId}/addresses/{addressId}

{
  "label": "Home Sweet Home",
  "addressLine": "12-34 Main Road, Apartment 5B",
  "pincode": "500001",
  "city": "Hyderabad",
  "isDefault": false
}
```

✅ Expected: 200 OK with updated address

---

### Test 9: Set Office as Default
```
PATCH http://localhost:8080/api/customers/{customerId}/addresses/{office_address_id}/default
```

✅ Expected: 204 No Content
📝 **Now Office should be default, Home should not be default**

---

### Test 10: Verify Default Address Changed
```
GET http://localhost:8080/api/customers/{customerId}/addresses
```

✅ Expected: 
- Home: `"isDefault": false`
- Office: `"isDefault": true`

---

### Test 11: Update Customer
```
PUT http://localhost:8080/api/customers/{customerId}

{
  "name": "Pavan Kumar Updated",
  "email": "pavan.updated@example.com",
  "phone": "9876543210",
  "defaultAddress": "Bangalore"
}
```

✅ Expected: 200 OK with updated customer

---

### Test 12: Delete Address
```
DELETE http://localhost:8080/api/customers/{customerId}/addresses/{addressId}
```

✅ Expected: 204 No Content

---

### Test 13: Delete Customer
```
DELETE http://localhost:8080/api/customers/{customerId}
```

✅ Expected: 204 No Content
📝 **All addresses for this customer will be automatically deleted**

---

## Error Testing

### Test Error 1: Duplicate Email
Try creating a customer with the same email as Test 1

```
POST http://localhost:8080/api/customers

{
  "name": "Someone Else",
  "email": "pavan@example.com",
  "phone": "1234567890",
  "password": "Password@123",
  "defaultAddress": "Mumbai"
}
```

✅ Expected: 409 Conflict
```json
{
  "status": 409,
  "error": "Duplicate Email",
  "message": "Email already exists: pavan@example.com"
}
```

---

### Test Error 2: Duplicate Phone
Try creating a customer with the same phone as Test 1

```
POST http://localhost:8080/api/customers

{
  "name": "Another Person",
  "email": "another@example.com",
  "phone": "9876543210",
  "password": "Password@123",
  "defaultAddress": "Delhi"
}
```

✅ Expected: 409 Conflict

---

### Test Error 3: Customer Not Found
Try to get a non-existent customer

```
GET http://localhost:8080/api/customers/99999999-9999-9999-9999-999999999999
```

✅ Expected: 404 Not Found

---

### Test Error 4: Address Not Found
Try to get a non-existent address

```
GET http://localhost:8080/api/customers/{customerId}/addresses/99999999-9999-9999-9999-999999999999
```

✅ Expected: 404 Not Found

---

### Test Error 5: Address Doesn't Belong to Customer
Create two customers, try to access one customer's address using another customer's ID

```
GET http://localhost:8080/api/customers/{customer1_id}/addresses/{customer2_address_id}
```

✅ Expected: 404 Not Found (Security feature - prevents cross-customer data leakage)

---

### Test Error 6: Validation Error
Try creating a customer without required fields

```
POST http://localhost:8080/api/customers

{
  "name": "Pavan Kumar"
}
```

✅ Expected: 400 Bad Request
```json
{
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

## Database Verification

After testing, you can verify the database:

```sql
-- Check customers
SELECT * FROM CUSTOMERS;

-- Check addresses
SELECT * FROM ADDRESSES;

-- Check relationships
SELECT c.id, c.name, c.email, a.id, a.label, a.is_default 
FROM CUSTOMERS c 
LEFT JOIN ADDRESSES a ON c.id = a.customer_id;
```

---

## Important Notes

1. **Password Security**: Passwords are hashed with BCrypt. Never return or log raw passwords.

2. **Email & Phone**: Must be unique. The system validates this automatically.

3. **Default Address**: Only one address per customer can be default. Setting a new default automatically unsets the old one.

4. **Data Ownership**: Addresses must belong to the specified customer. Cross-customer access returns 404.

5. **Cascade Delete**: Deleting a customer automatically deletes all associated addresses.

6. **Transaction Safety**: Default address changes are wrapped in transactions to ensure consistency.

---

## Postman Environment Setup

In Postman, create/update these variables:

```
baseUrl = http://localhost:8080
customerId = (copy from first customer creation response)
addressId = (copy from first address creation response)
```

Then use:
- `{{baseUrl}}` in URLs
- `{{customerId}}` in paths
- `{{addressId}}` in paths

---

## Troubleshooting

### Application Won't Start
- Check if port 8080 is already in use
- Verify Java 21 is installed: `java -version`
- Check application.properties database URL

### Database Connection Error
- Start MySQL: `net start MySQL80` (Windows)
- Verify credentials: `mysql -u root -p`
- Check if database can be created

### Request Returns 500
- Check application logs for detailed error message
- Verify JSON request body is valid
- Check if all required fields are present

### Duplicate Email/Phone Error When Shouldn't Be
- Make sure you're using different emails/phones for each customer
- Or clean database: `DROP DATABASE salaryneeds;` and restart

