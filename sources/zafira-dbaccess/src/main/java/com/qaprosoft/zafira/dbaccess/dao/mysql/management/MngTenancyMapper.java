/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.zafira.dbaccess.dao.mysql.management;

import com.qaprosoft.zafira.models.db.Tenancy;

import java.util.List;

public interface MngTenancyMapper {

    void createTenancy(Tenancy tenancy);

    Tenancy getTenancyById(Long id);

    Tenancy getTenancyByName(String name);

    List<Tenancy> getAllTenancies();

    void updateTenancy(Tenancy tenancy);

    void deleteTenancyById(Long id);

    void deleteTenancyByName(String name);
}
