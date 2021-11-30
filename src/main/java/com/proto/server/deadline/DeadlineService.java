package com.proto.server.deadline;


import com.google.common.util.concurrent.Uninterruptibles;
import com.proto.models.*;
import com.proto.server.rpctypes.AccountDatabase;
import com.proto.server.rpctypes.CashStreamingRequest;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;


public class DeadlineService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {

        int accountNumber = request.getAccountNumber();

        System.out.println("Received the request for " + accountNumber);
        Balance balance = Balance.newBuilder()
                            .setAmount(AccountDatabase.getBalance(accountNumber))
                            .build();

        //simulate time-consuming call
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);

        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {


        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount();
        int balance = AccountDatabase.getBalance(accountNumber);

        if(balance < amount){

            Status status = Status.FAILED_PRECONDITION.withDescription("No enough money. You only have " + balance);
            responseObserver.onError(status.asRuntimeException());
            return;
        }


        for ( int i=0; i<(amount/10); i++){

            Money money = Money.newBuilder().setValue(10).build();
            //simulate time-consuming call
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            if(!Context.current().isCancelled()) {

                responseObserver.onNext(money);
                System.out.println("Delivered 10$");
                AccountDatabase.deductBalance(accountNumber, 10);
            }
            else{
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Completed");
        responseObserver.onCompleted();

    }


    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {

        return new CashStreamingRequest(responseObserver);

    }
}
















