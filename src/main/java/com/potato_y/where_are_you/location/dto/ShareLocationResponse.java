package com.potato_y.where_are_you.location.dto;

import com.potato_y.where_are_you.location.domain.UserLocation;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.user.dto.UserResponse;
import java.util.List;

public record ShareLocationResponse(
    LocationInfo targetLocation,
    List<ShareDataResponse> data
) {

  public ShareLocationResponse(GroupSchedule schedule, List<UserLocation> locations) {
    this(
        new LocationInfo(schedule),
        locations.stream().map(ShareDataResponse::new).toList()
    );
  }

  public record LocationInfo(
      String location,
      CoordinateResponse coordinate
  ) {

    public LocationInfo(GroupSchedule schedule) {
      this(
          schedule.getLocation(),
          new CoordinateResponse(schedule.getLocationLatitude(), schedule.getLocationLongitude())
      );
    }
  }

  public record ShareDataResponse(
      UserResponse user,
      CoordinateResponse location
  ) {

    public ShareDataResponse(UserLocation userLocation) {
      this(
          new UserResponse(userLocation.getUser()),
          new CoordinateResponse(userLocation.getLocationLatitude(),
              userLocation.getLocationLongitude())
      );
    }
  }

  public record CoordinateResponse(
      double latitude,
      double longitude
  ) {

  }
}
