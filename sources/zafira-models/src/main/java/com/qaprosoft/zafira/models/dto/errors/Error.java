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
package com.qaprosoft.zafira.models.dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Error {

    @JsonInclude(Include.NON_EMPTY)
    private String field;
    @JsonInclude(Include.NON_EMPTY)
    private String message;
    @JsonInclude(Include.NON_NULL)
    private ErrorCode code;
    @JsonInclude(Include.NON_NULL)
    private AdditionalErrorData additional;

    public Error(ErrorCode code) {
        this.code = code;
    }

    public Error(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public Error(ErrorCode code, String field, String message) {
        this.code = code;
        this.field = field;
        this.message = message;
    }

}
