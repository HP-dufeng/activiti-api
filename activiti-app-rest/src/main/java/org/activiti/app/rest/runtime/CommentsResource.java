/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.activiti.app.rest.runtime;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.runtime.AppDefinitionRepresentation;
import org.activiti.app.model.runtime.CommentRepresentation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST resource related to comment collection on tasks and process instances.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Api(tags = { "runtime - comments" }, description = "Comments resource", authorizations = { @Authorization(value = "basicAuth") })
@RestController
public class CommentsResource extends AbstractCommentsResource {

    @ApiOperation(value = "Get task comments", response = CommentRepresentation.class,responseContainer = "List")
    @RequestMapping(value = "/rest/tasks/{taskId}/comments", method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getTaskComments(@PathVariable("taskId") String taskId,
            @RequestParam(value = "latestFirst", required = false) Boolean latestFirst) {

    	return super.getTaskComments(taskId, latestFirst);
    	
    }

    @ApiOperation(value = "Add task comment", response = CommentRepresentation.class)
    @RequestMapping(value = "/rest/tasks/{taskId}/comments", method = RequestMethod.POST, produces = "application/json")
    public CommentRepresentation addTaskComment(@RequestBody CommentRepresentation commentRequest,
            @PathVariable("taskId") String taskId) {

        return super.addTaskComment(commentRequest, taskId);
    	
    }

    @ApiOperation(value = "Get process instance comments", response = CommentRepresentation.class,responseContainer = "List")
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/comments", method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getProcessInstanceComments(@PathVariable("processInstanceId") String processInstanceId,
            @RequestParam(value = "latestFirst", required = false) Boolean latestFirst) {

    	return super.getProcessInstanceComments(processInstanceId, latestFirst);
       
    }

    @ApiOperation(value = "Add process instance comment", response = CommentRepresentation.class)
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/comments", method = RequestMethod.POST, produces = "application/json")
    public CommentRepresentation addProcessInstanceComment(@RequestBody CommentRepresentation commentRequest,
            @PathVariable("processInstanceId") String processInstanceId) {

        return super.addProcessInstanceComment(commentRequest, processInstanceId);
    	
    }

}
