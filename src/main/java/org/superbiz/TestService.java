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

import org.superbiz.cluster.ClusterException;
import org.superbiz.cluster.ClusterFactory;
import org.superbiz.cluster.ClusterType;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import static javax.ejb.LockType.READ;

@Lock(READ)
@Singleton
@Startup
@Path("/test")
public class TestService {

    @PostConstruct
    public void startup() {
        try {
            // Initiialize the cluster
            ClusterFactory.getClusterInstance(ClusterType.HAZELCAST);
        } catch (ClusterException e) {
            e.printStackTrace();
        }
    }

    @POST
    @Path("{key}")
    public void set(@PathParam("key") final String key, final String value) {
        try {
            ClusterFactory.getClusterInstance(ClusterType.HAZELCAST).set(key, value);
        } catch (ClusterException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("{key}")
    public String get(@PathParam("key") final String key) {
        try {
            return ClusterFactory.getClusterInstance(ClusterType.HAZELCAST).get(key);
        } catch (ClusterException e) {
            e.printStackTrace();
        }

        return null;
    }

}
