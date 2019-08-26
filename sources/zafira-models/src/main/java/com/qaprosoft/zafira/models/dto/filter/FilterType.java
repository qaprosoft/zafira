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
 ******************************************************************************/
package com.qaprosoft.zafira.models.dto.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaprosoft.zafira.models.db.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Getter
@Setter
public class FilterType extends AbstractEntity {

    private static final long serialVersionUID = -2497558955789794119L;

    @NotNull(message = "Name required")
    private String name;
    private String description;
    @Valid
    private Subject subject;
    private Long userId;
    private boolean publicAccess;

    public void setSubjectFromString(String subject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.subject = mapper.readValue(subject, Subject.class);
    }

}
