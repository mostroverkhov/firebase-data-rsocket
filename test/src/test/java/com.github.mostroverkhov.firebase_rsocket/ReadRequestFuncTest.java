package com.github.mostroverkhov.firebase_rsocket;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.mostroverkhov.firebase_rsocket.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.requests.Req;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

/** Created by Maksym Ostroverkhov on 28.02.17. */
public class ReadRequestFuncTest extends AbstractTest {

  private static final int SAMPLE_ITEM_COUNT = 10;
  private static final int WINDOW_SIZE = 2;
  private static final int READ_REPEAT_N = 3;

  @Test
  public void presentRead() {

    ReadRequest readRequest = presentReadRequest();
    Flux<TypedReadResponse<Data>> dataWindow =
        client
            .request()
            .dataWindow(readRequest)
            .map(dataWindowTransformer)
            .publishOn(Schedulers.elastic());

    int windowsCount = SAMPLE_ITEM_COUNT / WINDOW_SIZE;
    StepVerifier.create(dataWindow.collectList())
        .expectNextMatches(list -> verifyDataWindows(windowsCount, list))
        .expectComplete()
        .verify(Duration.ofSeconds(20));
  }

  private boolean verifyDataWindows(int windowsCount, List<TypedReadResponse<Data>> windows) {
    if (windows.size() != windowsCount) {
      return false;
    }
    for (int i = 0; i < windowsCount; i++) {
      TypedReadResponse<Data> window = windows.get(i);
      if (window.getData().size() != 2) {
        return false;
      }
      if (!assertWindowContent(window, i)) {
        return false;
      }
    }
    return true;
  }

  @Test
  public void presentReadStartWith() {

    ReadRequest allRequest = presentReadRequest();
    Flux<TypedReadResponse<Data>> readResponse =
        client
            .request()
            .dataWindow(allRequest)
            .map(dataWindowTransformer)
            .publishOn(Schedulers.elastic());

    TypedReadResponse<Data> last = readResponse.takeLast(1).blockFirst();
    String startKey = last.getReadRequest().getWindowStartWith();

    ReadRequest tailRequest =
        Req.read("test", "read")
            .asc()
            .windowWithSize(WINDOW_SIZE)
            .orderByKey()
            .startWith(startKey)
            .build();

    Flux<TypedReadResponse<Data>> tailResponse =
        client
            .request()
            .dataWindow(tailRequest)
            .map(dataWindowTransformer)
            .publishOn(Schedulers.elastic());

    StepVerifier.create(tailResponse)
        .expectNextMatches(
            window -> {
              List<Data> data = window.getData();
              return data.size() == 2 && contains(data, new Data("8", "8"), new Data("9", "9"));
            })
        .expectComplete()
        .verify(Duration.ofSeconds(20));
  }

  @Test
  public void missingRead() {

    ReadRequest readRequest = missingReadRequest();
    Flux<TypedReadResponse<Data>> response =
        client
            .request()
            .dataWindow(readRequest)
            .map(dataWindowTransformer)
            .publishOn(Schedulers.elastic());

    StepVerifier.create(response)
        .expectNextCount(0)
        .expectComplete()
        .verify(Duration.ofSeconds(10));
  }

  @Test
  public void presentReadRepeat() {

    ReadRequest readRequest = presentReadRequest();
    Flux<TypedReadResponse<Data>> dataWindowFlow =
        client.request().dataWindow(readRequest).map(dataWindowTransformer);

    Flux<TypedReadResponse<Data>> response =
        dataWindowFlow.repeat(READ_REPEAT_N).publishOn(Schedulers.elastic());

    int windowsCount = READ_REPEAT_N * SAMPLE_ITEM_COUNT / WINDOW_SIZE;

    StepVerifier.create(response)
        .expectNextCount(windowsCount)
        .expectComplete()
        .verify(Duration.ofSeconds(30));
  }

  @Test
  public void presentReadNotifications() {

    ReadRequest readRequest = presentReadRequest();
    Mono<List<TypedNotifResponse<Data>>> notifications =
        client
            .request()
            .dataWindowNotifications(readRequest)
            .map(notifTransformer)
            .takeUntilOther(Mono.delay(Duration.ofSeconds(10)))
            .collectList()
            .publishOn(Schedulers.elastic());

    Duration duration =
        StepVerifier.create(notifications)
            .expectNextMatches(
                responses -> dataWindowMatches(responses, new Data("0", "0"), new Data("1", "1")))
            .expectComplete()
            .verify(Duration.ofSeconds(11));

    assertThat(duration).isBetween(Duration.ofSeconds(10), Duration.ofSeconds(11));
  }

