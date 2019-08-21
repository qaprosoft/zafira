/*******************************************************************************
 * Copyright 2013-2019 Qaprosoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.zafira.ws.util.dozer;

import org.dozer.DozerConverter;

import java.util.Date;

import static com.qaprosoft.zafira.services.util.DateTimeUtil.calculateDurationFromDate;
import static com.qaprosoft.zafira.services.util.DateTimeUtil.calculateDurationToDate;

/**
 * ExpirationDateConverter - converts expiresIn seconds to expidationDate.
 * 
 * @author akhursevich
 */
public class ExpirationDateConverter extends DozerConverter<Integer, Date> {

    public ExpirationDateConverter() {
        super(Integer.class, Date.class);
    }

    @Override
    public Integer convertFrom(Date source, Integer destination) {
        return source != null ? calculateDurationFromDate(source) : null;
    }

    @Override
    public Date convertTo(Integer source, Date destination) {
        return source != null ? calculateDurationToDate(source) : null;
    }
}
