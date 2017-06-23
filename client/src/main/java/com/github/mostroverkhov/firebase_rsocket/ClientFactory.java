package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.api.Client;
import com.github.mostroverkhov.firebase_rsocket.api.Transform;
import com.github.mostroverkhov.firebase_rsocket.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ClientTransport;
import io.reactivex.Flowable;

import java.lang.reflect.*;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class ClientFactory {

    private final ClientFlow clientFlow;
    private final ClientCodec codec;
    private final Transform transform;

    public ClientFactory(ClientTransport transport,
                         ClientCodec codec,
                         Transform transform) {
        this.clientFlow = new ClientFlow(transport);
        this.codec = codec;
        this.transform = transform;
    }

    @SuppressWarnings("unchecked")
    public <T> T client(Class<T> client) {
        return (T) Proxy.newProxyInstance(client.getClassLoader(),
                new Class<?>[]{client},
                new ClientInvocationHandler(clientFlow, codec));
    }

    public Client client() {
        return client(Client.class);
    }

    public Transform transform() {
        return transform;
    }

    static class ClientInvocationHandler implements InvocationHandler {

        private final ClientFlow clientFlow;
        private final ClientCodec codec;

        public ClientInvocationHandler(ClientFlow clientFlow, ClientCodec codec) {
            this.clientFlow = clientFlow;
            this.codec = codec;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            if (method.isAnnotationPresent(Action.class)) {
                Action action = method.getDeclaredAnnotation(Action.class);
                Op op = action.value();
                Class<?> responseType = responseType(proxy, method);
                Object request = requestArg(args);
                KeyValue metadata = metadata(op);
                return clientFlow.request(
                        codec,
                        request,
                        metadata,
                        responseType
                );
            }
            throw new IllegalStateException("Client methods should have Metadata annotation");
        }

        static KeyValue metadata(Op op) {
            KeyValue metadata = new KeyValue();
            metadata.put(Op.key(), op.value());
            return metadata;
        }

        static Object requestArg(Object[] args) {
            int count = args.length;
            if (count != 1) {
                throw wrongArgsCountError();
            }
            return args[0];
        }

        static Class<?> responseType(Object proxy, Method method) {
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;
                Type rawType = parameterizedType.getRawType();
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                if (!rawType.equals(Flowable.class)) {
                    throw wrongReturnTypeError(proxy, method);
                }
                if (typeArgs.length != 1) {
                    throw wrongReturnTypeError(proxy, method);
                }
                Class<?> responseType = (Class<?>) typeArgs[0];
                return responseType;

            }
            throw wrongReturnTypeError(proxy, method);
        }

        private static IllegalArgumentException wrongReturnTypeError(Object proxy,
                                                                     Method method) {
            return new IllegalArgumentException(String
                    .format("Class %s, method %s - expected return type is Flowable<T>",
                            proxy.getClass(),
                            method.getName()));
        }

        private static IllegalArgumentException wrongArgsCountError() {
            return new IllegalArgumentException("Method should have exactly 1 argument");
        }
    }
}