  @Test
  public void presentReadNotificationsChained() {

    ReadRequest readRequest = presentReadRequest();
    Flux<TypedNotifResponse<Data>> notifications =
        client.request().dataWindowNotifications(readRequest).map(notifTransformer);

    Mono<List<TypedNotifResponse<Data>>> nextWindowNotifications =
        notifications
            .publishOn(Schedulers.elastic())
            .filter(TypedNotifResponse::isNextWindow)
            .map(TypedNotifResponse::getNextDataWindow)
            .flatMap(
                nextWindowRequest ->
                    client
                        .request()
                        .dataWindowNotifications(nextWindowRequest)
                        .map(notifTransformer))
            .takeUntilOther(Mono.delay(Duration.ofSeconds(10)))
            .collectList();

    Duration duration =
        StepVerifier.create(nextWindowNotifications)
            .expectNextMatches(
                responses -> dataWindowMatches(responses, new Data("2", "2"), new Data("3", "3")))
            .expectComplete()
            .verify(Duration.ofSeconds(11));

    assertThat(duration).isBetween(Duration.ofSeconds(10), Duration.ofSeconds(11));
  }

  @Test
  public void absentReadNotifications() {

    ReadRequest readRequest = missingReadRequest();

    Mono<List<TypedNotifResponse<Data>>> notifications =
        client
            .request()
            .dataWindowNotifications(readRequest)
            .map(notifTransformer)
            .takeUntilOther(Mono.delay(Duration.ofSeconds(10)))
            .collectList()
            .publishOn(Schedulers.elastic());

    Duration duration =
        StepVerifier.create(notifications)
            .expectNextMatches(
                responses -> {
                  boolean sizeMatches = responses.size() == 1;
                  if (!sizeMatches) {
                    return false;
                  }
                  TypedNotifResponse<Data> response = responses.get(0);
                  return response.isNextWindow()
                      && response.getNextDataWindow().getWindowStartWith().isEmpty();
                })
            .expectComplete()
            .verify(Duration.ofSeconds(11));

    assertThat(duration).isBetween(Duration.ofSeconds(10), Duration.ofSeconds(11));
  }

  private boolean dataWindowMatches(
      List<TypedNotifResponse<Data>> responses, Data first, Data second) {
    boolean sizeMatches = responses.size() == 3;
    List<Data> dataList = changeEvents(responses);
    boolean dataMatches = contains(dataList, first, second);

    Optional<TypedNotifResponse<Data>> nextWindow = singleNextEvent(responses);
    boolean nextWindowMatches =
        nextWindow
            .map(TypedNotifResponse::getNextDataWindow)
            .map(ReadRequest::getWindowStartWith)
            .isPresent();
    return sizeMatches && dataMatches && nextWindowMatches;
  }

  private <T> boolean contains(List<T> items, T... targetItems) {
    for (T targetItem : targetItems) {
      boolean found = false;
      for (T item : items) {
        if (item.equals(targetItem)) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  private <T> Optional<TypedNotifResponse<T>> singleNextEvent(
      List<TypedNotifResponse<T>> notifResponses) {
    List<TypedNotifResponse<T>> nextEvents =
        notifResponses.stream().filter(TypedNotifResponse::isNextWindow).collect(toList());
    if (nextEvents.size() == 1) {
      return Optional.of(nextEvents.get(0));
    } else {
      return Optional.empty();
    }
  }

  private <T> List<T> changeEvents(List<TypedNotifResponse<T>> notifResponses) {
    return notifResponses
        .stream()
        .filter(TypedNotifResponse::isChangeEvent)
        .map(TypedNotifResponse::getItem)
        .collect(toList());
  }

  private ReadRequest presentReadRequest() {
    return Req.read("test", "read").asc().windowWithSize(WINDOW_SIZE).orderByKey().build();
  }

  private ReadRequest missingReadRequest() {
    return Req.read("foo", "bar").asc().windowWithSize(WINDOW_SIZE).orderByKey().build();
  }

  private boolean assertWindowContent(TypedReadResponse<Data> window, int index) {
    int doubledIndex = index * 2;
    for (Data data : window.getData()) {
      if (!String.valueOf(doubledIndex).equals(data.getId())) {
        return false;
      }
      doubledIndex++;
    }
    return true;
  }
}
