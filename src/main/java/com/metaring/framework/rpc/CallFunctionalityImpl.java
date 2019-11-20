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

package com.metaring.framework.rpc;

import static com.ea.async.Async.await;
import static com.metaring.framework.crypto.CryptoFunctionalitiesManager.encrypt;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.metaring.framework.Core;
import com.metaring.framework.Tools;
import com.metaring.framework.auth.AuthModuleHelper;
import com.metaring.framework.auth.InternalFunctionalityAccessException;
import com.metaring.framework.email.BugReportMail;
import com.metaring.framework.exception.ManagedException;
import com.metaring.framework.functionality.FunctionalitiesInfoProvider;
import com.metaring.framework.functionality.FunctionalitiesManager;
import com.metaring.framework.functionality.FunctionalitiesProvider;
import com.metaring.framework.functionality.FunctionalityContext;
import com.metaring.framework.functionality.FunctionalityCreationException;
import com.metaring.framework.functionality.FunctionalityExecutionResult;
import com.metaring.framework.functionality.FunctionalityExecutionVerdictEnumerator;
import com.metaring.framework.functionality.FunctionalityExecutionWarningData;
import com.metaring.framework.functionality.FunctionalityExecutionWarningDataSeries;
import com.metaring.framework.functionality.FunctionalityInfo;
import com.metaring.framework.functionality.UnmanagedException;
import com.metaring.framework.rpc.auth.AuthFunctionalitiesManager;
import com.metaring.framework.rpc.auth.MissingRpcValueException;
import com.metaring.framework.type.DataRepresentation;
import com.metaring.framework.util.StringUtil;

public class CallFunctionalityImpl extends CallFunctionality {

    private static final String CALL_RESERVED_FUNCTIONALITY_NAME = AuthFunctionalitiesManager.CALL_RESERVED.getFunctionalityFullyQualifiedName();

    public static final String KEY_ID = "functionality.id";
    public static final String KEY_SOURCE_IP = "functionality.ip";
    public static final String KEY_DATA = "functionality.data";

    public static final CompletableFuture<FunctionalityExecutionResult> execute(Long id, String sourceIp, String broadcastKeyName, String broadcastKey, boolean stateless, String rpcRequest) {
        FunctionalityContext functionalityContext = new FunctionalityContext();
        functionalityContext.getData().put(CallFunctionalityImpl.KEY_ID, id);
        functionalityContext.getData().put(CallFunctionalityImpl.KEY_SOURCE_IP, sourceIp);
        functionalityContext.getData().put(CallFunctionalityImpl.KEY_DATA, Tools.FACTORY_DATA_REPRESENTATION.create());
        functionalityContext.getData().put(AuthModuleHelper.STATELESS_REQUEST_KEY, stateless);
        if(!StringUtil.isNullOrEmpty(broadcastKey)) {
            functionalityContext.getData().put(broadcastKeyName, broadcastKey);
        }
        try {
            return ((CallFunctionalityImpl) FunctionalitiesProvider.getFunctionality(RpcFunctionalitiesManager.CALL, CallFunctionality.class)).execute(functionalityContext, RpcRequest.fromJson(rpcRequest));
        } catch (FunctionalityCreationException e) {
            return null;
        }
    }

    public final CompletableFuture<FunctionalityExecutionResult> execute(FunctionalityContext context, RpcRequest reuest) {
        return super.execute(context, reuest);
    }

    @Override
    protected final CompletableFuture<Void> preConditionCheck(RpcRequest rpcRequest) throws Exception {
        if (rpcRequest == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest");
        }
        if (rpcRequest.getId() == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest.id");
        }
        if (rpcRequest.getName() == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "rpcRequest.name");
        }

