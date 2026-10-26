package dev.jstus.snitch.report;

import java.util.List;

public record OpenCaseSummary(ReportCase reportCase, int reporters, List<String> reasons) {
}
