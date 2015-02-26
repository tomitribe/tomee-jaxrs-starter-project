/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz;

import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Lock(READ)
@Singleton
@Path("/color")
public class ColorService {

    private String color;

    public ColorService() {
        this.color = "white";
    }

    @GET
    public String getColor() {
        return color;
    }

    @Lock(WRITE)
    @Path("{color}")
    @POST
    public void setColor(@PathParam("color") String color) {
        this.color = color;
    }

    @Path("object")
    @GET
    @Produces({APPLICATION_JSON})
    public Color getColorObject() {
        return new Color("orange", 0xE7, 0x71, 0x00);
    }
}
