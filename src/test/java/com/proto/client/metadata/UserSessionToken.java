package com.proto.client.metadata;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.CallCredentials;
import io.grpc.Metadata;

import javax.jws.soap.SOAPBinding;
import java.util.concurrent.Executor;

public class UserSessionToken  extends CallCredentials {


    private String jwt;

    public UserSessionToken(String jwt){

        this.jwt = jwt;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {

        // we use the execute in order to run the code that we wrote inside the function faster
        executor.execute(()->{

            Metadata metadata = new Metadata();
            metadata.put(ClientConstants.USER_TOKEN, this.jwt);
            metadataApplier.apply(metadata);


        });
    }

    @Override
    public void thisUsesUnstableApi() {

    }
}
