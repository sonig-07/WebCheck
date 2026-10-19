package widgets;

import core.Browser;
import core.Check;
import core.Verify;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Checks of common web-page widgets (the-internet.herokuapp.com + jqueryui.com). */
public final class WidgetChecks {
    private static final String BASE = "https://the-internet.herokuapp.com";
    private WidgetChecks() {}

    public static List<Check> all() {
        return List.of(

            new Check("Checkboxes can be ticked and unticked", d -> {
                d.get(BASE + "/checkboxes");
                List<WebElement> boxes = d.findElements(By.cssSelector("input[type='checkbox']"));
                for (WebElement b : boxes) if (!b.isSelected()) b.click();
                for (WebElement b : boxes) Verify.isTrue(b.isSelected(), "Box should be ticked");
                for (WebElement b : boxes) if (b.isSelected()) b.click();
                for (WebElement b : boxes) Verify.isTrue(!b.isSelected(), "Box should be unticked");
            }),

            new Check("Dropdown selects by index, text and value", d -> {
                d.get(BASE + "/dropdown");
                Select dd = new Select(Browser.visible(d, By.id("dropdown")));
                dd.selectByIndex(1);
                Verify.equal("Option 1", dd.getFirstSelectedOption().getText(), "By index");
                dd.selectByVisibleText("Option 2");
                Verify.equal("Option 2", dd.getFirstSelectedOption().getText(), "By text");
                dd.selectByValue("1");
                Verify.equal("Option 1", dd.getFirstSelectedOption().getText(), "By value");
            }),

            new Check("JS alert, confirm and prompt are handled", d -> {
                d.get(BASE + "/javascript_alerts");
                Browser.clickable(d, By.xpath("//button[text()='Click for JS Alert']")).click();
                Browser.until(d, ExpectedConditions.alertIsPresent()).accept();
                Verify.contains(d.findElement(By.id("result")).getText(), "successfully clicked an alert", "Alert");

                d.findElement(By.xpath("//button[text()='Click for JS Confirm']")).click();
                d.switchTo().alert().dismiss();
                Verify.contains(d.findElement(By.id("result")).getText(), "Cancel", "Confirm");

                d.findElement(By.xpath("//button[text()='Click for JS Prompt']")).click();
                Alert prompt = d.switchTo().alert();
                prompt.sendKeys("WebCheck");
                prompt.accept();
                Verify.contains(d.findElement(By.id("result")).getText(), "WebCheck", "Prompt");
            }),

            new Check("Nested frames: text read from the middle frame", d -> {
                d.get(BASE + "/nested_frames");
                d.switchTo().frame("frame-top");
                d.switchTo().frame("frame-middle");
                Verify.equal("MIDDLE", Browser.visible(d, By.id("content")).getText(), "Middle frame text");
                d.switchTo().defaultContent();
            }),

            new Check("Child window opens, is read and closed", d -> {
                d.get(BASE + "/windows");
                String parent = d.getWindowHandle();
                d.findElement(By.linkText("Click Here")).click();
                Browser.until(d, ExpectedConditions.numberOfWindowsToBe(2));
                for (String h : d.getWindowHandles()) {
                    if (!h.equals(parent)) {
                        d.switchTo().window(h);
                        Verify.equal("New Window", Browser.visible(d, By.tagName("h3")).getText(), "Child heading");
                        d.close();
                    }
                }
                d.switchTo().window(parent);
                Verify.equal("Opening a new window", d.findElement(By.tagName("h3")).getText(), "Parent heading");
            }),

            new Check("Web table: row count and lookup by name", d -> {
                d.get(BASE + "/tables");
                List<WebElement> rows = d.findElements(By.xpath("//table[@id='table1']/tbody/tr"));
                Verify.equal(4, rows.size(), "Row count");
                String due = null;
                for (int r = 1; r <= rows.size(); r++) {
                    String first = d.findElement(By.xpath("//table[@id='table1']/tbody/tr[" + r + "]/td[2]")).getText();
                    if (first.equals("Jason")) {
                        due = d.findElement(By.xpath("//table[@id='table1']/tbody/tr[" + r + "]/td[4]")).getText();
                    }
                }
                Verify.isTrue(due != null && due.startsWith("$"), "Jason's due amount should be found and start with $");
            }),

            new Check("Mouse hover reveals the user caption", d -> {
                d.get(BASE + "/hovers");
                WebElement figure = Browser.visible(d, By.cssSelector(".figure:nth-of-type(1)"));
                new Actions(d).moveToElement(figure).perform();
                Verify.equal("name: user1",
                        Browser.visible(d, By.cssSelector(".figure:nth-of-type(1) h5")).getText(), "Hover caption");
            }),

            new Check("Right-click opens the context-menu alert", d -> {
                d.get(BASE + "/context_menu");
                new Actions(d).contextClick(Browser.visible(d, By.id("hot-spot"))).perform();
                Alert alert = Browser.until(d, ExpectedConditions.alertIsPresent());
                Verify.equal("You selected a context menu", alert.getText(), "Alert text");
                alert.accept();
            }),

            new Check("Keyboard key press is detected", d -> {
                d.get(BASE + "/key_presses");
                Browser.visible(d, By.id("target")).sendKeys(Keys.ENTER);
                Verify.equal("You entered: ENTER", d.findElement(By.id("result")).getText(), "Key result");
            }),

            new Check("Content appears after a dynamic wait", d -> {
                d.get(BASE + "/dynamic_loading/1");
                Browser.clickable(d, By.cssSelector("#start button")).click();
                Verify.equal("Hello World!", Browser.visible(d, By.id("finish")).getText(), "Loaded text");
            }),

            new Check("Date picker selects a future date", d -> {
                d.get("https://jqueryui.com/datepicker/");
                d.switchTo().frame(Browser.visible(d, By.className("demo-frame")));
                Browser.clickable(d, By.id("datepicker")).click();
                int guard = 0;
                while (!(d.findElement(By.className("ui-datepicker-month")).getText().equals("December")
                        && d.findElement(By.className("ui-datepicker-year")).getText().equals("2026"))) {
                    d.findElement(By.className("ui-datepicker-next")).click();
                    Verify.isTrue(++guard < 60, "Target month not reached");
                }
                d.findElement(By.xpath("//table[@class='ui-datepicker-calendar']//a[text()='25']")).click();
                Verify.equal("12/25/2026", d.findElement(By.id("datepicker")).getAttribute("value"), "Chosen date");
                d.switchTo().defaultContent();
            }),

            new Check("File upload is accepted", d -> {
                try {
                    Path file = Files.createTempFile("webcheck-", ".txt");
                    Files.writeString(file, "uploaded by WebCheck");
                    d.get(BASE + "/upload");
                    d.findElement(By.id("file-upload")).sendKeys(file.toAbsolutePath().toString());
                    d.findElement(By.id("file-submit")).click();
                    Verify.equal(file.getFileName().toString(),
                            Browser.visible(d, By.id("uploaded-files")).getText(), "Uploaded file name");
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            }),

            new Check("JavaScript scroll reaches the page footer", d -> {
                d.get(BASE + "/large");
                JavascriptExecutor js = (JavascriptExecutor) d;
                WebElement footer = Browser.visible(d, By.id("page-footer"));
                js.executeScript("arguments[0].scrollIntoView(true);", footer);
                Number scrolled = (Number) js.executeScript("return window.scrollY;");
                Verify.isTrue(scrolled.doubleValue() > 0, "Page should have scrolled down");
            })
        );
    }
}
