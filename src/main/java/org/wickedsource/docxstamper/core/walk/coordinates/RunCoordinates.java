package org.wickedsource.docxstamper.core.walk.coordinates;

import org.docx4j.wml.R;

import java.util.ListIterator;

public class RunCoordinates extends AbstractCoordinates {

	private final R run;
	private final ListIterator<Object> index;

	public RunCoordinates(R run, ListIterator<Object> index) {
		this.run = run;
		this.index = index;
	}

	public R getRun() {
		return run;
	}

	public ListIterator<Object> getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return String.format("run at index %d", index);
	}

}
