package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.requests.Req;
import io.reactivex.Flowable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class ResponseDelayEstimate extends AbstractTest {

  private static final int REQUEST_N = 1;
  private static final int SUBSCRIBERS_COUNT = 1000;

  @Ignore
  @Test
  public void read() {

    ReadRequest readRequest = requestStreamRequest();
    Flux<TypedReadResponse<Data>> dataWindow =
        client
            .request()
            .dataWindow(readRequest)
            .map(dataWindowTransformer)
            .repeatWhen(completed -> completed.flatMap(Flowable::just))
            .publishOn(Schedulers.fromExecutorService(Executors.newFixedThreadPool(16)));

    ConcurrentHistogram histogram = new ConcurrentHistogram(20000, 1);
    histogram.setAutoResize(true);
    Recorder recorder = new Recorder(histogram);

    List<TestSubscriber> subscribers = subscribers(SUBSCRIBERS_COUNT, recorder);

    subscribers.forEach(dataWindow::subscribe);
    Mono.delay(Duration.ofSeconds(10)).block();
    subscribers.forEach(TestSubscriber::cancel);
    histogram.outputPercentileDistribution(System.out, 1d);
  }

  @Override
  public void tearDown() {
    stopServerDelayed(100);
  }

  private List<TestSubscriber> subscribers(int count, Recorder recorder) {
    List<TestSubscriber> subscribers = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      subscribers.add(testSubscriber(recorder));
    }
    return subscribers;
  }

  private TestSubscriber testSubscriber(Recorder recorder) {
    return new TestSubscriber(recorder);
  }

  private static class TestSubscriber implements Subscriber {

    private final Recorder recorder;
    private volatile Subscription s;

    public TestSubscriber(Recorder recorder) {
      this.recorder = recorder;
    }

    @Override
    public void onSubscribe(Subscription s) {
      this.s = s;
      s.request(REQUEST_N);
    }

    @Override
    public void onNext(Object o) {
      recorder.record();
      s.request(REQUEST_N);
    }

    @Override
    public void onError(Throwable t) {}

    @Override
    public void onComplete() {}

    public void cancel() {
      if (s != null) {
        s.cancel();
      }
    }
  }

  private static class Recorder {

    private final Histogram histogram;
    private volatile long from;
    private AtomicInteger wip = new AtomicInteger();
    private final Queue<FromTo> queue = new ConcurrentLinkedQueue<>();

    public Recorder(Histogram histogram) {
      this.histogram = histogram;
    }

    public void record() {
      long fr = from;
      long to = System.currentTimeMillis();
      from = to;
      FromTo fromTo = new FromTo(fr, to);
      queue.offer(fromTo);
      if (wip.getAndIncrement() == 0) {
        do {
          FromTo peek = queue.poll();
          if (peek.getFrom() != 0) {
            histogram.recordValue(peek.getTo() - peek.getFrom());
          }
        } while (wip.decrementAndGet() != 0);
      }
    }

    private static class FromTo {

      private final long from;
      private final long to;

      public FromTo(long from, long to) {
        this.from = from;
        this.to = to;
      }

      public long getFrom() {
        return from;
      }

      public long getTo() {
        return to;
      }
    }
  }

  private ReadRequest requestStreamRequest() {
    return Req.read("test", "read").asc().windowWithSize(2).orderByKey().build();
  }
}
