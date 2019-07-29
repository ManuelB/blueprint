sap.ui.define(["sap/ui/model/odata/v2/ODataModel", "sap/ui/core/cache/CacheManager"],
function(ODataModel, CacheManager) {
	"use strict";
	return ODataModel.extend("blueprint.model.ETagODataModel", {

		constructor: function () {
			ODataModel.apply(this, arguments);
			/*
			 * This map will contains for every url that
			 * provides an etag header a pair with the following
			 * structure:
			 * {
			 *     "Todos?sap-context-token=1&$skip=60&$top=20" : {
			 *         "ETag": "9d2a55bcfc40a85c4aa7fcf83caa3e0d",
			 *         "body" : "\{...\}",
			 *         "data" : {...}
			 *     }
			 * }
			 * 
			 */
			this.oETagUrlCache = {};
			var pCacheLoaded = new Promise(function (fnResolve) {			
				// Read cache from persistent cache
				CacheManager.get("ETagUrlCache").then(function(sETagUrlCache) {
					this.oETagUrlCache = JSON.parse(sETagUrlCache);
					fnResolve();
				}.bind(this));
			}.bind(this));
			var pMetadataLoaded = this.oMetadata.pLoaded;
			this.oMetadata.pLoaded = Promise.all([pMetadataLoaded, pCacheLoaded]);
		},
		_updateETag : function(oRequest, oResponse) {
			// Add the body of every request with the url ot our internal map
			if("ETag" in oResponse.headers) {
				var sUrl = oRequest.requestUri.replace(this.sServiceUrl + '/', '');
				if (!sUrl.startsWith("/")) {
					sUrl = "/" + sUrl;
				}
				var contentType = "";
				if("Content-Type" in oResponse.headers) {					
					contentType = oResponse.headers["Content-Type"];
				}
				this.oETagUrlCache[sUrl] = {
					"ETag": oResponse.headers.ETag,
					"statusCode": oResponse.statusCode,
					"body": oResponse.body,
					"data": contentType.match(/^application\/json.*/) ? JSON.parse(oResponse.body).d : oResponse.body, 
					"Content-Type": contentType
				}
				
				CacheManager.set("ETagUrlCache", JSON.stringify(this.oETagUrlCache));
				
			}
			return ODataModel.prototype._updateETag.apply(this, arguments);
		},
		_createRequest: function () {
			var oRequest = ODataModel.prototype._createRequest.apply(this, arguments);
			var sUrl = oRequest.requestUri.replace(this.sServiceUrl + '/', '');
			if (!sUrl.startsWith("/")) {
				sUrl = "/" + sUrl;
			}
			if(sUrl in this.oETagUrlCache) {
				oRequest.headers["If-None-Match"] = this.oETagUrlCache[sUrl].ETag;
			}
			return oRequest;
		},
		_getODataHandler: function (sUrl) {
			var oHandler = ODataModel.prototype._getODataHandler.apply(this, arguments);
			if (sUrl.indexOf("$batch") > -1) {
				var oldPartHandlerRead = oHandler.partHandler.read;
				var oldRead = oHandler.read;
				oHandler.read = function(oResponse, oParameter) {
					// Replace status so the request will be recognized as success
					oResponse.body = oResponse.body.replace(/304 Not Modified/g, "299 Custom not modified");
					var oRetVal = oldRead.apply(oHandler, arguments);
					return oRetVal;
				};
				
			}
			return oHandler;
		},
		_processSuccess : function(oRequest, oResponse, fnSuccess, mGetEntities, mChangeEntities, mEntityTypes, bBatch, aRequests) {
			// if we get a not modified header back and we have the Url in our cache
			var sUrl = oRequest.requestUri.replace(this.sServiceUrl + '/', '');
			if (!sUrl.startsWith("/")) {
				sUrl = "/" + sUrl;
			}
			if((oResponse.statusCode === 299 || oResponse.statusCode === '299') && sUrl in this.oETagUrlCache) {
				oResponse.statusCode = this.oETagUrlCache[sUrl].statusCode;
				oResponse.body = this.oETagUrlCache[sUrl].body;
				oResponse.data = this.oETagUrlCache[sUrl].data;
			}
			return ODataModel.prototype._processSuccess.apply(this, arguments);
		}

	});
});
