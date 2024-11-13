package com.potato_y.where_are_you.common.utils;

import com.potato_y.where_are_you.common.constants.Number;
import java.time.LocalDateTime;

public class DateTimeUtils {

  public static LocalDateTime clearSecondAndNano(LocalDateTime dateTime) {
    return dateTime.withSecond(Number.ZERO.getValue()).withNano(Number.ZERO.getValue());
  }
}
