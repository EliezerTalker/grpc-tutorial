package com.proto.server.metadata;

import io.grpc.Context;
import io.grpc.Metadata;

public class ServerConstants {

    // we are making a key of type string named "client-token"
    public static final Metadata.Key<String> TOKEN = Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER);


    public static final Metadata.Key<String> USER_TOKEN = Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<UserRole> CTX_USER_RULE = Context.key("user-rule");
    public static final Context.Key<UserRole> CTX_USER_RULE1 = Context.key("user-rule");

}
