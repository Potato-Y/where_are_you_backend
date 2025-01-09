package com.potato_y.timely.location.dto;

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
