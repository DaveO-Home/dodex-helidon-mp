/**
 * @fileoverview gRPC-Web generated client stub for handicap.grpc
 * @enhanceable
 * @public
 */

// GENERATED CODE -- DO NOT EDIT!


/* eslint-disable */
// @ts-nocheck



const grpc = {};
grpc.web = require('grpc-web');

const proto = {};
proto.handicap = {};
proto.handicap.grpc = require('./handicap_pb.js');

/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?grpc.web.ClientOptions} options
 * @constructor
 * @struct
 * @final
 */
proto.handicap.grpc.HandicapServiceClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options.format = 'text';

  /**
   * @private @const {!grpc.web.GrpcWebClientBase} The client
   */
  this.client_ = new grpc.web.GrpcWebClientBase(options);

  /**
   * @private @const {string} The hostname
   */
  this.hostname_ = hostname;

};


/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?grpc.web.ClientOptions} options
 * @constructor
 * @struct
 * @final
 */
proto.handicap.grpc.HandicapServicePromiseClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options.format = 'text';

  /**
   * @private @const {!grpc.web.GrpcWebClientBase} The client
   */
  this.client_ = new grpc.web.GrpcWebClientBase(options);

  /**
   * @private @const {string} The hostname
   */
  this.hostname_ = hostname;

};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.ListCoursesResponse>}
 */
const methodDescriptor_HandicapService_ListCourses = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/ListCourses',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.ListCoursesResponse,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.ListCoursesResponse.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.ListCoursesResponse)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.ListCoursesResponse>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.listCourses =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/ListCourses',
      request,
      metadata || {},
      methodDescriptor_HandicapService_ListCourses,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.ListCoursesResponse>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.listCourses =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/ListCourses',
      request,
      metadata || {},
      methodDescriptor_HandicapService_ListCourses);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.HandicapData>}
 */
const methodDescriptor_HandicapService_GetCourses = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/GetCourses',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.HandicapData,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.HandicapData.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.HandicapData)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.HandicapData>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.getCourses =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GetCourses',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GetCourses,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.HandicapData>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.getCourses =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GetCourses',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GetCourses);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.HandicapData>}
 */
const methodDescriptor_HandicapService_AddRating = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/AddRating',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.HandicapData,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.HandicapData.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.HandicapData)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.HandicapData>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.addRating =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/AddRating',
      request,
      metadata || {},
      methodDescriptor_HandicapService_AddRating,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.HandicapData>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.addRating =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/AddRating',
      request,
      metadata || {},
      methodDescriptor_HandicapService_AddRating);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.HandicapData>}
 */
const methodDescriptor_HandicapService_AddScore = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/AddScore',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.HandicapData,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.HandicapData.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.HandicapData)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.HandicapData>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.addScore =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/AddScore',
      request,
      metadata || {},
      methodDescriptor_HandicapService_AddScore,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.HandicapData>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.addScore =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/AddScore',
      request,
      metadata || {},
      methodDescriptor_HandicapService_AddScore);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.HandicapData>}
 */
const methodDescriptor_HandicapService_RemoveScore = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/RemoveScore',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.HandicapData,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.HandicapData.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.HandicapData)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.HandicapData>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.removeScore =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/RemoveScore',
      request,
      metadata || {},
      methodDescriptor_HandicapService_RemoveScore,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.HandicapData>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.removeScore =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/RemoveScore',
      request,
      metadata || {},
      methodDescriptor_HandicapService_RemoveScore);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.HandicapData>}
 */
const methodDescriptor_HandicapService_GolferScores = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/GolferScores',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.HandicapData,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.HandicapData.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.HandicapData)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.HandicapData>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.golferScores =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GolferScores',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GolferScores,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.HandicapData>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.golferScores =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GolferScores',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GolferScores);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.HandicapSetup,
 *   !proto.handicap.grpc.HandicapData>}
 */
const methodDescriptor_HandicapService_GetGolfer = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/GetGolfer',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.HandicapSetup,
  proto.handicap.grpc.HandicapData,
  /**
   * @param {!proto.handicap.grpc.HandicapSetup} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.HandicapData.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.HandicapSetup} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.HandicapData)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.HandicapData>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.getGolfer =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GetGolfer',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GetGolfer,
      callback);
};


/**
 * @param {!proto.handicap.grpc.HandicapSetup} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.HandicapData>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.getGolfer =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GetGolfer',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GetGolfer);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.ListPublicGolfers>}
 */
const methodDescriptor_HandicapService_ListGolfers = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/ListGolfers',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.ListPublicGolfers,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.ListPublicGolfers.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.ListPublicGolfers)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.ListPublicGolfers>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.listGolfers =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/ListGolfers',
      request,
      metadata || {},
      methodDescriptor_HandicapService_ListGolfers,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.ListPublicGolfers>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.listGolfers =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/ListGolfers',
      request,
      metadata || {},
      methodDescriptor_HandicapService_ListGolfers);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.handicap.grpc.Command,
 *   !proto.handicap.grpc.HandicapData>}
 */
const methodDescriptor_HandicapService_GetGolfers = new grpc.web.MethodDescriptor(
  '/handicap.grpc.HandicapService/GetGolfers',
  grpc.web.MethodType.UNARY,
  proto.handicap.grpc.Command,
  proto.handicap.grpc.HandicapData,
  /**
   * @param {!proto.handicap.grpc.Command} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.handicap.grpc.HandicapData.deserializeBinary
);


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.RpcError, ?proto.handicap.grpc.HandicapData)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.handicap.grpc.HandicapData>|undefined}
 *     The XHR Node Readable Stream
 */
proto.handicap.grpc.HandicapServiceClient.prototype.getGolfers =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GetGolfers',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GetGolfers,
      callback);
};


/**
 * @param {!proto.handicap.grpc.Command} request The
 *     request proto
 * @param {?Object<string, string>=} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.handicap.grpc.HandicapData>}
 *     Promise that resolves to the response
 */
proto.handicap.grpc.HandicapServicePromiseClient.prototype.getGolfers =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/handicap.grpc.HandicapService/GetGolfers',
      request,
      metadata || {},
      methodDescriptor_HandicapService_GetGolfers);
};


module.exports = proto.handicap.grpc;

