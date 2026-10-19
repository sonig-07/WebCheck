package core;

import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

/** Runs checks one by one. One failing check never stops the others. */
public class Runner {
    private final List<Result> results = new ArrayList<>();

    public List<Result> results() { return results; }

    public void run(String module, WebDriver driver, List<Check> checks) {
        System.out.println("\n=== " + module.toUpperCase() + " (" + checks.size() + " checks) ===");
        for (Check check : checks) {
            resetBrowser(driver);
            long start = System.currentTimeMillis();
            try {
                check.body().accept(driver);
                add(new Result(module, check.name(), true, "", System.currentTimeMillis() - start, ""));
            } catch (Throwable t) {
                String shot = Screenshots.take(driver, module + "_" + check.name());
                String msg = String.valueOf(t.getMessage()).split("\n")[0];
                add(new Result(module, check.name(), false, msg, System.currentTimeMillis() - start, shot));
            }
        }
    }

    /** Clean slate before each check: no cookies, no open alert, one window, main frame. */
    private void resetBrowser(WebDriver driver) {
        try { driver.switchTo().alert().dismiss(); } catch (Exception ignored) { }
        try {
            String first = driver.getWindowHandles().iterator().next();
            for (String h : new ArrayList<>(driver.getWindowHandles())) {
                if (!h.equals(first)) { driver.switchTo().window(h); driver.close(); }
            }
            driver.switchTo().window(first);
            driver.switchTo().defaultContent();
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) { }
    }

    private void add(Result r) {
        results.add(r);
        System.out.printf("[%s] %-8s %-55s %5.1fs%n", r.passed() ? "PASS" : "FAIL", r.module(), r.name(), r.millis() / 1000.0);
        if (!r.passed()) System.out.println("         -> " + r.detail());
    }

    public void printSummary() {
        long passed = results.stream().filter(Result::passed).count();
        System.out.println("\n----------------------------------------");
        System.out.println("TOTAL: " + results.size() + "   PASSED: " + passed + "   FAILED: " + (results.size() - passed));
        System.out.println("----------------------------------------");
    }
}
