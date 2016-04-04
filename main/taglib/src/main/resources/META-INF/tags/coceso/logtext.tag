<%@tag body-content="empty"%>
<%@attribute name="log" required="true" rtexprvalue="true" type="at.wrk.coceso.entity.LogEntry"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@tag trimDirectiveWhitespaces="true"%>
<%--
/**
 * CoCeSo
 * Client HTML log text tag
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

<c:choose>
  <c:when test="${log.auto}"><spring:message code="log.type.${log.type}" text="${log.text}"/></c:when>
  <c:otherwise><c:out value="${log.text}"/></c:otherwise>
</c:choose>
