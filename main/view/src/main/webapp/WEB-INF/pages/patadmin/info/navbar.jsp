<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%--
/**
 * CoCeSo
 * Patadmin HTML navbar element
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
<div class="navbar navbar-default" role="navigation">
  <div class="navbar-header">
    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
      <span class="sr-only">Toggle navigation</span>
      <span class="icon-bar"></span>
      <span class="icon-bar"></span>
      <span class="icon-bar"></span>
    </button>
    <a class="navbar-brand" href="<c:url value="/patadmin"/>"><spring:message code="patadmin"/></a>
  </div>
  <div class="navbar-collapse collapse">
    <form action="<c:url value="/patadmin/info/search"/>" class="navbar-form navbar-right" method="get">
      <div class="form-group"><input type="text" name="q" class="form-control" placeholder="<spring:message code="patadmin.search"/>" accesskey="s" id="search" value="<c:out value="${search}"/>"/></div>
    </form>
    <ul class="nav navbar-nav">
      <li class="active"><a href="<c:url value="/patadmin/info"/>"><spring:message code="patadmin.info"/></a></li>
        <c:if test="${accessLevels[0]}">
        <li class="${nav_settings}"><a href="<c:url value="/patadmin/settings"/>"><spring:message code="patadmin.settings"/></a></li>
        </c:if>
        <c:if test="${accessLevels[1]}">
        <li><a href="<c:url value="/patadmin/triage"/>"><spring:message code="patadmin.triage"/></a></li>
        </c:if>
        <c:if test="${accessLevels[2]}">
        <li><a href="<c:url value="/patadmin/postprocessing"/>"><spring:message code="patadmin.postprocessing"/></a></li>
        </c:if>
      <li><a href="<c:url value="/home"/>"><spring:message code="exit"/></a></li>
    </ul>
  </div>
</div>
