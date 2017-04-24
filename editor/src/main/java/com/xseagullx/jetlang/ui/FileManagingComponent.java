package com.xseagullx.jetlang.ui;

import com.xseagullx.jetlang.EditorState;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class FileManagingComponent {
	private final Consumer<File> onOpen;
	private final EditorState editorState;

	public FileManagingComponent(Consumer<File> onOpen, EditorState editorState) {
		this.onOpen = onOpen;
		this.editorState = editorState;
	}

	public void openFileDialog(JFrame frame) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("JL and plain text files", "jl", "txt"));
		int returnVal = chooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			onOpen.accept(chooser.getSelectedFile());
	}

	public void saveFile(JFrame frame, String text) {
		if (editorState.getFile() == null) {
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showSaveDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				editorState.setFile(chooser.getSelectedFile());
		}

		if (editorState.getFile() == null)
			return;

		try {
			Files.write(Paths.get(editorState.getFile().getPath()), text.getBytes("UTF-8"));
			JOptionPane.showMessageDialog(frame, "File saved");
		}
		catch (UnsupportedEncodingException e) {
			throw new ThisShouldNeverHappenException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e); // todo show in ide
		}
	}
}
