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

import com.metaring.framework.GeneratedCoreType;
import com.metaring.framework.auth.AuthModuleHelper;
import com.metaring.framework.functionality.FunctionalityContext;
import com.metaring.framework.type.DataRepresentation;

class RpcAuthHelper {

    private static final String IDENTIFICATION_DATA = "IDENTIFICATION_DATA";
    private static final String ENABLE_DATA = "ENABLE_DATA";

    protected static final void putIdentificationData(FunctionalityContext functionalityContext, DataRepresentation identificationData) throws Exception {
        functionalityContext.getData().put(IDENTIFICATION_DATA, identificationData.as(AuthModuleHelper.IDENTIFICATION_DATA_CLASS));
    }

    public static final GeneratedCoreType getIdentificationData(FunctionalityContext functionalityContext) {
        return (GeneratedCoreType) functionalityContext.getData().get(IDENTIFICATION_DATA);
    }

    protected static final void putEnableData(FunctionalityContext functionalityContext, DataRepresentation enableData) throws Exception {
        functionalityContext.getData().put(ENABLE_DATA, enableData.as(AuthModuleHelper.ENABLE_DATA_CLASS));
    }

    public static final GeneratedCoreType getEnableData(FunctionalityContext functionalityContext) {
        return (GeneratedCoreType) functionalityContext.getData().get(ENABLE_DATA);
    }
}
