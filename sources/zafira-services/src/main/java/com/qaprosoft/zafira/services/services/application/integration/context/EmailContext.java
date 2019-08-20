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
package com.qaprosoft.zafira.services.services.application.integration.context;

import com.qaprosoft.zafira.models.db.Setting;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

public class EmailContext extends AbstractContext {

    private JavaMailSender javaMailSender;
    private String fromAddress;
    private Boolean isConnected;

    public EmailContext(Map<Setting.SettingType, String> settings) {
        super(settings, settings.get(Setting.SettingType.EMAIL_ENABLED));

        String host = settings.get(Setting.SettingType.EMAIL_HOST);
        int port = Integer.parseInt(settings.get(Setting.SettingType.EMAIL_PORT));
        String user = settings.get(Setting.SettingType.EMAIL_USER);
        String password = settings.get(Setting.SettingType.EMAIL_PASSWORD);
        String fromAddress = settings.get(Setting.SettingType.EMAIL_FROM_ADDRESS);

        this.javaMailSender = new JavaMailSenderImpl();
        ((JavaMailSenderImpl) this.javaMailSender).setDefaultEncoding("UTF-8");
        ((JavaMailSenderImpl) this.javaMailSender).setJavaMailProperties(new Properties() {
            private static final long serialVersionUID = -7384945982042097581L;
            {
                setProperty("mail.smtp.auth", "true");
                setProperty("mail.smtp.starttls.enable", "true");
            }
        });
        ((JavaMailSenderImpl) this.javaMailSender).setHost(host);
        ((JavaMailSenderImpl) this.javaMailSender).setPort(port);
        ((JavaMailSenderImpl) this.javaMailSender).setUsername(user);
        ((JavaMailSenderImpl) this.javaMailSender).setPassword(password);
        this.fromAddress = fromAddress;
    }

    public JavaMailSender getJavaMailSender() {
        return javaMailSender;
    }

    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public Boolean isConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }
}
