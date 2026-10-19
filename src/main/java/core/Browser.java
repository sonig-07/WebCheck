package core;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Browser start-up and explicit-wait shortcuts used by every check. */
public final class Browser {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private Browser() {}

    /** Headless with:  "-Dheadless=true" */
    public static WebDriver start() {
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);   // hide CDP warnings
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications", "--window-size=1920,1080");
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        // stop Chrome's "change your password" popup on the demo store
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        WebDriver driver = new ChromeDriver(options);
        if (!Boolean.getBoolean("headless")) driver.manage().window().maximize();
        return driver;                      // no implicit wait: we only use explicit waits
    }

    public static WebElement visible(WebDriver d, By by) {
        return new WebDriverWait(d, TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement clickable(WebDriver d, By by) {
        return new WebDriverWait(d, TIMEOUT).until(ExpectedConditions.elementToBeClickable(by));
    }

    public static <T> T until(WebDriver d, java.util.function.Function<WebDriver, T> condition) {
        return new WebDriverWait(d, TIMEOUT).until(condition);
    }
}
