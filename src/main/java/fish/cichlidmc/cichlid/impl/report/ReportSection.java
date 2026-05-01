package fish.cichlidmc.cichlid.impl.report;

import java.util.Arrays;
import java.util.List;

public record ReportSection(String header, List<ReportDetail> details) {
	public ReportSection(String header, List<ReportDetail> details) {
		this.header = header;
		this.details = List.copyOf(details);
	}

	public ReportSection(String header) {
		this(header, List.of());
	}

	public ReportSection(String header, ReportDetail... details) {
		this(header, Arrays.asList(details));
	}
}
