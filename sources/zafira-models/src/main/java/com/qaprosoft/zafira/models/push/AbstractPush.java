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
package com.qaprosoft.zafira.models.push;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AbstractPush {

    private Type type;
    private String uid;

    public AbstractPush(Type type) {
        this.type = type;
        this.uid = UUID.randomUUID().toString();
    }

    public enum Type {
        TEST_RUN("/topic/%s.testRuns"),
        TEST("/topic/%s.testRuns.%s.tests"),
        TEST_RUN_STATISTICS("/topic/%s.statistics"),
        LAUNCHER("/topic/%s.launchers"),
        LAUNCHER_RUN("/topic/%s.launcherRuns");

        private final String websocketPathTemplate;

        Type(String websocketPathTemplate) {
            this.websocketPathTemplate = websocketPathTemplate;
        }

        public String getWebsocketPathTemplate() {
            return websocketPathTemplate;
        }

        public String buildWebsocketPath(Object... parameters) {
            return String.format(getWebsocketPathTemplate(), parameters);
        }

    }

}