        String functionalityName = rpcRequest.getName();
        FunctionalityInfo functionalityInfo = FunctionalitiesInfoProvider.get((String) functionalityName);
        if (functionalityInfo == null) {
            throw new MissingRpcValueException(this.getContext().getStackElementSeries(), "functionalityInfo");
        }
        if (functionalityInfo.isInternal() && !functionalityName.equals(CALL_RESERVED_FUNCTIONALITY_NAME)) {
            throw new InternalFunctionalityAccessException(this.getContext().getStackElementSeries(), functionalityName);
        }
        if (functionalityInfo.isReserved()) {
            throw new ReserverdFunctionalityAccessException(this.getContext().getStackElementSeries(), functionalityName);
        }
        if (functionalityInfo.isRestricted()) {
            throw new RestrictedFunctionalityAccessException(this.getContext().getStackElementSeries(), functionalityName);
        }
        return end;
    }

    @Override
    protected final CompletableFuture<RpcResponse> call(RpcRequest rpcRequest) throws Exception {
        final Executor executor = EXECUTOR;
        final CompletableFuture<RpcResponse> call = new CompletableFuture<>();
        String jsonParam = rpcRequest.getParam()  == null ? "null" : rpcRequest.getParam().toJson();
        FunctionalitiesManager.executeFromJson(rpcRequest.getName(), this, jsonParam).whenCompleteAsync((result, error) -> {
            if(error != null) {
                call.completeExceptionally(error);
                return;
            }
            mergeResultAndExit(rpcRequest, result).whenCompleteAsync((r, e) -> {
                if(e != null) {
                    call.completeExceptionally(e);
                    return;
                }
                call.complete(r);
            }, executor);
        }, executor);
        return call;
    }

    @Override
    protected CompletableFuture<Void> postConditionCheck(RpcRequest input, RpcResponse output) throws Exception {
        return end;
    }

    private CompletableFuture<RpcResponse> mergeResultAndExit(RpcRequest input, FunctionalityExecutionResult functionalityExecutionResult) {
        DataRepresentation returnData = await(tryPrintError(input, functionalityExecutionResult));
        FunctionalityExecutionResult result = functionalityExecutionResult;
        FunctionalityExecutionVerdictEnumerator verdict = FunctionalityExecutionVerdictEnumerator.SUCCESS;
        LinkedList<FunctionalityExecutionWarningData> warningsList = new LinkedList<FunctionalityExecutionWarningData>();
        if (result != null) {
            while (result.getVerdict() != FunctionalityExecutionVerdictEnumerator.FAULT) {
                verdict = result.getVerdict();
                this.fillWarningDatas(result, warningsList);
                DataRepresentation r = result.getResult();
                if (r == null || r.isNull()) {
                    break;
                }
                if (result.getResultIdentificator().equals(FunctionalityExecutionResult.class.getName())) {
                    result = r.as(FunctionalityExecutionResult.class);
                    continue;
                }
                if (!result.getResultIdentificator().equals(RpcResponse.class.getName())) {
                    break;
                }
                result = r.as(RpcResponse.class).getResult();
            }
            if (result.getVerdict() == FunctionalityExecutionVerdictEnumerator.FAULT) {
                verdict = result.getVerdict();
                this.fillWarningDatas(result, warningsList);
            }
            result = FunctionalityExecutionResult.create(verdict, (warningsList == null || warningsList.isEmpty() ? null : FunctionalityExecutionWarningDataSeries.create(warningsList.toArray(new FunctionalityExecutionWarningData[warningsList.size()]))), result.getErrorData(), result.getResultIdentificator(), result.getResult());
        }
        return CompletableFuture.completedFuture(RpcResponse.create(input.getId(), result, returnData));
    }

    private final void fillWarningDatas(FunctionalityExecutionResult result, LinkedList<FunctionalityExecutionWarningData> warningsList) {
        if (result != null && result.getWarningData() != null) {
            FunctionalityExecutionWarningDataSeries series = result.getWarningData();
            int size = series.size();
            for (int i = 0; i < size; ++i) {
                warningsList.addLast(series.get(i));
            }
        }
    }

    private final CompletableFuture<DataRepresentation> tryPrintError(RpcRequest input, FunctionalityExecutionResult functionalityExecutionResult) {
        DataRepresentation responseData = getContextData(CallFunctionalityImpl.KEY_DATA);
        try {
            FunctionalitiesManager.verifyAndReturnFunctionalityExecutionResult(functionalityExecutionResult);
        } catch (Exception e) {
            if (!(e instanceof ManagedException)) {
                responseData = null;
                String stackTrace = cleanStackTrace(e);
                System.err.println("UNMANAGED EXCEPTION:\n\n" + stackTrace + "\n");
                try {
                    if (!Core.SYSKB.get("email").get("supportTeam").isEmpty()) {
                        BugReportMail.send(getContextData(KEY_ID), await(encrypt(null, getContextData(KEY_SOURCE_IP))), await(encrypt(null, input.toJson())), functionalityExecutionResult, stackTrace);
                    }
                } catch (Exception exx) {
                    exx.printStackTrace();
                }
            }
        }
        return end(responseData);
    }

    private final String cleanStackTrace(Exception e) {
        String stackTrace = StringUtil.fromThrowableToString(e);
        return stackTrace.substring(stackTrace.indexOf(UnmanagedException.EXCEPTION_START), stackTrace.indexOf(UnmanagedException.EXCEPTION_END));
    }

    public CallFunctionality setParameter(String key, Object value) {
        setContextData(key, value);
        return this;
    }
}