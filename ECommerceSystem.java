import java.util.ArrayList;
import java.util.Date;
import java.util.List;

interface Shippable {
    String getName();
    double getWeight();
}

interface Expirable {
    boolean isExpired();
    Date getExpiryDate();
}

abstract class Product {
    private String name;
    private double price;
    private int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public abstract boolean requiresShipping();
}

class NonExpirableProduct extends Product {
    public NonExpirableProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }
}

class ExpirableProduct extends Product implements Expirable {
    private Date expiryDate;

    public ExpirableProduct(String name, double price, int quantity, Date expiryDate) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isExpired() {
        return new Date().after(expiryDate);
    }

    @Override
    public Date getExpiryDate() {
        return expiryDate;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }
}

class ShippableProduct extends NonExpirableProduct implements Shippable {
    private double weight; 

    public ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }
}

class NonShippableProduct extends NonExpirableProduct {
    public NonShippableProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }

    @Override
    public boolean requiresShipping() {
        return false;
    }
}

class ExpirableShippableProduct extends ExpirableProduct implements Shippable {
    private double weight; // in kg

    public ExpirableShippableProduct(String name, double price, int quantity, Date expiryDate, double weight) {
        super(name, price, quantity, expiryDate);
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }
}

class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

class ShoppingCart {
    private List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock for " + product.getName());
        }

        for (CartItem item : items) {
            if (item.getProduct().equals(product)) {
                throw new IllegalArgumentException("Product already in cart. Update quantity instead.");
            }
        }

        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double calculateSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public List<Shippable> getShippableItems() {
        List<Shippable> shippableItems = new ArrayList<>();
        for (CartItem item : items) {
            if (item.getProduct() instanceof Shippable && item.getProduct().requiresShipping()) {
                shippableItems.add((Shippable) item.getProduct());
            }
        }
        return shippableItems;
    }

    public void clear() {
        items.clear();
    }
}

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void deductBalance(double amount) {
        if (balance < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        balance -= amount;
    }
}

class ShippingService {
    public void shipItems(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;

        for (Shippable item : items) {
            double weight = item.getWeight();
            System.out.printf("%s %.0fg%n", item.getName(), weight * 1000);
            totalWeight += weight;
        }

        System.out.printf("Total package weight %.1fkg%n", totalWeight);
    }
}

// E-commerce system
class ECommerceSystem {
    private ShippingService shippingService = new ShippingService();

    public void checkout(Customer customer, ShoppingCart cart) {
        // Validate cart
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot checkout with empty cart");
        }

        // Check for expired or out of stock items
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Product " + product.getName() + " is out of stock");
            }

            if (product instanceof Expirable) {
                Expirable expirable = (Expirable) product;
                if (expirable.isExpired()) {
                    throw new IllegalStateException("Product " + product.getName() + " is expired");
                }
            }
        }

        // Calculate costs
        double subtotal = cart.calculateSubtotal();
        double shippingFee = calculateShippingFee(cart);
        double total = subtotal + shippingFee;

        // Check customer balance
        if (customer.getBalance() < total) {
            throw new IllegalStateException("Insufficient balance");
        }

        // Process shipping
        List<Shippable> shippableItems = cart.getShippableItems();
        if (!shippableItems.isEmpty()) {
            shippingService.shipItems(shippableItems);
        }

        // Process payment
        customer.deductBalance(total);

        // Update product quantities
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
        }

        // Print receipt
        printReceipt(cart, subtotal, shippingFee, total, customer);

        // Clear cart
        cart.clear();
    }

    private double calculateShippingFee(ShoppingCart cart) {
        // Simple shipping calculation: 30 per shippable item
        return cart.getShippableItems().size() * 30;
    }

    private void printReceipt(ShoppingCart cart, double subtotal, double shippingFee, double total, Customer customer) {
        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s %.0f%n",
                    item.getQuantity(),
                    item.getProduct().getName(),
                    item.getTotalPrice());
        }
        System.out.printf("Subtotal %.0f%n", subtotal);
        System.out.printf("Shipping %.0f%n", shippingFee);
        System.out.printf("Amount %.0f%n", total);
        System.out.printf("Customer balance after payment: %.0f%n", customer.getBalance());
    }
}

public class ECommerceDemo {
    public static void main(String[] args) {
        Product cheese = new ExpirableShippableProduct("Cheese", 100, 10,
                new Date(System.currentTimeMillis() + 86400000 * 7), 0.4); // expires in 7 days
        Product biscuits = new ExpirableShippableProduct("Biscuits", 150, 5,
                new Date(System.currentTimeMillis() + 86400000 * 30), 0.7); // expires in 30 days
        Product tv = new ShippableProduct("TV", 1000, 3, 15.0);
        Product mobile = new NonShippableProduct("Mobile scratch card", 50, 100);

        Customer customer = new Customer("John Doe", 5000);

        ShoppingCart cart = new ShoppingCart();

        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(tv, 1);
        cart.add(mobile, 3);

        ECommerceSystem system = new ECommerceSystem();

        system.checkout(customer, cart);

        System.out.println("\n--- Testing error cases ---");

        // Test empty cart
        ShoppingCart emptyCart = new ShoppingCart();
        try {
            system.checkout(customer, emptyCart);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Test insufficient balance
        Customer poorCustomer = new Customer("Jane Doe", 100);
        ShoppingCart expensiveCart = new ShoppingCart();
        expensiveCart.add(tv, 1);
        try {
            system.checkout(poorCustomer, expensiveCart);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Test expired product
        Product expiredCheese = new ExpirableShippableProduct("Expired Cheese", 100, 5,
                new Date(System.currentTimeMillis() - 86400000), 0.4); // expired yesterday
        ShoppingCart expiredCart = new ShoppingCart();
        expiredCart.add(expiredCheese, 1);
        try {
            system.checkout(customer, expiredCart);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        ShoppingCart outOfStockCart = new ShoppingCart();
        outOfStockCart.add(biscuits, 10);
        try {
            system.checkout(customer, outOfStockCart);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}