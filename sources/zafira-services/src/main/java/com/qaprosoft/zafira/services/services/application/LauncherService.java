/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package com.qaprosoft.zafira.services.services.application;

import com.qaprosoft.zafira.dbaccess.dao.mysql.application.LauncherMapper;
import com.qaprosoft.zafira.models.db.Launcher;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LauncherService {

    @Autowired
    private LauncherMapper launcherMapper;

    @Transactional(rollbackFor = Exception.class)
    public Launcher createLauncher(Launcher launcher) throws ServiceException {
        launcherMapper.createLauncher(launcher);
        return launcher;
    }

    @Transactional(readOnly = true)
    public Launcher getLauncherById(Long id) throws ServiceException {
        return launcherMapper.getLauncherById(id);
    }

    @Transactional(readOnly = true)
    public List<Launcher> getAllLaunchers() throws ServiceException {
        return launcherMapper.getAllLaunchers();
    }

    @Transactional(rollbackFor = Exception.class)
    public Launcher updateLauncher(Launcher launcher) throws ServiceException {
        launcherMapper.updateLauncher(launcher);
        return launcher;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLauncherById(Long id) throws ServiceException {
        launcherMapper.deleteLauncherById(id);
    }
}
