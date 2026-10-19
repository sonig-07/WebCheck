package core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Writes a simple self-contained HTML report to reports/report.html (no libraries). */
public final class ReportWriter {
    private ReportWriter() {}

    public static void write(List<Result> results) throws Exception {
        long passed = results.stream().filter(Result::passed).count();
        StringBuilder rows = new StringBuilder();
        for (Result r : results) {
            rows.append("<tr class='").append(r.passed() ? "pass" : "fail").append("'>")
                .append("<td>").append(r.module()).append("</td>")
                .append("<td>").append(esc(r.name())).append("</td>")
                .append("<td>").append(r.passed() ? "PASS" : "FAIL").append("</td>")
                .append("<td>").append(String.format("%.1f s", r.millis() / 1000.0)).append("</td>")
                .append("<td>").append(esc(r.detail()));
            if (!r.screenshot().isEmpty()) {
                rows.append(" <a href='../screenshots/").append(r.screenshot()).append("'>screenshot</a>");
            }
            rows.append("</td></tr>\n");
        }
        String html = """
            <!DOCTYPE html><html><head><meta charset="utf-8"><title>WebCheck Report</title>
            <style>
              body{font-family:Segoe UI,Arial,sans-serif;margin:30px;color:#1a1a1a}
              h1{margin-bottom:4px} .sum{font-size:18px;margin:10px 0 20px}
              table{border-collapse:collapse;width:100%%} th,td{border:1px solid #999;padding:8px;text-align:left}
              th{background:#1f3a5f;color:#fff} tr.pass td:nth-child(3){color:#0a7d2c;font-weight:bold}
              tr.fail{background:#fde8e8} tr.fail td:nth-child(3){color:#b00020;font-weight:bold}
            </style></head><body>
            <h1>WebCheck - Automation Report</h1>
            <div>%s</div>
            <div class="sum">Total: %d &nbsp; Passed: %d &nbsp; Failed: %d</div>
            <table><tr><th>Module</th><th>Check</th><th>Status</th><th>Time</th><th>Details</th></tr>
            %s</table></body></html>
            """.formatted(new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date()),
                results.size(), passed, results.size() - passed, rows);
        Files.createDirectories(Path.of("reports"));
        Files.writeString(Path.of("reports/report.html"), html);
        System.out.println("Report  : reports/report.html");
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
