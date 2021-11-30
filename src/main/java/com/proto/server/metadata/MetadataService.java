package com.proto.server.metadata;


import com.google.common.util.concurrent.Uninterruptibles;
import com.proto.models.*;
import com.proto.server.rpctypes.AccountDatabase;
import com.proto.server.rpctypes.CashStreamingRequest;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;


public class MetadataService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {

        int accountNumber = request.getAccountNumber();
        int amount = AccountDatabase.getBalance(accountNumber);

        //TODO: we can think of this like that: when we saved the user rule in the context under this key name and passed
        //TODO:   it to the service layer then it created a namespace for the context that we can access using the key name
        //TODO:  and get its value
        //TODO: and just like how if a thread stores a value locally then only he can retrieve it then
        //TODO: also only the rpc call that saved the context value in the namespace will be able to retrieve the
        //TODO: value of the key it was saved under
        UserRole userRole = ServerConstants.CTX_USER_RULE.get();

        amount = UserRole.PRIME.equals(userRole) ? amount : (amount - 15);

        System.out.println("Received the request for " + accountNumber);
        Balance balance = Balance.newBuilder()
                            .setAmount(amount)
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

        if(amount < 10 || (amount % 10) != 0){
            Metadata metadata = new Metadata();
            // here we make a key of type WithdrawalError
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder().setAmount(balance).setErrorMessage(ErrorMessage.ONLY_TEN_MULTIPLES).build();
            metadata.put(errorKey,withdrawalError);
            //TODO: this is how we return a status that had our custom error in the metadata
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));

            return;
        }


        if(balance < amount){
            Metadata metadata = new Metadata();
            // here we make a key of type WithdrawalError
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder().setAmount(balance).setErrorMessage(ErrorMessage.INSUFFICIENT_BALANCE).build();
            metadata.put(errorKey,withdrawalError);
            Status status = Status.FAILED_PRECONDITION.withDescription("No enough money. You only have " + balance);
            //TODO: this is how we return a pre-defined status error
           // responseObserver.onError(status.asRuntimeException());
            //TODO: this is how we return a status that had our custom error in the metadata
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));

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
















