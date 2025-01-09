package com.potato_y.where_are_you.location.dto;

public record StateMessage() {

  public record StateMessageResponse(
      String message
  ) {

  }

  public record StateMessageRequest(
      String message
  ) {

  }
}
