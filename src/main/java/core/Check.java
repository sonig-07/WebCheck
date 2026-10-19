package core;

import org.openqa.selenium.WebDriver;

import java.util.function.Consumer;

/** One automated check: a name + the browser steps. If the body throws, the check FAILS. */
public record Check(String name, Consumer<WebDriver> body) { }
