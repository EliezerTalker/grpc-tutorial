package com.proto.client.deadline;

import com.proto.client.rpctypes.BalanceStreamObserver;
import com.proto.client.rpctypes.MoneyStreamingResponse;
import com.proto.models.*;
import com.proto.server.deadline.DeadlineInterceptor;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Time;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeadlineClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    public void setup(){

        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .intercept(new DeadlineInterceptor()) //TODO: we tell the channel to use our interceptor implement
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
        Balance balance = this.blockingStub
                .getBalance(balanceCheckRequest);

        System.out.println("Received: " + balance);

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
