package com.metaring.framework.rpc;

import com.metaring.framework.functionality.FunctionalityInfo;
import com.metaring.framework.functionality.FunctionalitiesManager;
import com.metaring.framework.functionality.GeneratedFunctionalitiesManager;
import com.metaring.framework.functionality.Functionality;
import java.util.concurrent.CompletableFuture;
import com.metaring.framework.rpc.RpcRequest;
import com.metaring.framework.rpc.RpcResponse;

public class RpcFunctionalitiesManager extends FunctionalitiesManager implements GeneratedFunctionalitiesManager {

    public static final FunctionalityInfo CALL = FunctionalityInfo.create("com.metaring.framework.rpc.call", true, false, false, "com.metaring.framework.rpc.RpcRequest", "com.metaring.framework.rpc.RpcResponse");

    public static final CompletableFuture<RpcResponse> call(RpcRequest rpcRequest) {
        return call(CALL, CallFunctionality.class, getCallingFunctionality(), rpcRequest, result -> result.as(RpcResponse.class));
    }

    public static final CompletableFuture<RpcResponse> call(Functionality functionality, RpcRequest rpcRequest) {
        return call(CALL, CallFunctionality.class, functionality, rpcRequest, result -> result.as(RpcResponse.class));
    }

    public static final CompletableFuture<RpcResponse> callFromJson(String rpcRequestJson) {
        return callFromJson(CALL, CallFunctionality.class, getCallingFunctionality(), rpcRequestJson, result -> result.as(RpcResponse.class));
    }

    public static final CompletableFuture<RpcResponse> callFromJson(Functionality callingFunctionality, String rpcRequestJson) {
        return callFromJson(CALL, CallFunctionality.class, callingFunctionality, rpcRequestJson, result -> result.as(RpcResponse.class));
    }

}
