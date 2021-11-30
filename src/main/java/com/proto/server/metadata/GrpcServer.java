package com.proto.server.metadata;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(6565)
               // .intercept(new ContextAuthInterceptor())// TODO: we give her our implemention for the AuthInterceptor (ServerInterceptor)
                .addService(new MetadataService())
                .build();

        server.start();
        server.awaitTermination();

    }
}
