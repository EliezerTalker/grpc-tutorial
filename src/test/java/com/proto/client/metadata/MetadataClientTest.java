package com.proto.client.metadata;

import com.proto.client.rpctypes.BalanceStreamObserver;
import com.proto.client.rpctypes.MoneyStreamingResponse;
import com.proto.models.*;
import com.proto.server.deadline.DeadlineInterceptor;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    public void setup(){

        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken())) //TODO: we use grpc built in interceptor and give it our metadata to attach
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.bankServiceStub = BankServiceGrpc.newStub(managedChannel);
    }

    @Test
    public void balanceTest(){

        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(2)
                .build();

        for (int i = 0; i < 20; i++) {

            try {
                int random = ThreadLocalRandom.current().nextInt(1, 4);

                System.out.println("Random : " + random);

                Balance balance = this.blockingStub
                        .withCallCredentials(new UserSessionToken("user-secret-" + random + ":prime")) //TODO: we give the stub our implemention of how to attach the user token to the metadata of the method request
                        .getBalance(balanceCheckRequest);

                System.out.println("Received: " + balance);
            }catch (StatusRuntimeException e){

                e.printStackTrace();
            }
        }
    }



    @Test
    public void withdrawTest(){

        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(7).setAmount(40).build();

         this.blockingStub
                 .withDeadline(Deadline.after(2,TimeUnit.SECONDS))
                 .withdraw(withdrawRequest).forEachRemaining(money -> System.out.println("Received : " + money.getValue()));

    }


    @Test
    public void withdrawAsyncTest() throws InterruptedException {

        CountDownLatch  latch = new CountDownLatch(1);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(9).setAmount(40).build();

        this.bankServiceStub.withdraw(withdrawRequest, new MoneyStreamingResponse(latch));
        latch.await();

    }


    @Test
    public void cashStreamingRequest() throws InterruptedException {
        CountDownLatch  latch = new CountDownLatch(1);

        StreamObserver<DepositRequest> depositRequestStreamObserver = this.bankServiceStub.cashDeposit(new BalanceStreamObserver(latch));

        for (int i = 0; i <10 ; i++) {

            DepositRequest depositRequest = DepositRequest.newBuilder().setAccountNumber(8).setAmount(10).build();
            depositRequestStreamObserver.onNext(depositRequest);
        }

        depositRequestStreamObserver.onCompleted();
        latch.await();

    }
}
