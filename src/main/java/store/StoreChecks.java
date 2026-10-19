package store;

import core.Browser;
import core.Check;
import core.Verify;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

/** End-to-end checks of an online store (https://www.saucedemo.com). */
public final class StoreChecks {
    private static final String URL = "https://www.saucedemo.com";
    private static final String BACKPACK = "add-to-cart-sauce-labs-backpack";
    private static final String TSHIRT = "add-to-cart-sauce-labs-bolt-t-shirt";

    private StoreChecks() {}

    // ----- small helpers (keep each check short) -----
    private static void login(WebDriver d, String user, String pass) {
        d.get(URL);
        Browser.visible(d, By.id("user-name")).sendKeys(user);
        d.findElement(By.name("password")).sendKeys(pass);
        d.findElement(By.cssSelector("input#login-button")).click();
    }

    private static void loginAsValidUser(WebDriver d) {
        login(d, "standard_user", "secret_sauce");
        Browser.visible(d, By.className("inventory_list"));
    }

    private static String error(WebDriver d) {
        return Browser.visible(d, By.cssSelector("h3[data-test='error']")).getText();
    }

    public static List<Check> all() {
        return List.of(

            new Check("Valid login opens the Products page", d -> {
                login(d, "standard_user", "secret_sauce");
                Verify.equal("Products", Browser.visible(d, By.className("title")).getText(), "Page heading");
                Verify.contains(d.getCurrentUrl(), "inventory", "URL after login");
            }),

            new Check("Locked-out user sees an error", d -> {
                login(d, "locked_out_user", "secret_sauce");
                Verify.contains(error(d), "locked out", "Error message");
            }),

            new Check("Wrong password is rejected", d -> {
                login(d, "standard_user", "wrong_password");
                Verify.contains(error(d), "do not match", "Error message");
            }),

            new Check("Empty login shows 'Username is required'", d -> {
                d.get(URL);
                Browser.clickable(d, By.id("login-button")).click();
                Verify.contains(error(d), "Username is required", "Error message");
            }),

            new Check("Catalogue lists 6 products", d -> {
                loginAsValidUser(d);
                Verify.equal(6, d.findElements(By.className("inventory_item")).size(), "Product count");
            }),

            new Check("Sort by price (low to high) is correct", d -> {
                loginAsValidUser(d);
                new Select(d.findElement(By.className("product_sort_container"))).selectByValue("lohi");
                List<Double> prices = new ArrayList<>();
                for (WebElement p : d.findElements(By.className("inventory_item_price"))) {
                    prices.add(Double.parseDouble(p.getText().replace("$", "")));
                }
                for (int i = 1; i < prices.size(); i++) {
                    Verify.isTrue(prices.get(i) >= prices.get(i - 1), "Prices not ascending: " + prices);
                }
            }),

            new Check("Cart badge follows add / remove", d -> {
                loginAsValidUser(d);
                d.findElement(By.id(BACKPACK)).click();
                d.findElement(By.id(TSHIRT)).click();
                Verify.equal("2", Browser.visible(d, By.className("shopping_cart_badge")).getText(), "Badge after 2 adds");
                d.findElement(By.id("remove-sauce-labs-backpack")).click();
                Verify.equal("1", d.findElement(By.className("shopping_cart_badge")).getText(), "Badge after remove");
            }),

            new Check("Cart page shows the chosen product", d -> {
                loginAsValidUser(d);
                d.findElement(By.id(BACKPACK)).click();
                d.findElement(By.className("shopping_cart_link")).click();
                Verify.equal(1, d.findElements(By.className("cart_item")).size(), "Items in cart");
                Verify.equal("Sauce Labs Backpack",
                        Browser.visible(d, By.className("inventory_item_name")).getText(), "Product in cart");
            }),

            new Check("Full checkout completes the order", d -> {
                loginAsValidUser(d);
                d.findElement(By.id(BACKPACK)).click();
                d.findElement(By.className("shopping_cart_link")).click();
                Browser.clickable(d, By.id("checkout")).click();
                Browser.visible(d, By.id("first-name")).sendKeys("Soni");
                d.findElement(By.id("last-name")).sendKeys("Gupta");
                d.findElement(By.id("postal-code")).sendKeys("400001");
                d.findElement(By.id("continue")).click();
                Browser.clickable(d, By.id("finish")).click();
                Verify.equal("Thank you for your order!",
                        Browser.visible(d, By.className("complete-header")).getText(), "Confirmation");
            }),

            new Check("Checkout needs customer details", d -> {
                loginAsValidUser(d);
                d.findElement(By.className("shopping_cart_link")).click();
                Browser.clickable(d, By.id("checkout")).click();
                Browser.clickable(d, By.id("continue")).click();
                Verify.contains(error(d), "First Name is required", "Error message");
            }),

            new Check("Logout returns to the login page", d -> {
                loginAsValidUser(d);
                d.findElement(By.id("react-burger-menu-btn")).click();
                Browser.clickable(d, By.linkText("Logout")).click();
                Verify.isTrue(Browser.visible(d, By.id("login-button")).isDisplayed(), "Login button visible");
            }),

            new Check("Footer social link opens a new tab", d -> {
                loginAsValidUser(d);
                String parent = d.getWindowHandle();
                d.findElement(By.cssSelector(".social_twitter a")).click();
                Browser.until(d, ExpectedConditions.numberOfWindowsToBe(2));
                Verify.equal(2, d.getWindowHandles().size(), "Open windows");
                for (String handle : d.getWindowHandles()) {          // close the new tab, return to parent
                    if (!handle.equals(parent)) { d.switchTo().window(handle); d.close(); }
                }
                d.switchTo().window(parent);
                Verify.contains(d.getCurrentUrl(), "inventory", "Back on the store page");
            })
        );
    }
}
