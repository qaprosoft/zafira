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
package com.qaprosoft.zafira.models.dto;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Getter
public class CertificationType {

    private Set<String> platforms = new TreeSet<>();
    private Set<String> steps = new TreeSet<>();
    private Map<String, Map<String, String>> screenshots = new HashMap<>();

    public void addScreenshot(String step, String platform, String url) {
        if (step == null || step.isEmpty()) {
            return;
        }

        platforms.add(platform);
        steps.add(step);
        if (!screenshots.containsKey(platform)) {
            screenshots.put(platform, new HashMap<>());
        }
        screenshots.get(platform).put(step, url);
    }

}
