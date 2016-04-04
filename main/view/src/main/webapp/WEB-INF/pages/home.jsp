<!DOCTYPE html>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@taglib uri="coceso" prefix="t"%>
<%--
/**
 * CoCeSo
 * Client HTML home page
 * Copyright (c) WRK\Coceso-Team
 *
 * Licensed under the GNU General Public License, version 3 (GPL-3.0)
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright Copyright (c) 2015 WRK\Coceso-Team
 * @link https://sourceforge.net/projects/coceso/
 * @license GPL-3.0 http://opensource.org/licenses/GPL-3.0
 */
--%>
<html>
  <head>
    <script type="text/javascript">
      var CocesoConf = {
        jsonBase: "<c:url value="/data/"/>",
        langBase: "<c:url value="/static/i18n/"/>",
        language: "<spring:message code="this.languageCode"/>",
        plugins: ${cocesoConfig.jsPlugins},
        initalError: ${error}
      };
    </script>
    <t:head title="nav.home" entry="home"/>
  </head>
  <body class="scroll">
    <div class="container">
      <c:set value="active" var="nav_home"/>
      <%@include file="parts/navbar.jsp"%>

      <%-- Userdetails -- DEBUG --%>
      <div class="alert alert-info alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <p><strong><spring:message code="user"/>:</strong> <c:out value="${user.firstname} ${user.lastname} (${user.username})"/></p>
        <p><spring:message code="user.roles"/>:</p>
        <ul>
          <c:forEach items="${user.authorities}" var="role">
            <li><c:out value="${role}"/></li>
            </c:forEach>
        </ul>
      </div>

      <%-- Show Error Message --%>
      <div class="alert alert-danger" data-bind="visible: error">
        <strong><spring:message code="error"/>:</strong> <span data-bind="text: errorText"></span>
      </div>

      <%-- Last Active Concern --%>
      <div class="alert alert-success" data-bind="visible: concernId">
        <p><strong><spring:message code="concern.last"/>:</strong>&nbsp;<span data-bind="text: concernName"></span></p>
        <p>
          <sec:authorize access="@auth.hasAccessLevel('Main')">
            <a href="<c:url value="/main"/>" class="btn btn-success"><spring:message code="concern.start"/></a>
          </sec:authorize>
          <a href="<c:url value="/patadmin"/>" class="btn btn-success"><spring:message code="patadmin"/></a>
        </p>
      </div>

      <%-- Create new Concern --%>
      <sec:authorize access="@auth.hasAccessLevel('Edit')">
        <div class="page-header">
          <h2><spring:message code="concern.create"/></h2>
        </div>
        <!-- ko with: create -->
        <form class="clearfix" data-bind="submit: save">
          <div class="col-md-5 form-group" data-bind="css: name.formcss">
            <label class="sr-only" for="create_name"><spring:message code="concern.name"/></label>
            <input type="text" id="create_name" maxlength="64" class="form-control"
                   data-bind="value: name, valueUpdate: 'input'"
                   placeholder="<spring:message code="concern.name"/>"/>
          </div>
          <div class="col-md-3">
            <button type="submit" class="btn btn-success" data-bind="enable: name.changed"><spring:message code="create"/></button>
          </div>
        </form>
        <!-- /ko -->
      </sec:authorize>

      <%-- Active Concerns --%>
      <div class="page-header">
        <h2><spring:message code="concerns"/></h2>
      </div>
      <div class="table-responsive">
        <table class="table table-striped">
          <tbody data-bind="foreach: open">
            <tr data-bind="css: {success: isActive}">
              <td data-bind="text: name"></td>
              <td>
                <button type="button" class="btn btn-sm btn-success" data-bind="click: select, disable: $root.locked"><spring:message code="concern.select"/></button>
                <sec:authorize access="@auth.hasAccessLevel('CloseConcern')">
                  <button type="button" class="btn btn-sm btn-danger" data-bind="click: close"><spring:message code="concern.close"/></button>
                </sec:authorize>
                <sec:authorize access="@auth.hasAccessLevel('Report')">
                  <a target="_blank" class="btn btn-sm btn-success" data-bind="attr: {href: '<c:url value="/pdf/report.pdf?id="/>' + id}"><spring:message code="pdf.report"/></a>
                  <a target="_blank" class="btn btn-sm btn-warning" data-bind="attr: {href: '<c:url value="/pdf/dump.pdf?id="/>' + id}"><spring:message code="pdf.dump"/></a>
                </sec:authorize>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="form-group" data-bind="visible: concernId">
          <sec:authorize access="@auth.hasAccessLevel('Main')">
            <a href="<c:url value="/main"/>" class="btn btn-success"><spring:message code="concern.start"/></a>
          </sec:authorize>
          <sec:authorize access="@auth.hasAccessLevel('Edit')">
            <a href="<c:url value="/edit"/>" class="btn btn-warning"><spring:message code="concern.edit"/></a>
          </sec:authorize>
          <a href="<c:url value="/patadmin"/>" class="btn btn-success"><spring:message code="patadmin"/></a>
        </div>

        <div class="alert alert-danger" data-bind="visible: locked">
          <p><spring:message code="concern.locked"/></p>
          <p><a class="btn btn-danger btn-xs" data-bind="click: forceUnlock"><spring:message code="concern.unlock"/></a></p>
        </div>
      </div>

      <%-- Closed Concerns --%>
      <sec:authorize access="@auth.hasAccessLevel('CloseConcern') or @auth.hasAccessLevel('Report')">
        <div class="page-header">
          <h2><spring:message code="concern.closed"/></h2>
        </div>
        <div class="table-responsive">
          <table class="table table-striped">
            <tbody data-bind="foreach: closed">
              <tr>
                <td data-bind="text: name"></td>
                <td>
                  <sec:authorize access="@auth.hasAccessLevel('Report')">
                    <a target="_blank" class="btn btn-sm btn-success" data-bind="attr: {href: '<c:url value="/pdf/report.pdf?id="/>' + id}"><spring:message code="pdf.report"/></a>
                    <a target="_blank" class="btn btn-sm btn-warning" data-bind="attr: {href: '<c:url value="/pdf/transport.pdf?id="/>' + id}"><spring:message code="pdf.transport"/></a>
                    <a target="_blank" class="btn btn-sm btn-warning" data-bind="attr: {href: '<c:url value="/pdf/patients.pdf?id="/>' + id}"><spring:message code="pdf.patients"/></a>
                  </sec:authorize>
                  <sec:authorize access="@auth.hasAccessLevel('CloseConcern')">
                    <button type="button" class="btn btn-sm btn-danger" data-bind="click: reopen"><spring:message code="concern.reopen"/></button>
                  </sec:authorize>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </sec:authorize>

      <div class="page-header"></div>
    </div>
  </body>
</html>
