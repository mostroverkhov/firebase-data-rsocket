package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class Router {

    private final List<ServerMapper<?>> mappers = new ArrayList<>();
    private final List<ServerRequestHandler<?, ?>> handlers = new ArrayList<>();

    public Router route(String key, String value, BiFunction<String, String, Route> routeF) {
        assertRouteArgs(key, value, routeF);
        Route route = routeF.apply(key, value);
        assertRoute(route);
        mappers.add(route.getMapper());
        handlers.add(route.getHandler());
        return this;
    }

    public Router defaultHandler(Supplier<ServerRequestHandler<?, ?>> defHandlerF) {
        assertArg(defHandlerF);
        handlers.add(defHandlerF.get());
        return this;
    }

    public MapperHandler asLists() {
        return new MapperHandler(mappers, handlers);
    }

    static class Route {
        private ServerMapper<?> mapper;
        private ServerRequestHandler<?, ?> handler;

        public Route() {
        }

        public Route mapper(ServerMapper<?> mapper) {
            this.mapper = mapper;
            return this;
        }

        public Route handler(ServerRequestHandler<?, ?> handler) {
            this.handler = handler;
            return this;
        }

        public ServerMapper<?> getMapper() {
            return mapper;
        }

        public ServerRequestHandler<?, ?> getHandler() {
            return handler;
        }
    }

    public static class MapperHandler {
        private final List<ServerMapper<?>> mappers;
        private final List<ServerRequestHandler<?, ?>> handlers;

        public MapperHandler(List<ServerMapper<?>> mappers,
                             List<ServerRequestHandler<?, ?>> handlers) {
            this.mappers = mappers;
            this.handlers = handlers;
        }

        public List<ServerMapper<?>> mappers() {
            return mappers;
        }

        public List<ServerRequestHandler<?, ?>> handlers() {
            return handlers;
        }
    }

    private static void assertArg(Object arg) {
        if (arg == null) {
            throw new IllegalArgumentException("Arg should not be null");
        }
    }

    private static void assertRoute(Route route) {
        if (route.getHandler() == null
                || route.getMapper() == null) {
            throw new IllegalArgumentException("handler and mapper should not be null");
        }
    }

    private static void assertRouteArgs(String key,
                                        String val,
                                        BiFunction<String, String, Route> f) {
        if (key == null || val == null || f == null) {
            throw new IllegalArgumentException("args should not be null");
        }
    }
}
