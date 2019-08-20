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
package com.qaprosoft.zafira.services.services.application;

import com.qaprosoft.zafira.dbaccess.dao.mysql.application.TagMapper;
import com.qaprosoft.zafira.models.db.Tag;
import com.qaprosoft.zafira.models.db.TestInfo;
import com.qaprosoft.zafira.models.dto.tag.IntegrationTag;
import com.qaprosoft.zafira.models.dto.tag.IntegrationDataType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    @Transactional(rollbackFor = Exception.class)
    public Tag createTag(Tag tag) {
        tagMapper.createTag(tag);
        if (tag.getId() == null || tag.getId() == 0) {
            Tag existsTag = getTagByNameAndValue(tag.getName(), tag.getValue());
            if (existsTag != null) {
                tag = existsTag;
            }
        }
        return tag;
    }

    @Transactional(rollbackFor = Exception.class)
    public Set<Tag> createTags(Set<Tag> tags) {
        Set<Tag> result = new HashSet<>();
        if (tags != null && !tags.isEmpty()) {
            result = tags.stream().map(this::createTag).collect(Collectors.toSet());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TestInfo> getTestInfoByTagNameAndTestRunCiRunId(IntegrationTag name, String ciRunId) {
        return tagMapper.getTestInfoByTagNameAndTestRunCiRunId(name, ciRunId);
    }

    @Transactional(readOnly = true)
    public Tag getTagByNameAndValue(String name, String value) {
        return tagMapper.getTagByNameAndValue(name, value);
    }

    @Transactional(readOnly = true)
    public void setTestInfoByIntegrationTag(String ciRunId, IntegrationTag integrationTag, IntegrationDataType integrationDataType) {
        List<TestInfo> testInfo = getTestInfoByTagNameAndTestRunCiRunId(integrationTag, ciRunId);
        integrationDataType.setTestInfo(testInfo);
    }

}
