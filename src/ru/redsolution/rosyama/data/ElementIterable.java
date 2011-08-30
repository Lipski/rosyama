package ru.redsolution.rosyama.data;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ElementIterable implements Iterable<Element> {
	private final NodeList nodeList;

	public ElementIterable(NodeList nodeList) {
		this.nodeList = nodeList;
	}

	@Override
	public Iterator<Element> iterator() {
		return new ElementIterator(nodeList);
	}
}
