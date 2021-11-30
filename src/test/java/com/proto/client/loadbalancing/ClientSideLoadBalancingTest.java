package com.proto.client.loadbalancing;

import com.proto.models.Balance;
import com.proto.models.BalanceCheckRequest;
import com.proto.models.BankServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientSideLoadBalancingTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;


    @BeforeAll
    public void setup(){
        List<String> instances = new ArrayList<>();
        instances.add("localhost:6535");
        instances.add("localhost:7575");
        ServiceRegistry.register("bank-service", instances);
        NameResolverRegistry.getDefaultRegistry().register(new TempNameResolverProvider());

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forTarget("http://bank-service") //TODO: we give the service name and then when we ask for this service grpc will will direct us to "TempNameResolverProvider" which in the end will give us an address for the service
                .defaultLoadBalancingPolicy("round_robin") // TODO: this is the way grpc will decide which address to send us to
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.bankServiceStub = BankServiceGrpc.newStub(managedChannel);

    }

    @Test
    public void balanceTest(){

        for (int i = 0; i < 100; i++) {


            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(ThreadLocalRandom.current().nextInt(1,11))
                    .build();
            Balance balance = this.blockingStub.getBalance(balanceCheckRequest);

            System.out.println("Received: " + balance.getAmount());
        }
    }


}
