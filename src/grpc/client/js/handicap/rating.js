// source: handicap.proto
/**
 * @fileoverview
 * @enhanceable
 * @suppress {missingRequire} reports error on implicit type usages.
 * @suppress {messageConventions} JS Compiler reports an error if a variable or
 *     field starts with 'MSG_' and isn't a translatable message.
 * @public
 */
// GENERATED CODE -- DO NOT EDIT!
/* eslint-disable */
// @ts-nocheck

goog.provide('proto.handicap.grpc.Rating');

goog.require('jspb.BinaryReader');
goog.require('jspb.BinaryWriter');
goog.require('jspb.Message');

/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.handicap.grpc.Rating = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.handicap.grpc.Rating, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  /**
   * @public
   * @override
   */
  proto.handicap.grpc.Rating.displayName = 'proto.handicap.grpc.Rating';
}



if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * Optional fields that are not set will be set to undefined.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     net/proto2/compiler/js/internal/generator.cc#kKeyword.
 * @param {boolean=} opt_includeInstance Deprecated. whether to include the
 *     JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @return {!Object}
 */
proto.handicap.grpc.Rating.prototype.toObject = function(opt_includeInstance) {
  return proto.handicap.grpc.Rating.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Deprecated. Whether to include
 *     the JSPB instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.handicap.grpc.Rating} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.handicap.grpc.Rating.toObject = function(includeInstance, msg) {
  var f, obj = {
    tee: jspb.Message.getFieldWithDefault(msg, 1, 0),
    rating: jspb.Message.getFieldWithDefault(msg, 2, ""),
    slope: jspb.Message.getFieldWithDefault(msg, 3, 0),
    par: jspb.Message.getFieldWithDefault(msg, 4, 0),
    color: jspb.Message.getFieldWithDefault(msg, 5, ""),
    seq: jspb.Message.getFieldWithDefault(msg, 6, 0)
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.handicap.grpc.Rating}
 */
proto.handicap.grpc.Rating.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.handicap.grpc.Rating;
  return proto.handicap.grpc.Rating.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.handicap.grpc.Rating} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.handicap.grpc.Rating}
 */
proto.handicap.grpc.Rating.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {number} */ (reader.readInt32());
      msg.setTee(value);
      break;
    case 2:
      var value = /** @type {string} */ (reader.readString());
      msg.setRating(value);
      break;
    case 3:
      var value = /** @type {number} */ (reader.readInt32());
      msg.setSlope(value);
      break;
    case 4:
      var value = /** @type {number} */ (reader.readInt32());
      msg.setPar(value);
      break;
    case 5:
      var value = /** @type {string} */ (reader.readString());
      msg.setColor(value);
      break;
    case 6:
      var value = /** @type {number} */ (reader.readInt32());
      msg.setSeq(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.handicap.grpc.Rating.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.handicap.grpc.Rating.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.handicap.grpc.Rating} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.handicap.grpc.Rating.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getTee();
  if (f !== 0) {
    writer.writeInt32(
      1,
      f
    );
  }
  f = message.getRating();
  if (f.length > 0) {
    writer.writeString(
      2,
      f
    );
  }
  f = message.getSlope();
  if (f !== 0) {
    writer.writeInt32(
      3,
      f
    );
  }
  f = message.getPar();
  if (f !== 0) {
    writer.writeInt32(
      4,
      f
    );
  }
  f = message.getColor();
  if (f.length > 0) {
    writer.writeString(
      5,
      f
    );
  }
  f = message.getSeq();
  if (f !== 0) {
    writer.writeInt32(
      6,
      f
    );
  }
};


/**
 * optional int32 tee = 1;
 * @return {number}
 */
proto.handicap.grpc.Rating.prototype.getTee = function() {
  return /** @type {number} */ (jspb.Message.getFieldWithDefault(this, 1, 0));
};


/**
 * @param {number} value
 * @return {!proto.handicap.grpc.Rating} returns this
 */
proto.handicap.grpc.Rating.prototype.setTee = function(value) {
  return jspb.Message.setProto3IntField(this, 1, value);
};


/**
 * optional string rating = 2;
 * @return {string}
 */
proto.handicap.grpc.Rating.prototype.getRating = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 2, ""));
};


/**
 * @param {string} value
 * @return {!proto.handicap.grpc.Rating} returns this
 */
proto.handicap.grpc.Rating.prototype.setRating = function(value) {
  return jspb.Message.setProto3StringField(this, 2, value);
};


/**
 * optional int32 slope = 3;
 * @return {number}
 */
proto.handicap.grpc.Rating.prototype.getSlope = function() {
  return /** @type {number} */ (jspb.Message.getFieldWithDefault(this, 3, 0));
};


/**
 * @param {number} value
 * @return {!proto.handicap.grpc.Rating} returns this
 */
proto.handicap.grpc.Rating.prototype.setSlope = function(value) {
  return jspb.Message.setProto3IntField(this, 3, value);
};


/**
 * optional int32 par = 4;
 * @return {number}
 */
proto.handicap.grpc.Rating.prototype.getPar = function() {
  return /** @type {number} */ (jspb.Message.getFieldWithDefault(this, 4, 0));
};


/**
 * @param {number} value
 * @return {!proto.handicap.grpc.Rating} returns this
 */
proto.handicap.grpc.Rating.prototype.setPar = function(value) {
  return jspb.Message.setProto3IntField(this, 4, value);
};


/**
 * optional string color = 5;
 * @return {string}
 */
proto.handicap.grpc.Rating.prototype.getColor = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 5, ""));
};


/**
 * @param {string} value
 * @return {!proto.handicap.grpc.Rating} returns this
 */
proto.handicap.grpc.Rating.prototype.setColor = function(value) {
  return jspb.Message.setProto3StringField(this, 5, value);
};


/**
 * optional int32 seq = 6;
 * @return {number}
 */
proto.handicap.grpc.Rating.prototype.getSeq = function() {
  return /** @type {number} */ (jspb.Message.getFieldWithDefault(this, 6, 0));
};


/**
 * @param {number} value
 * @return {!proto.handicap.grpc.Rating} returns this
 */
proto.handicap.grpc.Rating.prototype.setSeq = function(value) {
  return jspb.Message.setProto3IntField(this, 6, value);
};


