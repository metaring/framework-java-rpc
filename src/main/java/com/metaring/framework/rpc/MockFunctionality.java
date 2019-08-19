package com.metaring.framework.rpc;

import java.util.concurrent.CompletableFuture;

import com.ea.async.Async;
import com.metaring.framework.auth.AuthModuleHelper;
import com.metaring.framework.functionality.AbstractFunctionality;
import com.metaring.framework.functionality.FunctionalitiesManager;
import com.metaring.framework.functionality.FunctionalityContext;
import com.metaring.framework.functionality.FunctionalityExecutionResult;
import com.metaring.framework.functionality.FunctionalityInfo;
import com.metaring.framework.type.DataRepresentation;

public class MockFunctionality extends AbstractFunctionality {

    private static final MockFunctionality INSTANCE = new MockFunctionality();

    private class MockInfo {
        public final FunctionalityInfo info;
        public final Object input;

        protected MockInfo(FunctionalityInfo info, Object input) {
            super();
            this.info = info;
            this.input = input;
        }
    }

    public static final CompletableFuture<DataRepresentation> run(FunctionalityInfo info, Object input) throws Exception {
        FunctionalityContext functionalityContext = new FunctionalityContext();
        functionalityContext.getData().put(CallFunctionalityImpl.KEY_ID, 0l);
        functionalityContext.getData().put(CallFunctionalityImpl.KEY_SOURCE_IP, "127.0.0.1");
        functionalityContext.getData().put(CallFunctionalityImpl.KEY_DATA, newDataRepresentation());
        functionalityContext.getData().put(AuthModuleHelper.STATELESS_REQUEST_KEY, false);
        FunctionalityExecutionResult result = Async.await(INSTANCE.execute(functionalityContext, INSTANCE.create(info, input)));
        result = FunctionalitiesManager.verifyAndReturnFunctionalityExecutionResult(result).as(FunctionalityExecutionResult.class);
        return CompletableFuture.completedFuture(result.getResult());
    }

    private MockFunctionality() {
        super(FunctionalityInfo.create("", true, false, false, MockInfo.class.getName().toLowerCase(), null), FunctionalityExecutionResult.class);
    }

    protected final MockInfo create(FunctionalityInfo info, Object input) {
        return new MockInfo(info, input);
    }

    @Override
    protected CompletableFuture<Void> beforePreConditionCheck(Object input) throws Exception {
        return end;
    }

    @Override
    protected CompletableFuture<Void> preConditionCheck(Object input) throws Exception {
        return end;
    }

    @Override
    protected CompletableFuture<Void> afterPreConditionCheck(Object input) throws Exception {
        return end;
    }

    @Override
    protected CompletableFuture<Void> beforeCall(Object input) throws Exception {
        return end;
    }

    @Override
    protected CompletableFuture<Object> call(Object input) throws Exception {
        MockInfo mockInfo = (MockInfo) input;
        FunctionalityExecutionResult result = Async.await(FunctionalitiesManager.executeFromJson(mockInfo.info.getFunctionalityFullyQualifiedName(), this, mockInfo.input == null ? "null" : dataRepresentationFromObject(mockInfo.input).toJson()));
        return end(result);
    }

    @Override
    protected CompletableFuture<Void> afterCall(Object input, Object output) throws Exception {
        return end;
    }

    @Override
    protected CompletableFuture<Void> beforePostConditionCheck(Object input, Object output) throws Exception {
        return end;
    }

    @Override
    protected CompletableFuture<Void> postConditionCheck(Object input, Object output) throws Exception {
        return end;
    }

    @Override
    protected CompletableFuture<Void> afterPostConditionCheck(Object input, Object output) throws Exception {
        return end;
    }
}