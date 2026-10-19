package core;

public record Result(String module, String name, boolean passed, String detail, long millis, String screenshot) { }
