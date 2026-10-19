package app;

import core.*;
import org.openqa.selenium.WebDriver;
import store.StoreChecks;
import widgets.WidgetChecks;

/**
 * WebCheck - console QA runner.
 *   (no argument) or "all"  -> store + widgets
 *   store                   -> online-store checks only
 *   widgets                 -> web-widget checks only
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String which = args.length > 0 ? args[0].toLowerCase() : "all";
        boolean runStore = which.equals("all") || which.equals("store");
        boolean runWidgets = which.equals("all") || which.equals("widgets");
        if (!runStore && !runWidgets) {
            System.out.println("Usage: [all | store | widgets]");
            return;
        }

        WebDriver driver = Browser.start();
        Runner runner = new Runner();
        try {
            if (runStore) runner.run("store", driver, StoreChecks.all());
            if (runWidgets) runner.run("widgets", driver, WidgetChecks.all());
        } finally {
            driver.quit();
        }
        runner.printSummary();
        ReportWriter.write(runner.results());
    }
}
