/*
 * Copyright 2006-2010 the original author or authors.
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
 */

package com.consol.citrus.validation.builder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import com.consol.citrus.variable.dictionary.DataDictionary;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Abstract control message builder is aware of message headers and delegates message payload
 * generation to subclass.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageContentBuilder implements MessageContentBuilder {

    /** Optional message name */
    private String messageName = "";

    /** The control headers expected for this message */
    private Map<String, Object> messageHeaders = new HashMap<>();

    /** The message header as a file resource path */
    private List<String> headerResources = new ArrayList<>();

    /** The message header as inline data */
    private List<String> headerData = new ArrayList<>();

    /** Optional data dictionary that explicitly modifies control message content before construction */
    private DataDictionary dataDictionary;

    /** List of manipulators for static message payload */
    private List<MessageConstructionInterceptor> messageInterceptors = new ArrayList<>();

    /**
     * Constructs the control message with headers and payload coming from 
     * subclass implementation.
     */
    @Override
    public Message buildMessageContent(TestContext context, String messageType) {
        Object payload = buildMessagePayload(context, messageType);

        try {
            Message message = new DefaultMessage(payload, buildMessageHeaders(context));
            message.setName(messageName);

            if (payload != null) {
                message = context.getMessageConstructionInterceptors().interceptMessageConstruction(message, messageType, context);

                if (dataDictionary != null) {
                    message = dataDictionary.interceptMessageConstruction(message, messageType, context);
                }

                for (MessageConstructionInterceptor modifyer : messageInterceptors) {
                    message = modifyer.interceptMessageConstruction(message, messageType, context);
                }
            }

            for (String headerResourcePath : headerResources) {
                message.addHeaderData(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(headerResourcePath, context), FileUtils.getCharset(headerResourcePath))));
            }

            for (String data : headerData){
                message.addHeaderData(context.replaceDynamicContentInString(data.trim()));
            }

            return message;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to build message content", e);
        }

    }
    
    public abstract Object buildMessagePayload(TestContext context, String messageType);

    public Map<String, Object> buildMessageHeaders(TestContext context) {
        try {
            Map<String, Object> headers = context.resolveDynamicValuesInMap(messageHeaders);

            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                String value = entry.getValue().toString();
                
                if (MessageHeaderType.isTyped(value)) {
                    MessageHeaderType type = MessageHeaderType.fromTypedValue(value);
                    Constructor<?> constr = type.getHeaderClass().getConstructor(new Class[] { String.class });
                    entry.setValue(constr.newInstance(MessageHeaderType.removeTypeDefinition(value)));
                }
            }
            
            MessageHeaderUtils.checkHeaderTypes(headers);

            return headers;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to build message content", e);
        }
    }

    /**
     * Sets the messageName property.
     *
     * @param messageName
     */
    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    /**
     * Gets the value of the messageName property.
     *
     * @return the messageName
     */
    public String getMessageName() {
        return messageName;
    }

    @Override
    public void setDataDictionary(DataDictionary dataDictionary) {
        this.dataDictionary = dataDictionary;
    }

    /**
     * Gets the data dictionary.
     * @return
     */
    public DataDictionary getDataDictionary() {
        return dataDictionary;
    }

    /**
     * Sets the message headers for this control message.
     * @param messageHeaders the controlMessageHeaders to set
     */
    public void setMessageHeaders(Map<String, Object> messageHeaders) {
        this.messageHeaders = messageHeaders;
    }

    /**
     * Sets the message header resource paths.
     * @param headerResources the messageHeaderResource to set
     */
    public void setHeaderResources(List<String> headerResources) {
        this.headerResources = headerResources;
    }

    /**
     * Sets the message header data.
     * @param headerData
     */
    public void setHeaderData(List<String> headerData) {
        this.headerData = headerData;
    }

    /**
     * Gets the messageHeaders.
     * @return the messageHeaders
     */
    public Map<String, Object> getMessageHeaders() {
        return messageHeaders;
    }

    /**
     * Gets the message header resource paths.
     * @return the header resources.
     */
    public List<String> getHeaderResources() {
        return headerResources;
    }

    /**
     * Gets the message header data.
     * @return the headerData.
     */
    public List<String> getHeaderData() {
        return headerData;
    }

    /**
     * Adds a new interceptor to the message construction process.
     * @param interceptor
     */
    public void add(MessageConstructionInterceptor interceptor) {
        messageInterceptors.add(interceptor);
    }

    /**
     * Gets the messageInterceptors.
     * @return the messageInterceptors
     */
    public List<MessageConstructionInterceptor> getMessageInterceptors() {
        return messageInterceptors;
    }

    /**
     * Sets the messageInterceptors.
     * @param messageInterceptors the messageInterceptors to set
     */
    public void setMessageInterceptors(
            List<MessageConstructionInterceptor> messageInterceptors) {
        this.messageInterceptors = messageInterceptors;
    }
}
