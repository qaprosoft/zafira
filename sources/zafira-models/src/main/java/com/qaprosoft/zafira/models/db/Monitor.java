package com.qaprosoft.zafira.models.db;

public class Monitor extends AbstractEntity
{
	private static final long serialVersionUID = -1016459307109758493L;

	public enum HttpMethod
	{
		GET, POST, PUT, DELETE
	}

	public enum Type
	{
		HTTP, PING
	}

	private String name;
	private String url;
	private HttpMethod httpMethod;
	private String requestBody;
	private String cronExpression;
	private boolean notificationEnabled;
	private boolean running;
	private String recipients;
	private Type type;
	private int expectedCode;
	private int lastCode;
	private boolean lastRunPassed;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public HttpMethod getHttpMethod()
	{
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod)
	{
		this.httpMethod = httpMethod;
	}

	public String getRequestBody()
	{
		return requestBody;
	}

	public void setRequestBody(String requestBody)
	{
		this.requestBody = requestBody;
	}

	public String getCronExpression()
	{
		return cronExpression;
	}

	public void setCronExpression(String cronExpression)
	{
		this.cronExpression = cronExpression;
	}

	public boolean isNotificationEnabled()
	{
		return notificationEnabled;
	}

	public void setNotificationEnabled(boolean notificationEnabled)
	{
		this.notificationEnabled = notificationEnabled;
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setRunning(boolean running)
	{
		this.running = running;
	}

	public int getExpectedCode()
	{
		return expectedCode;
	}

	public void setExpectedCode(int expectedCode)
	{
		this.expectedCode = expectedCode;
	}

	public String getRecipients()
	{
		return recipients;
	}

	public void setRecipients(String recipients)
	{
		this.recipients = recipients;
	}

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public int getLastCode()
	{
		return lastCode;
	}

	public void setLastCode(int lastCode)
	{
		this.lastCode = lastCode;
	}

	public boolean isLastRunPassed()
	{
		return lastRunPassed;
	}

	public void setLastRunPassed(boolean lastRunPassed)
	{
		this.lastRunPassed = lastRunPassed;
	}

	@Override
	public String toString()
	{
		return "Monitor{" +
				"name='" + name + '\'' +
				", url='" + url + '\'' +
				", httpMethod=" + httpMethod +
				", requestBody='" + requestBody + '\'' +
				", cronExpression='" + cronExpression + '\'' +
				", active=" + notificationEnabled +
				", emails='" + recipients + '\'' +
				", type=" + type +
				", expectedCode=" + expectedCode +
				'}';
	}
}


