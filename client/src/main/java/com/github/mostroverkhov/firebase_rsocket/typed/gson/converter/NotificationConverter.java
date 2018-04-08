package com.github.mostroverkhov.firebase_rsocket.typed.gson.converter;

import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifEventKind;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.TypedNotifResponse;
import com.google.gson.Gson;
import java.util.function.Function;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class NotificationConverter<T> implements Function<NotifResponse, TypedNotifResponse<T>> {

  private Gson gson;
  private Class<T> itemType;

  public NotificationConverter(Gson gson, Class<T> itemType) {
    this.gson = gson;
    this.itemType = itemType;
  }

  @Override
  public TypedNotifResponse<T> apply(NotifResponse input) {
    return typedResponse(input);
  }

  private TypedNotifResponse<T> typedResponse(NotifResponse input) {
    if (input.isNextWindow()) {
      return TypedNotifResponse.nextWindow(input.getNextDataWindow());
    } else {
      NotifEventKind kind = input.getKind();
      T item = typedItem(input.getItem());
      return TypedNotifResponse.changeEvent(kind, item);
    }
  }

  private T typedItem(String item) {
    return gson.fromJson(item, itemType);
  }
}
