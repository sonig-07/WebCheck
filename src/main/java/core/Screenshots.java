package core;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Screenshots {
    private Screenshots() {}

    /** Saves screenshots/<name>_<time>.png and returns the file name, or "" if it could not be taken. */
    public static String take(WebDriver driver, String name) {
        try {
            String safe = name.replaceAll("[^A-Za-z0-9]+", "_");
            String file = safe + "_" + new SimpleDateFormat("HHmmss").format(new Date()) + ".png";
            Files.createDirectories(Path.of("screenshots"));
            Files.copy(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath(),
                    Path.of("screenshots", file), StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (Exception e) {
            return "";
        }
    }
}
