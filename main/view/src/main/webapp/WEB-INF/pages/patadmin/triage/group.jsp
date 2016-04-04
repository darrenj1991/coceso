<!DOCTYPE html>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="coceso" prefix="t"%>
<%@taglib uri="patadmin" prefix="p"%>
<%--
/**
 * CoCeSo
 * Patadmin HTML triage group details
 * Copyright (c) WRK\Coceso-Team
 *
 * Licensed under the GNU General Public License, version 3 (GPL-3.0)
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright Copyright (c) 2015 WRK\Coceso-Team
 * @link https://sourceforge.net/projects/coceso/
 * @license GPL-3.0 ( http://opensource.org/licenses/GPL-3.0 )
 */
--%>
<html>
  <head>
    <t:head maintitle="patadmin" title="patadmin.group" entry="navbar"/>
  </head>
  <body>
    <div class="container">
      <%@include file="navbar.jsp"%>

      <h2><spring:message code="patadmin.group"/>: <em><c:out value="${group.call}"/></em></h2>
      <dl class="dl-horizontal groups">
        <dt><spring:message code="patadmin.group.name"/></dt>
        <dd class="group_icon">
          <c:if test="${not empty group.imgsrc}">
            <img alt="" src="<c:url value="/static/imgs/groups/${group.imgsrc}"/>"/>
          </c:if>
          <c:out value="${group.call}"/>
        </dd>
        <dt><spring:message code="patadmin.group.occupation"/></dt>
        <dd>
          <c:set value="${group.state == 'EB' ? group.capacity : 0}" var="capacity"/>
          <c:out value="${group.patients}"/> <spring:message code="patadmin.group.of"/> <c:out value="${group.capacity}"/><br/>
          <c:if test="${group.patients > 0}">
            <c:forEach begin="1" end="${Math.min(group.patients, capacity.intValue())}"><img src="<c:url value="/static/imgs/capacity/occupied.png"/>" alt="X"/></c:forEach>
          </c:if>
          <c:if test="${group.patients > capacity}">
            <c:forEach begin="1" end="${group.patients-capacity}"><img src="<c:url value="/static/imgs/capacity/closed.png"/>" alt="X"/></c:forEach>>
          </c:if>
          <c:if test="${group.patients < capacity}">
            <c:forEach begin="1" end="${capacity-group.patients}"><img src="<c:url value="/static/imgs/capacity/free.png"/>" alt="0"/></c:forEach>
          </c:if>
        </dd>
        <dt><spring:message code="patadmin.group.active"/></dt>
        <dd>
          <spring:message code="${group.state == 'EB' ? 'yes' : 'no'}"/>
        </dd>
      </dl>

      <p>
        <a class="btn btn-default" href="<c:url value="/patadmin/triage/add?group=${group.id}"/>">
          <spring:message code="patient.add"/>
        </a>
      </p>

      <c:if test="${not empty incoming}">
        <div>
          <h3><spring:message code="patadmin.incoming"/></h3>
          <p:incoming incidents="${incoming}" hideGroup="true"/>
        </div>
      </c:if>

      <c:if test="${not empty treatment}">
        <div>
          <h3><spring:message code="patadmin.intreatment"/></h3>
          <p:patients patients="${treatment}" hideGroup="true"/>
        </div>
      </c:if>
    </div>
  </body>
</html>
