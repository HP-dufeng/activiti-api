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
import org.activiti.app.model.runtime.TaskRepresentation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

@Api(tags = { "runtime - task" }, description = "Task resource", authorizations = { @Authorization(value = "basicAuth") })
@RestController
public class TaskQueryResource extends AbstractTaskQueryResource {

	@ApiOperation(value = "List tasks", response = TaskRepresentation.class, responseContainer = "List")
	@RequestMapping(value = "/rest/query/tasks", method = RequestMethod.POST, produces = "application/json")
	public ResultListDataRepresentation listTasks(@RequestBody ObjectNode requestNode) {
		return super.listTasks(requestNode);	
	}

}
