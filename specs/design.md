# Monet Design Document

This document describes Monet design, and is based on the [functional requirements](functional-requirements.md).


## Domain

This sections specifies Monet domain model.  
Registered users, i.e., Buyers, and Painters, are both be treated under one model: User.

#### Painting
- uuid
- artist uuid
- name
- description
- category
- technique
- images
- price

#### Category
- uuid
- label (unique)

#### Technique
- uuid
- label (unique)

#### Cart
- uuid
- user uuid
- list of 
    - painting uuid
- total

#### Order
- uuid
- list of painting uuids
- user uuid
- total
- paymentId

#### Artist
- uuid
- email
- password
- name
- techniques

#### User
- uuid
- email
- password
- name

#### CreditCard
- name
- number
- expiration
- cvc

## API

The following API routes are defined based on the functional requirements and domain design. Unless otherwise specified, a route requires authentication.

### Account

\* POST _/v1/account_  
Open route.  
Inputs:
- email
- password
- name  

Outputs:
- 201 with the uuid and JWT
- 400 Bad Request for invalid input data
- 409 with a message, if email already exists in the db   

Ref: FR-GE01

---

\* POST _/v1/auth/login_  
Open route.  
Inputs:
- email
- password

Outputs:
- 200 with JWT
- 401 with a message, if login failed  

Ref: FR-GE02, FR-GE04

---

\* POST _/v1/auth/logout_  
Open route.  
Outputs:
- 204

Ref: FR-GE03

---

### Categories

\* GET _/categories_  
Open route. 
Output: list of 
- uuid
- label  

Ref: FR-GE06

---

### Techniques

\* GET _/techniques  
Open route. 
Output: list of 
- uuid
- label  

Ref: FR-GE07

---

### Painting

\* GET _/v1/paintings/all_
Open route.
Output:
- 200 with a list of
  - artist id
  - painting (as specified in the Domain section)

Ref: FR-GE05

---

\* POST _/v1/paintings/search_    
Open route.  
Inputs: at least one of:
- category
- technique
- price range (lower and upper)
- artist id

Output: 
- 200 with a list of 
    - artist id
    - painting (as specified in the Domain section)

Ref: FR-GE06

---

\* GET _/v1/paintings/{uuid}  
Open route.  
Output:
- 200 with a painting (as specified in the Domain section)
- 404 if there is no painting with the uuid found

Ref: FR-B01

---

\* POST _/v1/paintings_ 
Inputs:
- artist uuid
- painting (as specified in the Domain section)

Output:
- 200 with the painting uuid
- 401 unauthorized

Ref: FR-P01

---

\* DELETE _/v1/paintings/{uuid}_
Output:
- 200 ok
- 404, if there is no painting with the uuid found

Ref: FR-P02

---

\* PUT _/v1/paintings_  
Inputs:
- artist uuid
- painting (as specified in the Domain section)

Output:
- 200 
- 400 if invalid inputs

Ref: FR-P03

---

### Cart

\* GET _/v1/cart_  
Input:
- user uuid (from jwt)

Output:
- 200 with a Cart (as specified in the Domain section)

Ref: FR-BE02

--- 

\* POST _/v1/cart_  
Inputs:
- user uuid (from jwt)
- painting uuid

Output:
- 200 
- 404, if either user or painting does not exist
- 409 if painting is already in the cart

Ref: FR-B03

--- 

\* DELETE _/v1/cart/{paintingUUID}_  
Output:
- 200 ok
- 404, if there is no painting with the uuid found

Ref: FR-P04

---

### Checkout

\* POST _/v1/checkout_  
Input:
- user uuid
- credit card

Output:
- 200 with an order uuid
- 400 if card is invalid  

Ref: FR-B05

--- 

### Order

\* GET _/v1/orders/all/{userUUID}_  
Output:
- 200 with a list of orders (as specified in the Domain section)

Ref: FR-B07

---

\* GET _/v1/orders/{orderUuid}_  
Output:
- 200 with an order (as specified in the Domain section)

Ref: FR-B08












