package ru.redsolution.rosyama;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;

public class RedirctedHttpClient extends DefaultHttpClient {
	private final ArrayList<String> redirects = new ArrayList<String>();

	private class SmartRedirectHandler extends DefaultRedirectHandler {
		@Override
		public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
			URI uri = super.getLocationURI(response, context);
			redirects.add(response.getFirstHeader("location").getValue());
			return uri;
		}
	}

	@Override
	protected RedirectHandler createRedirectHandler() {
		return new SmartRedirectHandler();
	}

	@Override
	public <T> T execute(HttpUriRequest request,
			ResponseHandler<? extends T> responseHandler) throws IOException,
			ClientProtocolException {
		redirects.clear();
		return super.execute(request, responseHandler);
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request,
			ResponseHandler<? extends T> responseHandler) throws IOException,
			ClientProtocolException {
		redirects.clear();
		return super.execute(target, request, responseHandler);
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request,
			ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		redirects.clear();
		return super.execute(target, request, responseHandler, context);
	}

	@Override
	public <T> T execute(HttpUriRequest request,
			ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		redirects.clear();
		return super.execute(request, responseHandler, context);
	}

	/**
	 * Returns redirect urls if any.
	 * 
	 * @return
	 */
	public Collection<String> getRedirects() {
		return Collections.unmodifiableCollection(redirects);
	}
}
