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
package org.activiti.app.rest.editor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.activiti.app.model.editor.AppDefinitionRepresentation;
import org.activiti.app.model.editor.FormSaveRepresentation;
import org.activiti.app.model.editor.form.FormRepresentation;
import org.activiti.app.service.editor.ActivitiFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@Api(tags = { "editor - form" }, description = "Form model editor", authorizations = { @Authorization(value = "basicAuth") })
@RestController
@RequestMapping("/rest/form-models")
public class FormResource {

  @Autowired
  protected ActivitiFormService formService;

  @ApiOperation(value = "Get a form definitions", response = FormRepresentation.class)
  @RequestMapping(value = "/{formId}", method = RequestMethod.GET, produces = "application/json")
  public FormRepresentation getForm(@PathVariable("formId") String formId) {
    return formService.getForm(formId);
  }

  @ApiOperation(value = "Get forms definitions", response = FormRepresentation.class, responseContainer = "List")
  @RequestMapping(value = "/values", method = RequestMethod.GET, produces = "application/json")
  public List<FormRepresentation> getForms(HttpServletRequest request) {
    String[] formIds = request.getParameterValues("formId");
    return formService.getForms(formIds);
  }

  @ApiOperation(value = "Get a form definitions history", response = FormRepresentation.class)
  @RequestMapping(value = "/{formId}/history/{formHistoryId}", method = RequestMethod.GET, produces = "application/json")
  public FormRepresentation getFormHistory(@PathVariable("formId") String formId, @PathVariable("formHistoryId") String formHistoryId) {
    return formService.getFormHistory(formId, formHistoryId);
  }

  @ApiOperation(value = "Save a form definitions", response = FormRepresentation.class)
  @RequestMapping(value = "/{formId}", method = RequestMethod.PUT, produces = "application/json")
  public FormRepresentation saveForm(@PathVariable("formId") String formId, @RequestBody FormSaveRepresentation saveRepresentation) {
    return formService.saveForm(formId, saveRepresentation);
  }
}
