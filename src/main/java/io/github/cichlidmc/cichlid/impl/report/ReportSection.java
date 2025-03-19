package io.github.cichlidmc.cichlid.impl.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ReportSection {
	public final String header;
	// mutable
	public final List<ReportDetail> details;

	public ReportSection(String header) {
		this(header, Collections.emptyList());
	}

	public ReportSection(String header, ReportDetail... details) {
		this(header, Arrays.asList(details));
	}

	public ReportSection(String header, List<ReportDetail> details) {
		this.header = header;
		this.details = new ArrayList<>(details);
	}
}
