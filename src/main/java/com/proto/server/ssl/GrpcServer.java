package com.proto.server.ssl;

import com.proto.server.loadbalancing.BankService;
import com.proto.server.rpctypes.TransferService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
import java.io.IOException;

public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {


        SslContext sslContext = GrpcSslContexts.configure(

                SslContextBuilder.forServer(

                        new File("src/main/java/com/proto/server/ssl/ssl-tls/localhost.crt"),
                        new File("src/main/java/com/proto/server/ssl/ssl-tls/localhost.pem")

                )
        ).build();



        Server server = NettyServerBuilder.forPort(6565)
                .sslContext(sslContext)
                .addService(new BankService())
                .build();

        server.start();
        server.awaitTermination();

    }
}
