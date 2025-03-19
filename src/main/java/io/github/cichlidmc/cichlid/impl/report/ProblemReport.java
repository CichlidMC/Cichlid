package io.github.cichlidmc.cichlid.impl.report;

import io.github.cichlidmc.cichlid.impl.util.Utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ProblemReport {
	private final List<ReportSection> sections;

	public ProblemReport() {
		this(new ArrayList<>());
	}

	public ProblemReport(List<ReportSection> sections) {
		this.sections = sections;
	}

	public void addSection(ReportSection section) {
		this.sections.add(section);
	}

	public void addSection(String header, List<ReportDetail> details) {
		this.addSection(new ReportSection(header, details));
	}

	public void addSection(String header, ReportDetail... details) {
		this.addSection(new ReportSection(header, details));
	}

	public void addSection(String header, Path file) {
		this.addSection(header, new ReportDetail("Location", file.toString()));
	}

	public Collection<ReportSection> sections() {
		return this.sections;
	}

	public boolean isEmpty() {
		return this.sections.isEmpty();
	}

	public void throwIfNotEmpty() throws ReportedException {
		if (!this.isEmpty()) {
			throw this.createException();
		}
	}

	public ReportedException createException() {
		return new ReportedException(this);
	}

	public static ProblemReport of(Throwable t) {
		if (t instanceof ReportedException) {
			return ((ReportedException) t).report;
		}

		ProblemReport report = new ProblemReport();
		report.addSection("Unhandled Error", new ReportDetail("Stacktrace", Utils.getStackTrace(t), true));
		return report;
	}
}
