package org.example;

import org.example.Amazon.Amazon;
import org.example.Amazon.Database;
import org.example.Amazon.Item;
import org.example.Amazon.ShoppingCart;
import org.example.Amazon.ShoppingCartAdaptor;
import org.example.Amazon.Cost.DeliveryPrice;
import org.example.Amazon.Cost.ExtraCostForElectronics;
import org.example.Amazon.Cost.ItemType;
import org.example.Amazon.Cost.PriceRule;
import org.example.Amazon.Cost.RegularCost;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AmazonIntegrationTest {

    private static final Database db = new Database();
    private ShoppingCart cart;

    @BeforeEach
    void resetDb() {
        db.resetDatabase();
        cart = new ShoppingCartAdaptor(db);
    }

    @AfterAll
    static void closeDb() {
        db.close();
    }

    @Test
    @DisplayName("specification-based: addToCart persists item so getItems returns it")
    void addToCart_itemPersistedInDb() {
        Amazon amazon = new Amazon(cart, List.of());
        Item item = new Item(ItemType.ELECTRONIC, "Laptop", 1, 1000.0);

        amazon.addToCart(item);

        List<Item> items = cart.getItems();
        assertEquals(1, items.size());
        assertEquals("Laptop", items.get(0).getName());
        assertEquals(ItemType.ELECTRONIC, items.get(0).getType());
        assertEquals(1, items.get(0).getQuantity());
        assertEquals(1000.0, items.get(0).getPricePerUnit());
    }

    @Test
    @DisplayName("specification-based: adding two different items both appear in getItems")
    void addTwoItems_bothReturnedFromDb() {
        Amazon amazon = new Amazon(cart, List.of());
        amazon.addToCart(new Item(ItemType.ELECTRONIC, "Phone", 1, 500.0));
        amazon.addToCart(new Item(ItemType.OTHER, "Book", 2, 15.0));

        List<Item> items = cart.getItems();
        assertEquals(2, items.size());
    }

    @Test
    @DisplayName("specification-based: DB is fresh after reset — getItems returns empty list")
    void afterReset_cartIsEmpty() {
        List<Item> items = cart.getItems();
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("specification-based: calculate with RegularCost returns sum of item prices")
    void calculate_regularCost_returnsCorrectTotal() {
        cart.add(new Item(ItemType.OTHER, "Book", 2, 10.0));
        cart.add(new Item(ItemType.OTHER, "Pen", 5, 2.0));

        Amazon amazon = new Amazon(cart, List.of(new RegularCost()));

        assertEquals(30.0, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: calculate with DeliveryPrice adds correct shipping fee")
    void calculate_deliveryPrice_oneItem_addsFive() {
        cart.add(new Item(ItemType.OTHER, "Notebook", 1, 5.0));

        Amazon amazon = new Amazon(cart, List.of(new DeliveryPrice()));

        assertEquals(5.0, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: calculate with DeliveryPrice adds 12.5 for 4 items")
    void calculate_deliveryPrice_fourItems_addsTwelvePointFive() {
        for (int i = 0; i < 4; i++) {
            cart.add(new Item(ItemType.OTHER, "Item" + i, 1, 1.0));
        }

        Amazon amazon = new Amazon(cart, List.of(new DeliveryPrice()));

        assertEquals(12.5, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: calculate adds electronics surcharge when ELECTRONIC item present")
    void calculate_withElectronic_addsExtraCost() {
        cart.add(new Item(ItemType.ELECTRONIC, "Tablet", 1, 200.0));

        Amazon amazon = new Amazon(cart, List.of(new ExtraCostForElectronics()));

        assertEquals(7.5, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: calculate no electronics surcharge for OTHER items only")
    void calculate_noElectronic_noExtraCost() {
        cart.add(new Item(ItemType.OTHER, "Shirt", 1, 30.0));

        Amazon amazon = new Amazon(cart, List.of(new ExtraCostForElectronics()));

        assertEquals(0.0, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: full pipeline with all rules — electronic item, 1 item in cart")
    void fullPipeline_electronicItem_allRules() {
        cart.add(new Item(ItemType.ELECTRONIC, "Headphones", 1, 100.0));

        Amazon amazon = new Amazon(cart, List.of(
                new RegularCost(),
                new DeliveryPrice(),
                new ExtraCostForElectronics()
        ));

        assertEquals(112.5, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: full pipeline — no electronics, 2 items")
    void fullPipeline_noElectronic_twoItems() {
        cart.add(new Item(ItemType.OTHER, "Book", 1, 15.0));
        cart.add(new Item(ItemType.OTHER, "Pen", 3, 2.0));

        Amazon amazon = new Amazon(cart, List.of(
                new RegularCost(),
                new DeliveryPrice(),
                new ExtraCostForElectronics()
        ));

        assertEquals(26.0, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: full pipeline — empty cart returns 0")
    void fullPipeline_emptyCart_returnsZero() {
        Amazon amazon = new Amazon(cart, List.of(
                new RegularCost(),
                new DeliveryPrice(),
                new ExtraCostForElectronics()
        ));

        assertEquals(0.0, amazon.calculate());
    }

    @Test
    @DisplayName("structural-based: DeliveryPrice boundary — 3 items = 5.0, 4 items = 12.5")
    void deliveryPrice_boundaryTest_3vs4Items() {
        for (int i = 0; i < 3; i++) cart.add(new Item(ItemType.OTHER, "Item" + i, 1, 1.0));
        Amazon amazon3 = new Amazon(cart, List.of(new DeliveryPrice()));
        assertEquals(5.0, amazon3.calculate());

        db.resetDatabase();
        cart = new ShoppingCartAdaptor(db);
        for (int i = 0; i < 4; i++) cart.add(new Item(ItemType.OTHER, "Item" + i, 1, 1.0));
        Amazon amazon4 = new Amazon(cart, List.of(new DeliveryPrice()));
        assertEquals(12.5, amazon4.calculate());
    }

    @Test
    @DisplayName("structural-based: DeliveryPrice boundary — 10 items = 12.5, 11 items = 20.0")
    void deliveryPrice_boundaryTest_10vs11Items() {
        for (int i = 0; i < 10; i++) cart.add(new Item(ItemType.OTHER, "Item" + i, 1, 1.0));
        Amazon amazon10 = new Amazon(cart, List.of(new DeliveryPrice()));
        assertEquals(12.5, amazon10.calculate());

        db.resetDatabase();
        cart = new ShoppingCartAdaptor(db);
        for (int i = 0; i < 11; i++) cart.add(new Item(ItemType.OTHER, "Item" + i, 1, 1.0));
        Amazon amazon11 = new Amazon(cart, List.of(new DeliveryPrice()));
        assertEquals(20.0, amazon11.calculate());
    }

    @Test
    @DisplayName("structural-based: RegularCost loops over all persisted items correctly")
    void regularCost_loopsAllPersistedItems() {
        cart.add(new Item(ItemType.OTHER, "A", 2, 10.0)); 
        cart.add(new Item(ItemType.OTHER, "B", 3, 5.0));   
        cart.add(new Item(ItemType.OTHER, "C", 1, 100.0)); 

        Amazon amazon = new Amazon(cart, List.of(new RegularCost()));

        assertEquals(135.0, amazon.calculate());
    }

    @Test
    @DisplayName("structural-based: ExtraCostForElectronics only charges once with multiple electronics")
    void extraCost_multipleElectronics_chargedOnce() {
        cart.add(new Item(ItemType.ELECTRONIC, "Laptop", 1, 800.0));
        cart.add(new Item(ItemType.ELECTRONIC, "Phone", 1, 600.0));

        Amazon amazon = new Amazon(cart, List.of(new ExtraCostForElectronics()));

        assertEquals(7.5, amazon.calculate());
    }

    @Test
    @DisplayName("structural-based: calculate with empty rules list returns 0 regardless of cart contents")
    void calculate_emptyRules_returnsZero() {
        cart.add(new Item(ItemType.ELECTRONIC, "Laptop", 1, 1000.0));
        Amazon amazon = new Amazon(cart, List.of());

        assertEquals(0.0, amazon.calculate());
    }

    @Test
    @DisplayName("structural-based: item data survives round-trip through DB correctly")
    void item_roundTripThroughDb_preservesAllFields() {
        Item original = new Item(ItemType.ELECTRONIC, "Camera", 3, 299.99);
        cart.add(original);

        Item retrieved = cart.getItems().get(0);

        assertEquals(original.getName(), retrieved.getName());
        assertEquals(original.getType(), retrieved.getType());
        assertEquals(original.getQuantity(), retrieved.getQuantity());
        assertEquals(original.getPricePerUnit(), retrieved.getPricePerUnit(), 0.001);
    }
}