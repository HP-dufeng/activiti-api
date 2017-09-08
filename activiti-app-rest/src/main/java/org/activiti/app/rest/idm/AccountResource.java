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
package org.activiti.app.rest.idm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.activiti.app.model.idm.GroupRepresentation;
import org.activiti.app.model.idm.UserRepresentation;


import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.UnauthorizedException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST controller for managing the current user's account.
 * 
 * @author Joram Barrez
 */
@RestController
@Api(tags = { "idm - account" }, description = "Users Info", authorizations = { @Authorization(value = "basicAuth") })
public class AccountResource {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private IdentityService identityService;

  /**
   * GET  /rest/authenticate -> check if the user is authenticated, and return its full name.
   */
  @ApiOperation(value = "Check if the user is authenticated")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Indicates the user is authenticated, and return its full name"),
          @ApiResponse(code = 401, message = "Indicates the requested did not contain valid authorization.")
  })
  @RequestMapping(value = "/rest/authenticate", method = RequestMethod.GET, produces = {"application/json"})
  public ObjectNode isAuthenticated(HttpServletRequest request) {
    String user = request.getRemoteUser();

//    JwtAuthenticatedProfile authentication = (JwtAuthenticatedProfile) SecurityContextHolder.getContext().getAuthentication();
//    WUCCUser currentUser = (WUCCUser)authentication.getPrincipal();
//    String user = currentUser.getName();
//
//    if(user == null) {
//        throw new UnauthorizedException("Request did not contain valid authorization");
//    }
    
    ObjectNode result = objectMapper.createObjectNode();
    result.put("login", user);
    return result;
  }

  /**
   * GET  /rest/account -> get the current user.
   */
  @ApiOperation(value = "Get the current user", response = UserRepresentation.class)
  @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces = "application/json")
  public UserRepresentation getAccount() {
    User user = SecurityUtils.getCurrentActivitiAppUser().getUserObject();
    
    UserRepresentation userRepresentation = new UserRepresentation(user);
    
    List<Group> groups = identityService.createGroupQuery().groupMember(user.getId()).list();
    for (Group group : groups) {
      userRepresentation.getGroups().add(new GroupRepresentation(group));
    }
    
    return userRepresentation;
  }
}
