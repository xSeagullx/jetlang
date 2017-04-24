package com.xseagullx.jetlang;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JetLangException extends RuntimeException {
	private TokenInformationHolder element;
	private TokenInformationHolder[] stackTrace;

	public JetLangException(String message, TokenInformationHolder element, TokenInformationHolder[] stackTrace) {
		super(message);
		this.element = element;
		this.stackTrace = stackTrace;
	}

	public TokenInformationHolder[] getJetLangStackTrace() {
		return stackTrace;
	}

	public String getDetailedMessage() {
		return element.toString() + " " + getMessage() + "\n" +
			Arrays.stream(stackTrace).map(TokenInformationHolder::toString).collect(Collectors.joining("\n"));
	}
}
