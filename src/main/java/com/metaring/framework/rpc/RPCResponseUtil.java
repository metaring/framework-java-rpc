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

import com.metaring.framework.CoreType;
import com.metaring.framework.functionality.FunctionalityContext;
import com.metaring.framework.type.DataRepresentation;

public final class RPCResponseUtil {

    public static final void addToResponseData(FunctionalityContext functionalityContext, String name, CoreType coreType) {
        ((DataRepresentation) functionalityContext.getData().get(CallFunctionalityImpl.KEY_DATA)).add(name, coreType);
    }

    public static final void addToResponseData(FunctionalityContext functionalityContext, String name, Long digit) {
        ((DataRepresentation) functionalityContext.getData().get(CallFunctionalityImpl.KEY_DATA)).add(name, digit);
    }

    public static final void addToResponseData(FunctionalityContext functionalityContext, String name, Double realDigit) {
        ((DataRepresentation) functionalityContext.getData().get(CallFunctionalityImpl.KEY_DATA)).add(name, realDigit);
    }

    public static final void addToResponseData(FunctionalityContext functionalityContext, String name, Boolean b) {
        ((DataRepresentation) functionalityContext.getData().get(CallFunctionalityImpl.KEY_DATA)).add(name, b);
    }

    public static final void addToResponseData(FunctionalityContext functionalityContext, String name, String string) {
        ((DataRepresentation) functionalityContext.getData().get(CallFunctionalityImpl.KEY_DATA)).add(name, string);
    }

    public static final DataRepresentation buildResponseData(FunctionalityContext functionalityContext) {
        return (DataRepresentation) functionalityContext.getData().get(CallFunctionalityImpl.KEY_DATA);
    }
}