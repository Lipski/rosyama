package ru.redsolution.rosyama;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

class EmptyFileBody extends AbstractContentBody {
	public EmptyFileBody() {
		super("application/octet-stream");
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		// Do noting
	}

	@Override
	public String getCharset() {
		return null;
	}

	@Override
	public long getContentLength() {
		return 0;
	}

	@Override
	public String getTransferEncoding() {
		return MIME.ENC_BINARY;
	}

	@Override
	public String getFilename() {
		return "";
	}
}