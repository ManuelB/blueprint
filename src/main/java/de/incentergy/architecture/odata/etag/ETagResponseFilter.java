package de.incentergy.architecture.odata.etag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;

@Provider
public class ETagResponseFilter implements ContainerResponseFilter {
	
	@Context
	private Providers providers;

	// https://stackoverflow.com/questions/25332103/capture-response-payload-in-jax-rs-filter
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		byte[] arrayOfByte = payloadMessage(responseContext);
		if (arrayOfByte == null) {
			return;
		}
		if (arrayOfByte.length == 0) {
			return;
		}
		
		String md5Hash = getMd5Digest(arrayOfByte);
		responseContext.getHeaders().add("ETag", md5Hash);
		String ifNoneMatchHeader = requestContext.getHeaderString("If-None-Match");
		
		if ((ifNoneMatchHeader != null) && (ifNoneMatchHeader.equals(md5Hash))) {
			responseContext.setStatus(304);
			responseContext.getHeaders().add("Last-Modified", requestContext.getHeaderString("If-Modified-Since"));
			responseContext.setEntity(null);
		} else {
			Calendar localCalendar = Calendar.getInstance();
			localCalendar.set(Calendar.MILLISECOND, 0);
			Date localDate = localCalendar.getTime();
			responseContext.getHeaders().add("Last-Modified", localDate.getTime());
		}
	}
	
    private byte[] payloadMessage(ContainerResponseContext responseContext) throws IOException {
        if (responseContext.hasEntity()) {
        	Object entity = responseContext.getEntity();
        	if(entity instanceof InputStream) {
	        	InputStream initialStream = (InputStream) entity;
	        	byte[] targetArray = getBytesFromInputStream(initialStream);
	            CircleStreamBuffer circleStreamBuffer = new CircleStreamBuffer();
	            circleStreamBuffer.getOutputStream().write(targetArray);
	            responseContext.setEntity(circleStreamBuffer.getInputStream());
	            initialStream.close();
	            return targetArray;
        	}
        }
        return new byte[] {};
    }
    
    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(); 
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) { 
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

	private String getMd5Digest(byte[] paramArrayOfByte) {
		MessageDigest localMessageDigest;
		try {
			localMessageDigest = MessageDigest.getInstance("MD5");
		} catch (Exception localException) {
			throw new RuntimeException("MD5 cryptographic algorithm is not available.", localException);
		}
		byte[] arrayOfByte = localMessageDigest.digest(paramArrayOfByte);
		BigInteger localBigInteger = new BigInteger(1, arrayOfByte);
		StringBuilder localStringBuilder = new StringBuilder(48);
		localStringBuilder.append(localBigInteger.toString(16));
		return localStringBuilder.toString();
	}

}