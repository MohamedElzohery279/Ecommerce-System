This is an ecommerce system that allow to add products with different features.
This is an example of output:
** Shipment notice **
Cheese 400g
Biscuits 700g
TV 15000g
Total package weight 16.1kg
** Checkout receipt **
2x Cheese 200
1x Biscuits 150
1x TV 1000
3x Mobile scratch card 150
Subtotal 1500
Shipping 90
Amount 1590
Customer balance after payment: 3410

--- Testing error cases ---
Error: Cannot checkout with empty cart
Error: Insufficient balance
Error: Product Expired Cheese is expired
Error: Product Biscuits is out of stock
