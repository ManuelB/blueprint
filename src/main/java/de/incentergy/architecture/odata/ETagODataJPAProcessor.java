package de.incentergy.architecture.odata;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPADefaultProcessor;

import de.incentergy.architecture.io.Util;

public class ETagODataJPAProcessor extends ODataJPADefaultProcessor {
	
	private static final Logger log = Logger.getLogger(ETagODataJPAProcessor.class.getName());

	public ETagODataJPAProcessor(ODataJPAContext oDataJPAContext) {
		super(oDataJPAContext);
	}

	@Override
	public ODataResponse executeBatch(final BatchHandler handler, final String contentType, final InputStream content)
			throws ODataException {
		try {
			oDataJPAContext.setODataContext(getContext());

			ODataResponse batchResponse;
			List<BatchResponsePart> batchResponseParts = new ArrayList<BatchResponsePart>();
			PathInfo pathInfo = getContext().getPathInfo();
			EntityProviderBatchProperties batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo)
					.build();
			List<BatchRequestPart> batchParts = EntityProvider.parseBatchRequest(contentType, content, batchProperties);

			for (BatchRequestPart batchPart : batchParts) {
				BatchResponsePart batchResponsePart = handler.handleBatchPart(batchPart);
				if(batchPart.getRequests().size() == 1 && batchResponsePart.getResponses().size() == 1) {
					ODataResponseBuilder oDataResponseBuilder = ODataResponse.newBuilder();
					ODataResponse firstBatchResponseODataResponse = batchResponsePart.getResponses().get(0);
					
					InputStream inputStream = firstBatchResponseODataResponse.getEntityAsStream();
		        	byte[] targetArray;
					try {
						targetArray = Util.getBytesFromInputStream(inputStream);
						inputStream.close();
						CircleStreamBuffer circleStreamBuffer = new CircleStreamBuffer();
						circleStreamBuffer.getOutputStream().write(targetArray);
						String etag = Util.getETagDigest(targetArray);
						ODataRequest oDataRequest = batchPart.getRequests().get(0);
						String ifNoneMatchHeader = oDataRequest.getRequestHeaderValue("If-None-Match");
						
						if ((ifNoneMatchHeader != null) && (ifNoneMatchHeader.equals(etag))) {
							oDataResponseBuilder.status(HttpStatusCodes.NOT_MODIFIED);
							oDataResponseBuilder.header("Last-Modified", oDataRequest.getRequestHeaderValue("If-Modified-Since"));
						} else {
							oDataResponseBuilder.status(firstBatchResponseODataResponse.getStatus());
							oDataResponseBuilder.eTag(etag);
							oDataResponseBuilder.header("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
							oDataResponseBuilder.entity(circleStreamBuffer.getInputStream());
							for (String key : firstBatchResponseODataResponse.getHeaderNames()) {
								oDataResponseBuilder.header(key, firstBatchResponseODataResponse.getHeader(key));
							}
						}
						batchResponsePart.getResponses().clear();
						batchResponsePart.getResponses().add(oDataResponseBuilder.build());
					} catch (IOException e) {
						log.log(Level.SEVERE, "Could no process etag, e");
					}
				}
				batchResponseParts.add(batchResponsePart);
			}
			batchResponse = EntityProvider.writeBatchResponse(batchResponseParts);
			return batchResponse;
		} finally {
			close(true);
		}
	}

}
