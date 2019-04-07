package com.metaring.framework.rpc;

import com.metaring.framework.functionality.FunctionalityExecutionResult;
import com.metaring.framework.Tools;
import com.metaring.framework.type.DataRepresentation;
import com.metaring.framework.GeneratedCoreType;

public class RpcResponse implements GeneratedCoreType {

    public static final String FULLY_QUALIFIED_NAME = "com.metaring.framework.rpc.rpcResponse";

    private Long id;
    private FunctionalityExecutionResult result;
    private DataRepresentation data;

    private RpcResponse(Long id, FunctionalityExecutionResult result, DataRepresentation data) {
        this.id = id;
        this.result = result;
        this.data = data;
    }

    public Long getId() {
        return this.id;
    }

    public FunctionalityExecutionResult getResult() {
        return this.result;
    }

    public DataRepresentation getData() {
        return this.data;
    }

    public static RpcResponse create(Long id, FunctionalityExecutionResult result, DataRepresentation data) {
        return new RpcResponse(id, result, data);
    }

    public static RpcResponse fromJson(String jsonString) {

        if(jsonString == null) {
            return null;
        }

        jsonString = jsonString.trim();
        if(jsonString.isEmpty()) {
            return null;
        }

        if(jsonString.equalsIgnoreCase("null")) {
            return null;
        }

        DataRepresentation dataRepresentation = Tools.FACTORY_DATA_REPRESENTATION.fromJson(jsonString);

        Long id = null;
        if(dataRepresentation.hasProperty("id")) {
            try {
                id = dataRepresentation.getDigit("id");
            } catch (Exception e) {
            }
        }

        FunctionalityExecutionResult result = null;
        if(dataRepresentation.hasProperty("result")) {
            try {
                result = dataRepresentation.get("result", FunctionalityExecutionResult.class);
            } catch (Exception e) {
            }
        }

        DataRepresentation data = null;
        if(dataRepresentation.hasProperty("data")) {
            try {
                data = dataRepresentation.get("data");
            } catch (Exception e) {
            }
        }

        RpcResponse rpcResponse = create(id, result, data);
        return rpcResponse;
    }

    public DataRepresentation toDataRepresentation() {
        DataRepresentation dataRepresentation = Tools.FACTORY_DATA_REPRESENTATION.create();
        if (id != null) {
            dataRepresentation.add("id", id);
        }

        if (result != null) {
            dataRepresentation.add("result", result);
        }

        if (data != null) {
            dataRepresentation.add("data", data);
        }

        return dataRepresentation;
    }

    @Override
    public String toJson() {
        return toDataRepresentation().toJson();
    }

    @Override
    public String toString() {
        return this.toJson();
    }
}