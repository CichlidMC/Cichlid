package io.github.cichlidmc.cichlid.impl.report;

public final class ReportedException extends RuntimeException {
	public final ProblemReport report;

	public ReportedException(ProblemReport report) {
		this.report = report;
	}
}
