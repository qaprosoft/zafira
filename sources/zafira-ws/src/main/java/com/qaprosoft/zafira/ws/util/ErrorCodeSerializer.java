package com.qaprosoft.zafira.ws.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.qaprosoft.zafira.ws.dto.errors.ErrorCode;

public class ErrorCodeSerializer extends JsonSerializer<ErrorCode>
{
	@Override
	public void serialize(ErrorCode value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException
	{
		jgen.writeNumber(value.getCode());
	}
}
