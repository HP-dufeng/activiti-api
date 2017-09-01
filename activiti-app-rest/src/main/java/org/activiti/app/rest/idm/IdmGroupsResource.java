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

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.activiti.app.constant.GroupIds;
import org.activiti.app.constant.GroupTypes;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.idm.GroupRepresentation;
import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Joram Barrez
 */
@RestController
@RequestMapping(value = "/rest/admin/groups")
@Api(tags = { "idm - idmGroups" }, description = "Manage idm groups", authorizations = { @Authorization(value = "basicAuth") })
public class IdmGroupsResource {

  @Autowired
  private IdentityService identityService;

  @ApiOperation(value = "Get all groups", responseContainer = "List", response = GroupRepresentation.class)
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @RequestMapping(method = RequestMethod.GET)
  public List<GroupRepresentation> getGroups() {
    validateAdminRole();
    List<Group> groups = identityService.createGroupQuery().list();
    List<GroupRepresentation> result = new ArrayList<GroupRepresentation>(groups.size());
    for (Group group : groups) {
      result.add(new GroupRepresentation(group));
    }
    return result;
  }

  @ApiOperation(value = "Get a group", response = GroupRepresentation.class)
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @RequestMapping(value = "/{groupId}", method = RequestMethod.GET)
  public GroupRepresentation getGroup(@ApiParam(name = "groupId", value="The id of the group.") @PathVariable String groupId) {
    validateAdminRole();
    return new GroupRepresentation(identityService.createGroupQuery().groupId(groupId).singleResult());
  }

  @ApiOperation(value = "Get users with a group",responseContainer = "List",response = UserRepresentation.class)
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @RequestMapping(value = "/{groupId}/users", method = RequestMethod.GET)
  public ResultListDataRepresentation getGroupUsers(@ApiParam(name = "groupId", value="The id of the group.") @PathVariable String groupId,
                                                    @ApiParam(name = "filter", value="The filter of user name.") @RequestParam(required = false) String filter,
                                                    @ApiParam(name = "page", value="The page number.") @RequestParam(required = false) Integer page,
                                                    @ApiParam(name = "pageSize", value="The page size.") @RequestParam(required = false) Integer pageSize) {
    validateAdminRole();
    int pageValue = page != null ? page.intValue() : 0;
    int pageSizeValue = pageSize != null ? pageSize.intValue() : 50;
    
    UserQuery userQuery = identityService.createUserQuery().memberOfGroup(groupId);
    if (StringUtils.isNotEmpty(filter)) {
      userQuery.userFullNameLike("%" + filter + "%");
    }
    List<User> users = userQuery.listPage(pageValue, pageSizeValue);
    
    List<UserRepresentation> userRepresentations = new ArrayList<UserRepresentation>(users.size());
    for (User user : users) {
      userRepresentations.add(new UserRepresentation(user));
    }

    ResultListDataRepresentation resultListDataRepresentation = new ResultListDataRepresentation(userRepresentations);
    resultListDataRepresentation.setStart(pageValue * pageSizeValue);
    resultListDataRepresentation.setSize(userRepresentations.size());
    resultListDataRepresentation.setTotal(userQuery.count());
    return resultListDataRepresentation;
  }

  @ApiOperation(value = "Create new group", response = GroupRepresentation.class)
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @RequestMapping(method = RequestMethod.POST)
  public GroupRepresentation createNewGroup(@RequestBody GroupRepresentation groupRepresentation) {
    validateAdminRole();
    if (StringUtils.isBlank(groupRepresentation.getName())) {
      throw new BadRequestException("Group name required");
    }

    Group newGroup = identityService.newGroup(groupRepresentation.getId());
    newGroup.setName(groupRepresentation.getName());
    
    if (groupRepresentation.getType() == null) {
      newGroup.setType(GroupTypes.TYPE_ASSIGNMENT);
    } else {
      newGroup.setType(groupRepresentation.getType());
    }
    
    identityService.saveGroup(newGroup);
    return new GroupRepresentation(newGroup);
  }

  @ApiOperation(value = "Update a group", response = GroupRepresentation.class)
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @RequestMapping(value = "/{groupId}", method = RequestMethod.PUT)
  public GroupRepresentation updateGroup(@ApiParam(name = "groupId", value="The id of the group.") @PathVariable String groupId, @RequestBody GroupRepresentation groupRepresentation) {
    validateAdminRole();
    if (StringUtils.isBlank(groupRepresentation.getName())) {
      throw new BadRequestException("Group name required");
    }

    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }
    
    group.setName(groupRepresentation.getName());
    identityService.saveGroup(group);
    
    return new GroupRepresentation(group);
  }

  @ApiOperation(value = "Delete a group")
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = "/{groupId}", method = RequestMethod.DELETE)
  public void deleteGroup(@ApiParam(name = "groupId", value="The id of the group.") @PathVariable String groupId) {
    validateAdminRole();
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }

    identityService.deleteGroup(groupId);
  }

  @ApiOperation(value = "Add user to group")
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = "/{groupId}/members/{userId}", method = RequestMethod.POST)
  public void addGroupMember(@ApiParam(name = "groupId", value="The id of the group.") @PathVariable String groupId,
                             @ApiParam(name = "userId", value="The id of the user.") @PathVariable String userId) {
    validateAdminRole();
    verifyGroupMemberExists(groupId, userId);
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }
    
    User user = identityService.createUserQuery().userId(userId).singleResult();
    if (user == null) {
      throw new NotFoundException();
    }
    
    identityService.createMembership(userId, groupId);
  }

  @ApiOperation(value = "Remove user from group")
  @ApiResponses(value = {
          @ApiResponse(code = 403, message = "Forbidden.")
  })
  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = "/{groupId}/members/{userId}", method = RequestMethod.DELETE)
  public void deleteGroupMember(@ApiParam(name = "groupId", value="The id of the group.") @PathVariable String groupId,
                                @ApiParam(name = "userId", value="The id of the user.") @PathVariable String userId) {
    validateAdminRole();
    verifyGroupMemberExists(groupId, userId);
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }
    
    User user = identityService.createUserQuery().userId(userId).singleResult();
    if (user == null) {
      throw new NotFoundException();
    }
    
    identityService.deleteMembership(userId, groupId);
  }
  
  protected void verifyGroupMemberExists(String groupId, String userId) {
    // Check existence
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    User user = identityService.createUserQuery().userId(userId).singleResult();
    for (User groupMember : identityService.createUserQuery().memberOfGroup(groupId).list()) {
      if (groupMember.getId().equals(userId)) {
        user = groupMember;
      }
    }

    if (group == null || user == null) {
      throw new NotFoundException();
    }
  }
  
  protected void validateAdminRole() {
    boolean isAdmin = identityService.createGroupQuery()
        .groupId(GroupIds.ROLE_ADMIN)
        .groupMember(SecurityUtils.getCurrentUserId())
        .count() > 0;
        if (!isAdmin) {
          throw new NotPermittedException();
        }
  }
  
}
