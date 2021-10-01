// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: udpResponse.proto

package com.roomreservation.protobuf.protos;

public interface UdpResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.roomreservation.collection.udpResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>required string response = 1;</code>
   * @return Whether the response field is set.
   */
  boolean hasResponse();
  /**
   * <code>required string response = 1;</code>
   * @return The response.
   */
  java.lang.String getResponse();
  /**
   * <code>required string response = 1;</code>
   * @return The bytes for response.
   */
  com.google.protobuf.ByteString
      getResponseBytes();

  /**
   * <code>required string dateTime = 2;</code>
   * @return Whether the dateTime field is set.
   */
  boolean hasDateTime();
  /**
   * <code>required string dateTime = 2;</code>
   * @return The dateTime.
   */
  java.lang.String getDateTime();
  /**
   * <code>required string dateTime = 2;</code>
   * @return The bytes for dateTime.
   */
  com.google.protobuf.ByteString
      getDateTimeBytes();

  /**
   * <code>required string requestType = 3;</code>
   * @return Whether the requestType field is set.
   */
  boolean hasRequestType();
  /**
   * <code>required string requestType = 3;</code>
   * @return The requestType.
   */
  java.lang.String getRequestType();
  /**
   * <code>required string requestType = 3;</code>
   * @return The bytes for requestType.
   */
  com.google.protobuf.ByteString
      getRequestTypeBytes();

  /**
   * <code>required string requestParameters = 4;</code>
   * @return Whether the requestParameters field is set.
   */
  boolean hasRequestParameters();
  /**
   * <code>required string requestParameters = 4;</code>
   * @return The requestParameters.
   */
  java.lang.String getRequestParameters();
  /**
   * <code>required string requestParameters = 4;</code>
   * @return The bytes for requestParameters.
   */
  com.google.protobuf.ByteString
      getRequestParametersBytes();

  /**
   * <code>required bool status = 5;</code>
   * @return Whether the status field is set.
   */
  boolean hasStatus();
  /**
   * <code>required bool status = 5;</code>
   * @return The status.
   */
  boolean getStatus();
}
