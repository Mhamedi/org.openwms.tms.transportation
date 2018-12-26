/*
 * Copyright 2018 Heiko Scherrer
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
 */
package org.openwms.tms.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * A TransportOrderApi.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@FeignClient(name = "tms-service", qualifier = "transportOrderApi", decode404 = true)
public interface TransportOrderApi {

    @PostMapping(value = "/transport-orders")
    @ResponseStatus(HttpStatus.CREATED)
    void createTO(@RequestParam(value = "barcode") String barcode, @RequestParam(value = "target") String target);

    @PostMapping(value = "/transport-orders")
    @ResponseStatus(HttpStatus.CREATED)
    void createTO(@RequestParam(value = "barcode") String barcode, @RequestParam(value = "target") String target, @RequestParam(value = "priority", required = false) String priority);

    @PostMapping(value = "/transport-orders/{id}", params = {"state"})
    void changeState(@PathVariable(value = "id") String pKey, @RequestParam(value = "state") String state);

    @GetMapping(value = "/transport-orders", params = {"barcode", "state"})
    List<TransportOrderVO> findBy(@RequestParam(value = "barcode") String barcode, @RequestParam(value = "state") String state);

    @GetMapping(value = "/transport-orders", params ={"sourceLocation", "state", "searchTargetLocationGroupNames"})
    TransportOrderVO getNextInfeed(@RequestParam("sourceLocation") String sourceLocation, @RequestParam("state") String state, @RequestParam("searchTargetLocationGroupNames") String searchTargetLocationGroups);

    @GetMapping(value = "/transport-orders", params ={"sourceLocationGroupName", "targetLocationGroupName", "state"})
    TransportOrderVO getNextInAisle(@RequestParam("sourceLocationGroupName") String sourceLocationGroupName, @RequestParam("targetLocationGroupName") String targetLocationGroupName, @RequestParam("state") String state);

    @GetMapping(value = "/transport-orders", params ={"state", "sourceLocationGroupName"})
    TransportOrderVO getNextOutfeed(@RequestParam("state") String state, @RequestParam("sourceLocationGroupName") String sourceLocationGroupName);
}
