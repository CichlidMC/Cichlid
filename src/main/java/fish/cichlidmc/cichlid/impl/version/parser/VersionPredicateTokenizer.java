package fish.cichlidmc.cichlid.impl.version.parser;

import fish.cichlidmc.cichlid.api.version.VersionPredicate;
import fish.cichlidmc.cichlid.impl.util.Utils;
import fish.cichlidmc.cichlid.impl.version.parser.token.BooleanOperatorToken;
import fish.cichlidmc.cichlid.impl.version.parser.token.ParenthesisToken;
import fish.cichlidmc.cichlid.impl.version.parser.token.Token;
import fish.cichlidmc.cichlid.impl.version.parser.token.VersionOperatorToken;
import fish.cichlidmc.cichlid.impl.version.parser.token.VersionToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VersionPredicateTokenizer {
	public static final Map<String, Token> CONSTANT_TOKENS = Utils.make(() -> {
		Map<String, Token> map = new LinkedHashMap<>();
		List<Class<? extends Token>> classes = new ArrayList<>();
		classes.add(ParenthesisToken.class);
		classes.add(BooleanOperatorToken.class);
		classes.add(VersionOperatorToken.class);

		for (Class<? extends Token> clazz : classes) {
			for (Token token : clazz.getEnumConstants()) {
				map.put(token.toString(), token);
			}
		}

		return map;
	});

	public static List<Token> tokenize(String string) {
		List<Token> tokens = new ArrayList<>();
		for (int i = 0; i < string.length();) {
			if (string.charAt(i) == ' ') {
				i++;
				continue;
			}

			String substring = string.substring(i);
			Token constant = readConstantToken(substring);
			if (constant != null) {
				tokens.add(constant);
				i += constant.length();
				continue;
			}

			// not a constant token, read a version
			Token prev = previous(tokens);
			if (!(prev instanceof VersionOperatorToken)) {
				throw VersionPredicate.SyntaxException.of("Expected operator before version at index " + i, string);
			}

			// read ahead to find the end of the version
			// end is either whitespace, string end, or another token
			int end = i;
			while (true) {
				if (end >= string.length() || string.charAt(end) == ' ')
					break;

				Token nextToken = readConstantToken(string.substring(end));
				if (nextToken != null)
					break;

				end++;
			}

			String versionString = string.substring(i, end);
			VersionToken token = new VersionToken(versionString);
			tokens.add(token);
			i += token.length();
		}
		return tokens;
	}

	private static Token readConstantToken(String s) {
		for (Map.Entry<String, Token> entry : CONSTANT_TOKENS.entrySet()) {
			if (s.startsWith(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static Token previous(List<Token> tokens) {
		return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
	}
}
