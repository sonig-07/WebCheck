# WebCheck - Automated Web QA Runner (Java + Selenium)

A console tool that opens a real browser, runs a list of automated checks on an
**online store** and on common **web-page widgets**, then prints PASS/FAIL, saves a
**screenshot for every failure** and writes an **HTML report**.

Plain Java + Selenium WebDriver. No TestNG / no framework - the runner, assertions and
report are written by hand (about 250 lines in `core/`), so every piece is easy to explain.

## What it checks
**store** (https://www.saucedemo.com) - 12 checks
valid / locked-out / wrong-password / empty login, catalogue count, price sorting,
cart badge add/remove, cart contents, full checkout, checkout validation, logout, footer link opens new tab.

**widgets** (the-internet.herokuapp.com, jqueryui.com) - 13 checks
checkboxes, dropdown, JS alerts, nested frames, child window, web table, hover, right-click,
keyboard keys, dynamic wait, date picker, file upload, JavaScript scrolling.

## Project structure
```
src/main/java/
  app/Main.java            entry point: choose all | store | widgets
  core/Check.java          a check = name + browser steps
  core/Runner.java         runs checks, isolates failures, resets browser between checks
  core/Verify.java         tiny assertion helper
  core/Browser.java        Chrome start-up (headless option) + explicit-wait helpers
  core/Screenshots.java    screenshot on failure
  core/ReportWriter.java   reports/report.html
  store/StoreChecks.java   online-store scenarios
  widgets/WidgetChecks.java  widget scenarios
reports/  screenshots/     generated at run time
```

## Run
Needs JDK 17+, Maven, Chrome (Selenium Manager downloads the driver automatically).

```powershell
mvn -q compile exec:java                                   # everything
mvn -q compile exec:java "-Dexec.args=store"               # store only
mvn -q compile exec:java "-Dexec.args=widgets"             # widgets only
mvn -q compile exec:java "-Dheadless=true"                 # no visible browser
```
(Quotes around `-D...` are needed in PowerShell.) Then open `reports/report.html`.

## Selenium concepts used (and where)
| Concept | Where |
|---|---|
| Launch / options / headless | Browser.start |
| Locators: id, name, class, tag, linkText, CSS, XPath | StoreChecks, WidgetChecks |
| WebElement methods (sendKeys, click, getText, getAttribute, isSelected) | everywhere |
| Explicit waits (visibility, clickable, alert, window count) | Browser.visible / clickable / until |
| Dropdown (Select) | price sort, dropdown check |
| Checkboxes | widgets |
| Alerts / confirm / prompt | widgets |
| Frames and nested frames | widgets, date picker |
| Windows and tabs | footer link, child window |
| Web tables | widgets |
| Actions: hover, right-click | widgets |
| Keyboard keys | widgets |
| Date picker | widgets |
| JavascriptExecutor | widgets |
| File upload | widgets |
| Screenshots | Screenshots.take (on every failure) |

## Resume / interview framing
**WebCheck - Automated Web QA Runner | Java, Selenium WebDriver, Maven**
- Built a Java/Selenium tool that runs 25 automated functional checks (login, catalogue, cart, checkout, alerts, frames, windows, tables, date picker, upload) against a demo e-commerce site and common web widgets.
- Designed a small custom runner with failure isolation, per-check browser reset, screenshot capture on failure and an HTML report - without any test framework.
- Used explicit waits throughout to remove flaky timing failures; supports headless execution.

Natural next step (to say in interviews): move the same checks to TestNG + Page Object Model + Extent Reports.
