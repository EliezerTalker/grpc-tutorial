package com.proto.server.deadline;

import io.grpc.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DeadlineInterceptor implements ClientInterceptor {


    //TODO: When the client calls a method on a stub, it will we intercepted by this method
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {

        //we check if the client set a time out and if he didnt then we set a time out of 10 seconds
        Deadline deadline = callOptions.getDeadline();

        if(Objects.isNull(deadline))
        {
            callOptions = callOptions.withDeadline(Deadline.after(10, TimeUnit.SECONDS));
        }

        return channel.newCall(methodDescriptor,callOptions);


            //TODO: this line means that we want to invoke the method just like the client did
        //return channel.newCall(methodDescriptor,callOptions);
    }
}
