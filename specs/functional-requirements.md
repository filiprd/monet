# Monet Functional Requirements Document

Monet is an online retail platform for paintings.

## Scope
- Monet only deals with paintings and does not consider other artworks.

## Users

Monet has several kinds of users/accounts:
- Painters, who are registered users which offer their paintings for sale
- Buyers, who are registered users that buy paintings
- Visitors, who are non-logged users browsing the painting collections

Painters can be Buyers att the same time, and vice-versa.

## Functional requirements

This document starts from general functional requirements (FRs) and then divides remaining functional requirements according to Monet user kinds.

### General FRs

*FR-GE01.* The system must allow Visitors to register for an account.  

*FR-GE02.* The system must allow Painters and Buyers to login.  

*FR-GE03.* The system must allow Painters and Buyers to logout.  

*FR-GE04.* The system must be able to verify credentials upon Visitor login attempt. 

*FR-GE05.* The system must be able to retrieve all paintings.

*FR-GE06.* The system must be able to provide painting search functionality to all users. Painting search must be performed with at least one of the following parameters:
- Category
- Technique
- Price range
- Painter 
If multiple parameters are specified, the final result is an intersection of each separate result.

*FR-GE07.* The system must be able to retrieve all categories.

*FR-GE08.* The system must be able to retrieve all techniques.

### Painters FRs

*FR-P01.* The system must allow Painters to add a painting for sale.  

*FR-P02.* The system must allow Painters to remove a painting from sale.  

*FR-P03.* The system must allow Painters to update information of a painting previously offered for sale.  

### Buyers FRs

*FR-B01.* The system must allow Buyers to browse a page of a specific painting.  

*FR-B02.* The system must allow Buyers to browse paintings in their carts.  

*FR-B03.* The system must allow Buyers to add a painting to their cart.  

*FR-B04.* The system must allow Buyers to remove a painting from their cart.  

*FR-B05.* The system must allow Buyers to check out their cart, i.e., to make a purchase order.

*FR-B06.* The system must allow Buyers to browse their order history.  

*FR-B07.* The system must allow Buyers to inspect all details of each individual order from their purchase history.




