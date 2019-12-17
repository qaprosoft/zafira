#!/bin/bash

if [ $( psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -tAc "SELECT 1 FROM pg_namespace WHERE nspname = 'zafira'" ) == '1' ];
then
echo "Schema already exists"
exit 1
fi

psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-mng-structure.sql
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-mng-data.sql
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-app-structure.sql
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-app-state-management.sql
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-app-data.sql
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-views.sql
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-views-cron.sql
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -f /docker-entrypoint-initdb.d/sql/db-widgets.sql


if [ "$ZAFIRA_AMAZON_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AMAZON_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AMAZON_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AMAZON_BUCKET' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AMAZON_BUCKET';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AMAZON_ACCESS_KEY' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AMAZON_ACCESS_KEY';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AMAZON_SECRET_KEY' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AMAZON_SECRET_KEY';"
fi

if [ "$ZAFIRA_JENKINS_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_JENKINS_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='JENKINS_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_JENKINS_URL' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='JENKINS_URL';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_JENKINS_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='JENKINS_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_JENKINS_API_TOKEN_OR_PASSWORD' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='JENKINS_API_TOKEN_OR_PASSWORD';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_JENKINS_FOLDER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='JENKINS_FOLDER';"
fi

if [ "$ZAFIRA_EMAIL_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_EMAIL_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='EMAIL_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_EMAIL_HOST' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='EMAIL_HOST';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_EMAIL_PORT' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='EMAIL_PORT';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_EMAIL_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='EMAIL_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_EMAIL_PASSWORD' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='EMAIL_PASSWORD';"
fi

if [ "$ZAFIRA_RABBITMQ_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_RABBITMQ_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='RABBITMQ_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_RABBITMQ_HOST' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='RABBITMQ_HOST';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_RABBITMQ_PORT' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='RABBITMQ_PORT';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_RABBITMQ_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='RABBITMQ_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_RABBITMQ_PASSWORD' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='RABBITMQ_PASSWORD';"
fi

if [ "$ZAFIRA_SELENIUM_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SELENIUM_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SELENIUM_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SELENIUM_URL' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SELENIUM_URL';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SELENIUM_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SELENIUM_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SELENIUM_PASSWORD' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SELENIUM_PASSWORD';"
fi

if [ "$ZAFIRA_MCLOUD_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_MCLOUD_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='MCLOUD_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_MCLOUD_URL' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='MCLOUD_URL';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_MCLOUD_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='MCLOUD_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_MCLOUD_PASSWORD' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='MCLOUD_PASSWORD';"
fi

if [ "$ZAFIRA_AEROKUBE_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AEROKUBE_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AEROKUBE_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AEROKUBE_URL' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AEROKUBE_URL';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AEROKUBE_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AEROKUBE_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_AEROKUBE_PASSWORD' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='AEROKUBE_PASSWORD';"
fi

if [ "$ZAFIRA_BROWSERSTACK_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_BROWSERSTACK_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='BROWSERSTACK_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_BROWSERSTACK_URL' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SBROWSERSTACK_URL';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_BROWSERSTACK_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='BROWSERSTACK_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_BROWSERSTACK_PASSWORD' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='BROWSERSTACK_PASSWORD';"
fi

if [ "$ZAFIRA_SAUCELABS_ENABLED" == true ];
then
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SAUCELABS_ENABLED' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SAUCELABS_ENABLED';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SAUCELABS_URL' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SAUCELABS_URL';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SAUCELABS_USER' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SAUCELABS_USER';"
    psql --username $POSTGRES_USER -c "update zafira.integration_settings ise set value='$ZAFIRA_SAUCELABS_PASS' from zafira.integration_params ip where ise.integration_param_id=ip.id and ip.name='SAUCELABS_PASSWORD';"
fi