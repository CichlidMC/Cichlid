package io.github.cichlidmc.cichlid.impl.report;

import io.github.cichlidmc.cichlid.impl.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class ProblemReportTextRenderer {
	public static final String SEPERATOR = "---------------------------------------------------\n";
	public static final String DETAIL_DELIMITER = ": ";
	public static final char NEWLINE = '\n';
	public static final int MAX_LENGTH = 80;
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	public static String render(ProblemReport report) {
		StringBuilder output = new StringBuilder();
		// skip the first line's logger prefix
		output.append(NEWLINE);

		for (ReportSection section : report.sections()) {
			output.append(SEPERATOR);
			split(section.header, MAX_LENGTH).forEach(line -> output.append(line).append(NEWLINE));

			if (!section.details.isEmpty()) {
				output.append(NEWLINE);
				output.append("Details:").append(NEWLINE);

				for (ReportDetail detail : section.details) {
					String keySpacer = Utils.repeat(" ", detail.key.length() + DETAIL_DELIMITER.length());
					int maxLength = detail.bypassLineLimit ? Integer.MAX_VALUE : MAX_LENGTH;
					List<String> message = split(detail.message, maxLength);
					for (int i = 0; i < message.size(); i++) {
						if (i == 0) {
							output.append(detail.key).append(DETAIL_DELIMITER);
						} else {
							output.append(keySpacer);
						}

						output.append(message.get(i)).append(NEWLINE);
					}
				}
			}

			output.append(SEPERATOR);
		}
		return output.toString();
	}

	private static List<String> split(String string, int maxLineLength) {
		if (string.length() <= maxLineLength) {
			return Collections.singletonList(string);
		}

		List<String> list = new ArrayList<>();
		String[] split = WHITESPACE.split(string);
		StringBuilder builder = new StringBuilder(maxLineLength);
		for (String word : split) {
			if (builder.length() != 0 && builder.length() + word.length() > maxLineLength) {
				list.add(builder.toString().trim());
				builder = new StringBuilder();
			}

			builder.append(word).append(' ');
		}
		return list;
	}
}
