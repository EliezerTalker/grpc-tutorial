package com.proto.server.metadata;

import io.grpc.*;
import org.omg.CORBA.Object;

import java.util.Objects;

public class AuthInterceptor implements ServerInterceptor {

    /*

    usr-secret-3:prime
    usr-secret-2:regular


    */


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        String clientToken = metadata.get(ServerConstants.TOKEN);
        String userToken = metadata.get(ServerConstants.USER_TOKEN);

        if(this.validateClientToken(clientToken) && this.validateUserToken(userToken)){

            UserRole userRole = this.extractUserRole(userToken);

            // if the client gave the right token we allow to invoke the method he wanted
            return serverCallHandler.startCall(serverCall,metadata);

        }else{

            Status status = Status.UNAUTHENTICATED.withDescription("invalid token/expired token");
            //if the client gave an invalid token then we close the connection
            serverCall.close(status,metadata);
        }

        // according to the grpc docs, also if the client gave an invalid token and we close the connection, we still must return a ServerCall.Listener so we return a dummy one(with no implementaion)
        return new ServerCall.Listener<ReqT>() {
        };

    }


    private  boolean validateClientToken(String token){

        return Objects.nonNull(token) && token.equals("bank-client-secret");

    }

    private  boolean validateUserToken(String token){

        return Objects.nonNull(token) && (token.startsWith("user-secret-3") ||token.startsWith("user-secret-2") );

    }


    private UserRole extractUserRole(String jwt){

        return jwt.endsWith("prime") ? UserRole.PRIME : UserRole.STANDARD;

    }
}
