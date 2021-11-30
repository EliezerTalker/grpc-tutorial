package com.proto.client.rpctypes;

import com.proto.client.metadata.ClientConstants;
import com.proto.models.Money;
import com.proto.models.WithdrawalError;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class MoneyStreamingResponse implements StreamObserver<Money> {

    private CountDownLatch latch;

    public MoneyStreamingResponse(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(Money money) {

        System.out.println("Received async: " + money.getValue());
    }

    @Override
    public void onError(Throwable throwable) {

        // TODO: this "Status.trailersFromThrowable(throwable)" takes the exception that was thrown and extracts
        // TODO:from it the metadata we attached to it in the server side (MetadataService -> withdraw() )
        Metadata metadata = Status.trailersFromThrowable(throwable);

        // we get the withdrawalError we put in the metadata at the server side
        WithdrawalError withdrawalError = metadata.get(ClientConstants.WITHDRAWAL_ERROR_KEY);

        System.out.println(
                withdrawalError.getAmount() + " : " + withdrawalError.getErrorMessage()

        );
        latch.countDown();
    }

    @Override
    public void onCompleted() {

        System.out.println("Server is done!!");
        latch.countDown();

    }
}
