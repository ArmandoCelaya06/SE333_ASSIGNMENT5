# Amazon Shopping Cart – SE333 Assignment 5

![CI Status](https://github.com/ArmandoCelaya06/SE333_ASSIGNMENT5/actions/workflows/SE333_CI.yml/badge.svg)

## Project Overview

This project implements a simplified Amazon-style shopping cart system in Java. It demonstrates:

- A `ShoppingCart` interface backed by an in-memory HSQLDB database via `ShoppingCartAdaptor`
- A `PriceRule` strategy pattern with three concrete rules: `RegularCost`, `DeliveryPrice`, and `ExtraCostForElectronics`
- An `Amazon` class that orchestrates cart operations and applies pricing rules
- Full unit and integration test suites
- Playwright-based UI tests targeting the DePaul University Bookstore

---

## Repository Structure

```
src/
├── main/java/org/example/Amazon/
│   ├── Amazon.java
│   ├── Database.java
│   ├── Item.java
│   ├── ShoppingCart.java
│   ├── ShoppingCartAdaptor.java
│   └── Cost/
│       ├── DeliveryPrice.java
│       ├── ExtraCostForElectronics.java
│       ├── ItemType.java
│       ├── PriceRule.java
│       └── RegularCost.java
└── test/java/
    ├── org/example/
    │   ├── AmazonUnitTest.java
    │   └── AmazonIntegrationTest.java
    └── playwrightTraditional/
        └── BookstoreTest.java
.github/workflows/SE333_CI.yml
videos/   ← Playwright test recordings saved here
```

---

## Part 1 – Tests & CI

### Unit Tests (`AmazonUnitTest.java`)
Tests individual classes in isolation using **Mockito** mocks:
- `Amazon`: `addToCart` delegation, `calculate` aggregation over multiple rules
- `DeliveryPrice`: all three delivery price tiers plus boundary conditions
- `ExtraCostForElectronics`: electronics present / absent / multiple electronics
- `RegularCost`: empty cart, single item, multiple items, quantity-zero edge case
- `Item`: getter correctness

### Integration Tests (`AmazonIntegrationTest.java`)
Tests real component interactions through a live HSQLDB in-memory database:
- Full `Amazon → ShoppingCartAdaptor → Database` pipeline
- DB is reset via `@BeforeEach` so every test starts from a clean state
- Covers all price rule combinations and boundary conditions end-to-end

### GitHub Actions Workflow (`.github/workflows/SE333_CI.yml`)
Triggers on every push to `main` and runs:
1. **Checkstyle** (validate phase) – static analysis, violations do not fail the build
2. **Maven Test + Verify** – runs all unit and integration tests
3. **JaCoCo** – generates code coverage report
4. Both `checkstyle-result.xml` and `jacoco.xml` are uploaded as workflow artifacts

---

## Part 2 – Playwright UI Testing

### Traditional Tests (`playwrightTraditional/BookstoreTest.java`)
Automates a complete purchase pathway on [https://depaul.bncollege.com/](https://depaul.bncollege.com/):

| # | Test Case | Key Assertions |
|---|-----------|----------------|
| 1 | Bookstore search & filter | Product name, SKU, price, description; cart shows 1 item |
| 2 | Shopping Cart page | Page title, product name, quantity, price; sidebar totals; promo code rejection |
| 3 | Create Account page | "Create Account" label present; proceed as guest |
| 4 | Contact Information page | Page heading; sidebar totals persist |
| 5 | Pickup Information page | Contact info, DePaul pickup location, sidebar totals, product |
| 6 | Payment Information page | Sidebar totals with calculated tax ($15.58); total $167.56 |
| 7 | Final Cart page | Delete product; cart is empty |

Test execution video is recorded automatically to the `videos/` directory.

### AI-Assisted Testing Reflection (Task 4)

**Manual UI Testing (Java + Playwright)**  
Writing tests manually in Java with Playwright gives you precise, deterministic control over every interaction. Selectors, wait strategies, and assertion logic are all explicit — you know exactly what is being tested and why. The main challenge is the initial investment: learning the Playwright API, choosing robust locators that won't break when the page changes, and handling timing issues like waiting for cart counts to update. Once the tests are written, however, they are easy to version-control, review in pull requests, and maintain as a team. Debugging is also straightforward because the full Java stack trace points directly to the failing assertion.

**AI-Assisted Testing (Playwright MCP)**  
Using the Playwright MCP agent through a natural-language prompt dramatically reduces the time needed to generate an initial test script. Describing the workflow in plain English — "search for earbuds, filter by JBL and Black, add to cart, and assert 1 item in cart" — produces runnable Java code within seconds, removing the need to look up the Playwright API or craft locators from scratch. This is especially valuable for non-trivial workflows with many steps, where manual coding would be tedious and error-prone.

However, AI-generated tests have clear limitations. The generated locators are often overly generic or fragile, relying on text matches that may shift with any UI copy change. The agent cannot observe the actual rendered DOM, so it sometimes generates selectors that do not match the real page structure. Assertions also tend to be shallow — the agent covers the happy path well but often misses edge cases like promo-code rejection messages or exact price calculations. As a result, the generated code almost always requires manual review and refinement before it can be relied upon in a CI pipeline.

In terms of maintenance, manually written tests are easier to update because the intent behind each line is self-evident. AI-generated tests can be harder to reason about after the fact, especially when the generation involved multiple prompt iterations. Overall, AI assistance is best used as a first-draft accelerator: prompt the agent to produce 80% of the boilerplate, then manually harden the locators, add precise assertions, and handle edge cases — giving you the speed benefits of AI without sacrificing reliability.

---

## How to Run

```bash
# Unit + Integration tests
mvn test

# Full build with Checkstyle + JaCoCo report
mvn verify

# Playwright codegen (record interactions)
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="codegen"
```

> **Note:** Clear your browser cache between Playwright test runs to ensure the cart and tax values reset correctly.