/*
 * Copyright (C) 2015-2024 Philipp Nanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.philippn.springremotingautoconfigure.server.spring;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class RemotingController implements Controller {

    private Object service;
    private Class<?> serviceInterface;

    private static final CBORFactory cborFactory = new CBORFactory(CBORMapper.builder()
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .findAndAddModules()
            .build());

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try (CBORParser input = cborFactory.createParser(request.getInputStream())) {
            input.nextToken();
            input.nextToken();
            String methodName = input.getValueAsString();
            input.nextToken();
            int arity = input.getValueAsInt();
            Optional<Method> method = findMethod(methodName, arity);
            if (method.isEmpty()) {
                response.sendError(422);
                return null;
            }
            Object[] args = new Object[arity];
            for (int i = 0; i < arity; i++) {
                Class<?> argumentClazz = method.get().getParameterTypes()[i];
                input.nextToken();
                args[i] = input.readValueAs(argumentClazz);
            }
            Object ret = method.get().invoke(service, args);
            if (!Void.class.equals(method.get().getReturnType())) {
                try (CBORGenerator output = cborFactory.createGenerator(response.getOutputStream())) {
                    output.writeObject(ret);
                }
            }
            return null;
        } catch (InvocationTargetException | IllegalAccessException | IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private Optional<Method> findMethod(String methodName, int arity) {
        return Arrays.stream(serviceInterface.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> m.getParameterCount() == arity)
                .findFirst();
    }
}
