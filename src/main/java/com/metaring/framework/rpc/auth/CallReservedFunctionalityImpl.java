/**
 *    Copyright 2019 MetaRing s.r.l.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.metaring.framework.rpc.auth;

import java.util.concurrent.CompletableFuture;

import com.ea.async.Async;
import com.metaring.framework.auth.InternalFunctionalityAccessException;
import com.metaring.framework.functionality.FunctionalitiesInfoProvider;
import com.metaring.framework.functionality.FunctionalitiesManager;
import com.metaring.framework.functionality.FunctionalityInfo;
import com.metaring.framework.rpc.RestrictedFunctionalityAccessException;
import com.metaring.framework.rpc.RpcRequest;
import com.metaring.framework.rpc.RpcResponse;

class CallReservedFunctionalityImpl extends CallReservedFunctionality {

    private static final String CALL_RESTRICTED_FUNCTIONALITY_FULLY_QUALIFIED_NAME = AuthFunctionalitiesManager.CALL_RESTRICTED.getFunctionalityFullyQualifiedName();

    @Override
    protected CompletableFuture<Void> preConditionCheck(RpcRequest input) throws Exception {
        if (RpcAuthHelper.getIdentificationData(this.getContext()) != null) {
            throw new IdentificationDataAlreadyExistsException(this.getContext());
        }
        if (input == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest");
        }
        if (input.getData() == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest.data");
        }
        if (input.getName() == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest.name");
        }
        String functionalityName = input.getName();
        FunctionalityInfo functionalityInfo = FunctionalitiesInfoProvider.get(functionalityName);
        if (functionalityInfo == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "functionalityInfo");
        }
        if (functionalityInfo.isRestricted() && !functionalityName.equalsIgnoreCase(CALL_RESTRICTED_FUNCTIONALITY_FULLY_QUALIFIED_NAME)) {
            throw new RestrictedFunctionalityAccessException(this.getContext().getStackElementSeries(), functionalityName);
        }
        if (functionalityInfo.isInternal() && !functionalityName.equalsIgnoreCase(CALL_RESTRICTED_FUNCTIONALITY_FULLY_QUALIFIED_NAME)) {
            throw new InternalFunctionalityAccessException(this.getContext().getStackElementSeries(), functionalityName);
        }
        Boolean result = Async.await(com.metaring.framework.auth.AuthFunctionalitiesManager.verifyIdentification(input.getData()));
        if (!(result != null && result)) {
            throw new IdentificationVerificationFailedException();
        }
        return end;
    }

    @Override
    protected CompletableFuture<Void> afterPreConditionCheck(RpcRequest input) throws Exception {
        RpcAuthHelper.putIdentificationData(this.getContext(), input.getData());
        return end;
    }

    @Override
    protected CompletableFuture<RpcResponse> call(RpcRequest input) throws Exception {
        return end(RpcResponse.create(input.getId(), Async.await(FunctionalitiesManager.executeFromJson(input.getName(), this, input.getParam() == null ? "null" : input.getParam().toJson())), null));
    }

    @Override
    protected CompletableFuture<Void> postConditionCheck(RpcRequest input, RpcResponse rpcResponse) throws Exception {
        return end;
    }
}
