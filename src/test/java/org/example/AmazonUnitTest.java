package org.example;

import org.example.Amazon.Amazon;
import org.example.Amazon.Item;
import org.example.Amazon.ShoppingCart;
import org.example.Amazon.Cost.DeliveryPrice;
import org.example.Amazon.Cost.ExtraCostForElectronics;
import org.example.Amazon.Cost.ItemType;
import org.example.Amazon.Cost.PriceRule;
import org.example.Amazon.Cost.RegularCost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AmazonUnitTest {

    private ShoppingCart mockCart;

    @BeforeEach
    void setUp() {
        mockCart = mock(ShoppingCart.class);
    }

    // ─────────────────────────────────────────────────────────────────
    // Amazon – specification-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("specification-based: addToCart delegates to ShoppingCart.add")
    void addToCart_delegatesToCart() {
        Amazon amazon = new Amazon(mockCart, List.of());
        Item item = mock(Item.class);

        amazon.addToCart(item);

        verify(mockCart, times(1)).add(item);
    }

    @Test
    @DisplayName("specification-based: calculate returns 0 when no rules and empty cart")
    void calculate_noRules_returnsZero() {
        when(mockCart.getItems()).thenReturn(List.of());
        Amazon amazon = new Amazon(mockCart, List.of());

        assertEquals(0.0, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: calculate sums results from multiple rules")
    void calculate_multipleRules_sumsAll() {
        Item item = mock(Item.class);
        when(mockCart.getItems()).thenReturn(List.of(item));

        PriceRule rule1 = mock(PriceRule.class);
        PriceRule rule2 = mock(PriceRule.class);
        when(rule1.priceToAggregate(anyList())).thenReturn(30.0);
        when(rule2.priceToAggregate(anyList())).thenReturn(20.0);

        Amazon amazon = new Amazon(mockCart, List.of(rule1, rule2));

        assertEquals(50.0, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based: calculate calls each rule exactly once")
    void calculate_callsEachRuleOnce() {
        when(mockCart.getItems()).thenReturn(List.of());

        PriceRule rule1 = mock(PriceRule.class);
        PriceRule rule2 = mock(PriceRule.class);
        when(rule1.priceToAggregate(anyList())).thenReturn(0.0);
        when(rule2.priceToAggregate(anyList())).thenReturn(0.0);

        Amazon amazon = new Amazon(mockCart, List.of(rule1, rule2));
        amazon.calculate();

        verify(rule1, times(1)).priceToAggregate(anyList());
        verify(rule2, times(1)).priceToAggregate(anyList());
    }

    @Test
    @DisplayName("specification-based: calculate passes cart items to each rule")
    void calculate_passesCartItemsToRules() {
        Item item = mock(Item.class);
        List<Item> items = List.of(item);
        when(mockCart.getItems()).thenReturn(items);

        PriceRule rule = mock(PriceRule.class);
        when(rule.priceToAggregate(items)).thenReturn(99.0);

        Amazon amazon = new Amazon(mockCart, List.of(rule));
        amazon.calculate();

        verify(rule).priceToAggregate(items);
    }

    // ─────────────────────────────────────────────────────────────────
    // Amazon – structural-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("structural-based: calculate loops over all rules in order")
    void calculate_loopsAllRules() {
        when(mockCart.getItems()).thenReturn(List.of());

        PriceRule ruleA = mock(PriceRule.class);
        PriceRule ruleB = mock(PriceRule.class);
        PriceRule ruleC = mock(PriceRule.class);
        when(ruleA.priceToAggregate(anyList())).thenReturn(1.0);
        when(ruleB.priceToAggregate(anyList())).thenReturn(2.0);
        when(ruleC.priceToAggregate(anyList())).thenReturn(3.0);

        Amazon amazon = new Amazon(mockCart, List.of(ruleA, ruleB, ruleC));

        assertEquals(6.0, amazon.calculate());
        verify(ruleA).priceToAggregate(anyList());
        verify(ruleB).priceToAggregate(anyList());
        verify(ruleC).priceToAggregate(anyList());
    }

    @Test
    @DisplayName("structural-based: calculate with single rule returns that rule's value")
    void calculate_singleRule_returnsThatRulesValue() {
        when(mockCart.getItems()).thenReturn(List.of());

        PriceRule rule = mock(PriceRule.class);
        when(rule.priceToAggregate(anyList())).thenReturn(42.0);

        Amazon amazon = new Amazon(mockCart, List.of(rule));

        assertEquals(42.0, amazon.calculate());
    }

    // ─────────────────────────────────────────────────────────────────
    // DeliveryPrice – specification-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("specification-based: DeliveryPrice returns 0 for empty cart")
    void deliveryPrice_emptyCart_returnsZero() {
        DeliveryPrice rule = new DeliveryPrice();
        assertEquals(0.0, rule.priceToAggregate(List.of()));
    }

    @Test
    @DisplayName("specification-based: DeliveryPrice returns 5 for 1 item")
    void deliveryPrice_oneItem_returnsFive() {
        DeliveryPrice rule = new DeliveryPrice();
        Item item = mock(Item.class);
        assertEquals(5.0, rule.priceToAggregate(List.of(item)));
    }

    @Test
    @DisplayName("specification-based: DeliveryPrice returns 5 for 3 items")
    void deliveryPrice_threeItems_returnsFive() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> items = List.of(mock(Item.class), mock(Item.class), mock(Item.class));
        assertEquals(5.0, rule.priceToAggregate(items));
    }

    @Test
    @DisplayName("specification-based: DeliveryPrice returns 12.5 for 4 items")
    void deliveryPrice_fourItems_returnsTwelvePointFive() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 4; i++) items.add(mock(Item.class));
        assertEquals(12.5, rule.priceToAggregate(items));
    }

    @Test
    @DisplayName("specification-based: DeliveryPrice returns 12.5 for 10 items")
    void deliveryPrice_tenItems_returnsTwelvePointFive() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) items.add(mock(Item.class));
        assertEquals(12.5, rule.priceToAggregate(items));
    }

    @Test
    @DisplayName("specification-based: DeliveryPrice returns 20 for 11 items")
    void deliveryPrice_elevenItems_returnsTwenty() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 11; i++) items.add(mock(Item.class));
        assertEquals(20.0, rule.priceToAggregate(items));
    }

    // ─────────────────────────────────────────────────────────────────
    // DeliveryPrice – structural-based (boundary)
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("structural-based: DeliveryPrice boundary at 3->4 items")
    void deliveryPrice_boundary3to4() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> three = new ArrayList<>();
        List<Item> four = new ArrayList<>();
        for (int i = 0; i < 3; i++) three.add(mock(Item.class));
        for (int i = 0; i < 4; i++) four.add(mock(Item.class));
        assertEquals(5.0, rule.priceToAggregate(three));
        assertEquals(12.5, rule.priceToAggregate(four));
    }

    @Test
    @DisplayName("structural-based: DeliveryPrice boundary at 10->11 items")
    void deliveryPrice_boundary10to11() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> ten = new ArrayList<>();
        List<Item> eleven = new ArrayList<>();
        for (int i = 0; i < 10; i++) ten.add(mock(Item.class));
        for (int i = 0; i < 11; i++) eleven.add(mock(Item.class));
        assertEquals(12.5, rule.priceToAggregate(ten));
        assertEquals(20.0, rule.priceToAggregate(eleven));
    }

    // ─────────────────────────────────────────────────────────────────
    // ExtraCostForElectronics – specification-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("specification-based: ExtraCost returns 7.5 when cart has an ELECTRONIC item")
    void extraCost_hasElectronic_returnsSeven50() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        Item electronic = mock(Item.class);
        when(electronic.getType()).thenReturn(ItemType.ELECTRONIC);

        assertEquals(7.5, rule.priceToAggregate(List.of(electronic)));
    }

    @Test
    @DisplayName("specification-based: ExtraCost returns 0 when cart has no ELECTRONIC item")
    void extraCost_noElectronic_returnsZero() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        Item other = mock(Item.class);
        when(other.getType()).thenReturn(ItemType.OTHER);

        assertEquals(0.0, rule.priceToAggregate(List.of(other)));
    }

    @Test
    @DisplayName("specification-based: ExtraCost returns 0 for empty cart")
    void extraCost_emptyCart_returnsZero() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        assertEquals(0.0, rule.priceToAggregate(List.of()));
    }

    // ─────────────────────────────────────────────────────────────────
    // ExtraCostForElectronics – structural-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("structural-based: ExtraCost returns 7.5 even with mix of ELECTRONIC and OTHER")
    void extraCost_mixedCart_returnsSeven50() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        Item electronic = mock(Item.class);
        Item other = mock(Item.class);
        when(electronic.getType()).thenReturn(ItemType.ELECTRONIC);
        when(other.getType()).thenReturn(ItemType.OTHER);

        assertEquals(7.5, rule.priceToAggregate(List.of(other, electronic)));
    }

    @Test
    @DisplayName("structural-based: ExtraCost returns 7.5 once regardless of how many electronics")
    void extraCost_multipleElectronics_stillSeven50() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        Item e1 = mock(Item.class);
        Item e2 = mock(Item.class);
        when(e1.getType()).thenReturn(ItemType.ELECTRONIC);
        when(e2.getType()).thenReturn(ItemType.ELECTRONIC);

        assertEquals(7.5, rule.priceToAggregate(List.of(e1, e2)));
    }

    // ─────────────────────────────────────────────────────────────────
    // RegularCost – specification-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("specification-based: RegularCost returns 0 for empty cart")
    void regularCost_emptyCart_returnsZero() {
        RegularCost rule = new RegularCost();
        assertEquals(0.0, rule.priceToAggregate(List.of()));
    }

    @Test
    @DisplayName("specification-based: RegularCost multiplies pricePerUnit by quantity")
    void regularCost_singleItem_returnsCorrect() {
        RegularCost rule = new RegularCost();
        Item item = mock(Item.class);
        when(item.getPricePerUnit()).thenReturn(25.0);
        when(item.getQuantity()).thenReturn(3);

        assertEquals(75.0, rule.priceToAggregate(List.of(item)));
    }

    @Test
    @DisplayName("specification-based: RegularCost sums multiple items correctly")
    void regularCost_multipleItems_sumsAll() {
        RegularCost rule = new RegularCost();
        Item item1 = mock(Item.class);
        Item item2 = mock(Item.class);
        when(item1.getPricePerUnit()).thenReturn(10.0);
        when(item1.getQuantity()).thenReturn(2);
        when(item2.getPricePerUnit()).thenReturn(5.0);
        when(item2.getQuantity()).thenReturn(4);

        // 10*2 + 5*4 = 20 + 20 = 40
        assertEquals(40.0, rule.priceToAggregate(List.of(item1, item2)));
    }

    // ─────────────────────────────────────────────────────────────────
    // RegularCost – structural-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("structural-based: RegularCost accumulates in loop across all items")
    void regularCost_loopsAllItems() {
        RegularCost rule = new RegularCost();
        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Item item = mock(Item.class);
            when(item.getPricePerUnit()).thenReturn(10.0);
            when(item.getQuantity()).thenReturn(1);
            items.add(item);
        }
        // 5 items * 10.0 = 50.0
        assertEquals(50.0, rule.priceToAggregate(items));
    }

    @Test
    @DisplayName("structural-based: RegularCost with quantity 0 contributes nothing")
    void regularCost_quantityZero_contributesNothing() {
        RegularCost rule = new RegularCost();
        Item item = mock(Item.class);
        when(item.getPricePerUnit()).thenReturn(100.0);
        when(item.getQuantity()).thenReturn(0);

        assertEquals(0.0, rule.priceToAggregate(List.of(item)));
    }

    // ─────────────────────────────────────────────────────────────────
    // Item – specification-based
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("specification-based: Item getters return correct values")
    void item_getters_returnCorrectValues() {
        Item item = new Item(ItemType.ELECTRONIC, "Laptop", 2, 999.99);

        assertEquals(ItemType.ELECTRONIC, item.getType());
        assertEquals("Laptop", item.getName());
        assertEquals(2, item.getQuantity());
        assertEquals(999.99, item.getPricePerUnit());
    }

    @Test
    @DisplayName("specification-based: Item with OTHER type returns OTHER from getType")
    void item_otherType_returnsOther() {
        Item item = new Item(ItemType.OTHER, "Book", 1, 12.0);
        assertEquals(ItemType.OTHER, item.getType());
    }
}