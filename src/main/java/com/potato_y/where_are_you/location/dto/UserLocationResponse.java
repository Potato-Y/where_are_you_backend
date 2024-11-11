package com.potato_y.where_are_you.location.dto;

import com.potato_y.where_are_you.location.domain.UserLocation;
import com.potato_y.where_are_you.user.dto.UserResponse;

public record UserLocationResponse(
    UserResponse user,
    LocationResponse location
) {

  public UserLocationResponse(UserLocation userLocation) {
    this(
        new UserResponse(userLocation.getUser()),
        new LocationResponse(userLocation.getLocationLatitude(),
            userLocation.getLocationLongitude())
    );
  }

  record LocationResponse(
      double locationLatitude,
      double locationLongitude
  ) {

  }
}
