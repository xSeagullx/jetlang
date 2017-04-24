package com.xseagullx.jetlang.services;

import java.util.Collection;

public class HighlightTask extends Task<Collection<StyledChunk>> {
	private final DocumentSnapshot documentSnapshot;
	private final HighlightingService highlightingService;

	public HighlightTask(DocumentSnapshot documentSnapshot, HighlightingService highlightingService) {
		this.highlightingService = highlightingService;
		this.documentSnapshot = documentSnapshot;
	}

	@Override public Collection<StyledChunk> call() {
		return highlightingService.highlight(documentSnapshot);
	}

	public DocumentSnapshot getDocumentSnapshot() {
		return documentSnapshot;
	}

	@Override public String getId() {
		return "highlightTask:" + documentSnapshot.getId();
	}
}
