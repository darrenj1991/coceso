<!DOCTYPE html>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%--
/**
 * CoCeSo
 * Client HTML incident form window
 * Copyright (c) WRK\Coceso-Team
 *
 * Licensed under the GNU General Public License, version 3 (GPL-3.0)
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright Copyright (c) 2014 WRK\Coceso-Team
 * @link https://sourceforge.net/projects/coceso/
 * @license GPL-3.0 http://opensource.org/licenses/GPL-3.0
 */
--%>
<html>
  <head>
    <title>No direct access</title>
  </head>
  <body style="display: none">
    <div class="ajax_content incident_form" data-bind="droppable: {drop: assignUnitForm, tolerance: 'pointer'}">
      <form data-bind="submit: save">
        <div class="alert alert-danger" data-bind="visible: error">
          <strong><spring:message code="error"/>:</strong> <span data-bind="text: errorText"></span>
        </div>

        <div class="clearfix">
          <div class="form-group col-md-7">
            <label class="sr-only"><spring:message code="incident.type"/>:</label>
            <div class="btn-group btn-group-sm">
              <button type="button" class="btn btn-default" data-bind="disable: disableTask, click: isTask.set, css: isTask.state">
                <spring:message code="incident.type.task"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: disableTask, click: isTransport.set, css: isTransport.state">
                <spring:message code="incident.type.transport"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: disableRelocation, click: isRelocation.set, css: isRelocation.state">
                <spring:message code="incident.type.relocation"/>
              </button>
            </div>
          </div>

          <div class="form-group col-md-5 text-right">
            <button type="button" class="btn btn-default btn-sm" data-bind="click: blue.toggle, css: blue.state">
              <spring:message code="incident.blue"/>
            </button>
            <button type="button" class="btn btn-default btn-sm" data-bind="click: priority.toggle, css: priority.state">
              <spring:message code="incident.priority"/>
            </button>
          </div>
        </div>

        <div class="clearfix">
          <div class="form-group col-md-12" data-bind="css: bo.formcss">
            <label for="bo" class=""><spring:message code="incident.bo"/>:</label>
            <textarea id="bo" name="bo" rows="3" class="form-control" placeholder="<spring:message code="incident.bo"/>" autofocus
                      data-bind="disable: disableBO, value: bo, valueUpdate: 'input', css: {'form-highlight': !bo() && !disableBO()}, point: true">
            </textarea>
          </div>
        </div>

        <div class="form-group col-md-12" data-bind="css: caller.formcss">
          <label for="caller" class=""><spring:message code="incident.caller"/>:</label>
          <input type="text" id="caller" name="caller" class="form-control" placeholder="<spring:message code="incident.caller"/>"
                 data-bind="value: caller, valueUpdate: 'input', css: {'form-highlight': !caller()}"/>
        </div>

        <div class="form-group col-md-12" data-bind="css: info.formcss">
          <label for="info" class=""><spring:message code="incident.info"/>:</label>
          <div class="alert alert-warning" data-bind="visible: info.serverChange">
            <spring:message code="serverchange"/>
            <a href="#" title="<spring:message code="serverchange.apply"/>" data-bind="text: info.serverChange, click: info.reset"></a>
          </div>
          <textarea id="info" name="info" rows="3" class="form-control" placeholder="<spring:message code="incident.info"/>"
                    data-bind="value: info, valueUpdate: 'input'">
          </textarea>
        </div>

        <div class="clearfix">
          <div class="form-group col-md-12" data-bind="css: ao.formcss">
            <label for="ao" class=""><spring:message code="incident.ao"/>:</label>
            <textarea id="ao" name="ao" rows="3" class="form-control"
                      placeholder="<spring:message code="incident.ao"/>"
                      data-bind="textInput: ao, css: {'form-highlight': highlightAO()}, point: true">
            </textarea>
          </div>
        </div>

        <div class="clearfix">
          <!-- ko if: patient -->
          <div class="col-md-7">
            <strong><spring:message code="patient.needs"/></strong>:
            <span data-bind="text: patient().ertype"></span>
            <!-- ko if: !patient().isUnknown() -->
            (<span data-bind="text: patient().localizedSex"></span>)
            <!-- /ko -->
          </div>
          <div class="form-group col-md-5">
            <button type="button" class="btn btn-primary btn-sm" data-bind="click: openPatient"><spring:message code="patient.edit"/></button>
          </div>
          <!-- /ko -->
          <!-- ko ifnot: patient -->
          <div class="form-group col-md-5">
            <button type="button" class="btn btn-danger btn-sm" data-bind="click: openPatient"><spring:message code="patient.add"/></button>
          </div>
          <!-- /ko -->
        </div>

        <div class="clearfix">
          <div class="form-group col-md-6" data-bind="css: casusNr.formcss">
            <label for="casus"><spring:message code="incident.casus"/>:</label>
            <input type="text" id="casus" name="casus" class="form-control" placeholder="<spring:message code="incident.casus"/>"
                   data-bind="value: casusNr, valueUpdate: 'input'"/>
          </div>
          <div class="form-group col-md-5 col-md-offset-1" data-bind="css: section.formcss, if: sections.hasSections">
            <label for="section"><spring:message code="concern.section"/>:</label>
            <select id="section" name="section" class="form-control" data-bind="value: section, options: sections.selectSections,
                    optionsValue: 'value', optionsText: 'name'">
            </select>
          </div>
        </div>

        <%-- Incident State --%>
        <div class="clearfix">
          <div class="form-group col-md-offset-2 col-md-10">
            <label class="sr-only"><spring:message code="incident.state"/>:</label>
            <div class="btn-group btn-group-sm">
              <button type="button" class="btn btn-default" data-bind="disable: disableNew, click: isNew.set, css: isNew.state">
                <spring:message code="incident.state.new"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="click: isOpen.set, css: isOpen.state">
                <spring:message code="incident.state.open"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: disableDispo, click: isDispo.set, css: isDispo.state">
                <spring:message code="incident.state.dispo"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: disableWorking, click: isWorking.set, css: isWorking.state">
                <spring:message code="incident.state.working"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="click: isDone.set, css: isDone.state">
                <spring:message code="incident.state.done"/>
              </button>
            </div>
          </div>
        </div>

        <%-- Assigned Units --%>
        <div class="assigned" data-bind="foreach: units">
          <div class="form-group clearfix">
            <label class="col-md-4 control-label">
              <!-- ko if: $parent.unitCount() > 1 -->
              <span class="glyphicon glyphicon-share" data-bind="click: $parent.duplicate"></span>
              <!-- /ko -->
              <span data-bind="text: unit() && unit().call"></span>
            </label>
            <div class="col-md-8 btn-group btn-group-sm nowrap">
              <button type="button" class="btn btn-default" data-bind="disable: $parent.disableAssigned, click: isAssigned.set, css: isAssigned.state">
                <spring:message code="task.state.assigned"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: $parent.disableBO, click: isZBO.set, css: isZBO.state">
                <spring:message code="task.state.zbo"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: $parent.disableBO, click: isABO.set, css: isABO.state">
                <spring:message code="task.state.abo"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: $parent.disableZAO, click: isZAO.set, css: isZAO.state">
                <spring:message code="task.state.zao"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="disable: $parent.disableAAO, click: isAAO.set, css: isAAO.state">
                <spring:message code="task.state.aao"/>
              </button>
              <button type="button" class="btn btn-default" data-bind="click: isDetached.set, css: isDetached.state">
                <spring:message code="task.state.detached"/>
              </button>
            </div>
          </div>
        </div>

        <div class="clearfix">
          <div class="form-group col-md-offset-2 col-md-10">
            <button type="button" class="btn btn-success" data-bind="enable: form.enable, click: ok">
              <spring:message code="ok"/>
            </button>
            <button type="submit" class="btn btn-primary" data-bind="enable: form.enable">
              <spring:message code="save"/>
            </button>
            <button type="button" class="btn btn-warning" data-bind="enable: form.changed, click: form.reset">
              <spring:message code="reset"/>
            </button>
            <button type="button" class="btn btn-default" data-bind="click: duplicate">
              <spring:message code="incident.duplicate"/>
            </button>
          </div>
        </div>
      </form>
    </div>
  </body>
</html>
