package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.ParseError;

import java.util.Collection;

public class HighlightTask extends Task<HighlightTask.HighlightingResults> {
	public static class HighlightingResults {
		public final Collection<StyledChunk> styledChunks;
		public final Collection<ParseError> parseErrors;

		HighlightingResults(Collection<ParseError> parseErrors, Collection<StyledChunk> styledChunks) {
			this.parseErrors = parseErrors;
			this.styledChunks = styledChunks;
		}
	}

	private final DocumentSnapshot documentSnapshot;
	private final HighlightingService highlightingService;

	public HighlightTask(DocumentSnapshot documentSnapshot, HighlightingService highlightingService) {
		this.highlightingService = highlightingService;
		this.documentSnapshot = documentSnapshot;
	}

	public DocumentSnapshot getDocumentSnapshot() {
		return documentSnapshot;
	}

	@Override public String getId() {
		return "highlightTask:" + documentSnapshot.getId();
	}

	@Override public HighlightingResults get() {
		return highlightingService.highlight(documentSnapshot);
	}
}
