package io.github.cichlidmc.cichlid.impl.report;

public final class ReportDetail {
	public final String key;
	public final String message;
	public final boolean bypassLineLimit;

	public ReportDetail(String key, String message) {
		this(key, message, false);
	}

	public ReportDetail(String key, String message, boolean bypassLineLimit) {
		this.key = key;
		this.message = message;
		this.bypassLineLimit = bypassLineLimit;
	}
}
