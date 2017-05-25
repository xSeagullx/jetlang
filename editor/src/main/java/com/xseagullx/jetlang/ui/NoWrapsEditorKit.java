package com.xseagullx.jetlang.ui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Solution has been taken, and adapted from: http://java-sl.com/wrap.html
 */
class NoWrapViewFactory implements ViewFactory {
	private final ViewFactory originalFactory;

	NoWrapViewFactory(ViewFactory originalFactory) {
		this.originalFactory = originalFactory;
	}

	@Override public View create(Element elem) {
		if (AbstractDocument.ParagraphElementName.equals(elem.getName())) {
			return new ParagraphView(elem) {
				public void layout(int width, int height) {
					super.layout(Short.MAX_VALUE, height);
				}

				public float getMinimumSpan(int axis) {
					return super.getPreferredSpan(axis);
				}
			};
		}
		return originalFactory.create(elem);
	}
}

public class NoWrapsEditorKit extends StyledEditorKit {
	private ViewFactory factoryWithCustomParagraphView = null;

	@Override public ViewFactory getViewFactory() {
		if (factoryWithCustomParagraphView == null) {
			factoryWithCustomParagraphView = new NoWrapViewFactory(super.getViewFactory());
		}

		return factoryWithCustomParagraphView;
	}
}
