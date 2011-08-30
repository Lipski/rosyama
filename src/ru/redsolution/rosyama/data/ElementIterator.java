package ru.redsolution.rosyama.data;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementIterator implements Iterator<Element> {
	/**
	 * Список нод.
	 */
	private final NodeList nodeList;

	/**
	 * Текущая позиция в списке.
	 */
	private int index;

	public ElementIterator(NodeList nodeList) {
		this.nodeList = nodeList;
		index = 0;
	}

	@Override
	public boolean hasNext() {
		int previous = index;
		Element element = next();
		index = previous;
		return element != null;
	}

	@Override
	public Element next() {
		while (index < nodeList.getLength()) {
			Node node = nodeList.item(index);
			index++;
			if (node instanceof Element)
				return (Element) node;
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
