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
import com.metaring.framework.auth.AuthFunctionalitiesManager;
import com.metaring.framework.auth.AuthModuleHelper;
import com.metaring.framework.auth.InternalFunctionalityAccessException;
import com.metaring.framework.auth.PreliminaryEnableData;
import com.metaring.framework.functionality.CriticalMissingValuesException;
import com.metaring.framework.functionality.FunctionalitiesInfoProvider;
import com.metaring.framework.functionality.FunctionalitiesManager;
import com.metaring.framework.functionality.FunctionalityInfo;
import com.metaring.framework.rpc.RpcRequest;
import com.metaring.framework.rpc.RpcResponse;
import com.metaring.framework.type.DataRepresentation;

class CallRestrictedFunctionalityImpl extends CallRestrictedFunctionality {

    @Override
    protected final CompletableFuture<Void> preConditionCheck(RpcRequest input) throws Exception {
        if (RpcAuthHelper.getIdentificationData(this.getContext()) == null) {
            throw new NoValidIdentificationDataProvidedException(this.getContext().getStackElementSeries());
        }
        if (RpcAuthHelper.getEnableData(this.getContext()) != null) {
            throw new EnableDataAlreadyExistsException(this.getContext());
        }
        if (input == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest.enableData");
        }
        DataRepresentation enableData = input.getData();
        if (enableData == null || enableData.isNull()) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest.enableData.enableData");
        }
        String functionalityName = input.getName();
        if (functionalityName == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest.enableData.name");
        }
        FunctionalityInfo functionalityInfo = FunctionalitiesInfoProvider.get(functionalityName);
        if (functionalityInfo == null) {
            throw new CriticalMissingValuesException(this.getContext().getStackElementSeries(), "functionalityInfo");
        }
        if (functionalityInfo.isInternal()) {
            throw new InternalFunctionalityAccessException(this.getContext().getStackElementSeries(), functionalityName);
        }
        Boolean result = Async.await(AuthFunctionalitiesManager.verifyEnable(input.toDataRepresentation().add(AuthModuleHelper.FUNCTIONALITY_NAME, functionalityName).add(AuthModuleHelper.FUNCTIONALITY_PARAM, input.getParam()).as(PreliminaryEnableData.class)));
        if (!(result != null && result)) {
            throw new EnableVerificationFailedException();
        }
        return end;
    }

    @Override
    protected CompletableFuture<Void> afterPreConditionCheck(RpcRequest input) throws Exception {
        RpcAuthHelper.putEnableData(this.getContext(), input.getData());
        return end;
    }

    @Override
    protected final CompletableFuture<RpcResponse> call(RpcRequest input) throws Exception {
        return CompletableFuture.completedFuture(RpcResponse.create(input.getId(), Async.await(FunctionalitiesManager.executeFromJson(input.getName(), this, input.getParam() == null ? "null" : input.getParam().toJson())), null));
    }

    @Override
    protected final CompletableFuture<Void> postConditionCheck(RpcRequest input, RpcResponse rpcResponse) throws Exception {
        return end;
    }
}