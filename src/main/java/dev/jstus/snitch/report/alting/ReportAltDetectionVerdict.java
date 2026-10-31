package dev.jstus.snitch.report.alting;

/**
 * The verdict, in this case, represents if a report should be allowed or denied,
 * based on what the alt detection service found.
 */
public enum ReportAltDetectionVerdict {
    ALLOW,
    DENY,
    FAILURE
}
