package com.proto.server.rpctypes;

import com.proto.server.loadbalancing.BankService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(6535)
                .addService(new BankService())
                .addService(new TransferService())
                .build();

        server.start();
        server.awaitTermination();

    }
}
