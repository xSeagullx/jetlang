package com.xseagullx.jetlang;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JetLangException extends RuntimeException {
	private TokenInformationHolder element;
	private TokenInformationHolder[] stackTrace;

	public JetLangException(String message, TokenInformationHolder element) {
		super(message);
		this.element = element;
	}

	public TokenInformationHolder[] getJetLangStackTrace() {
		return stackTrace;
	}

	public void setJetLangStackTrace(TokenInformationHolder[] stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getDetailedMessage() {
		return element.toString() + " " + getMessage() + "\n" +
			Arrays.stream(stackTrace).map(TokenInformationHolder::toString).collect(Collectors.joining("\n"));
	}
}
