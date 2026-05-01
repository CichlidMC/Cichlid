package fish.cichlidmc.cichlid.impl.report;

public record ReportDetail(String key, String message, boolean bypassLineLimit) {
	public ReportDetail(String key, String message) {
		this(key, message, false);
	}
}
