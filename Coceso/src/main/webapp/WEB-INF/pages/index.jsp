<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>

<html>
  <head>
    <title><spring:message code="label.coceso"/></title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta charset="utf-8" />

    <link rel="icon" href="<c:url value="/static/favicon.ico"/>" type="image/x-icon" />
    <link href="<c:url value="/static/css/coceso.css"/>" rel="stylesheet" />
  </head>
  <body>
    <div class="container">
      <div class="page-header">
        <h1><spring:message code="label.welcome"/></h1>
      </div>
      <div>
        <spring:message code="text.welcome"/>
      </div>

      <div class="page-header">
        <h3><spring:message code="label.getting_started" /></h3>
      </div>
      <div>
        <spring:message code="text.getting_started" />
      </div>

      <div class="page-header">
        <h3><spring:message code="label.nav.main"/></h3>
      </div>
      <div>
        <a href="<c:url value="/welcome" />" class="btn-lg btn-success"><spring:message code="label.nav.login"/></a>
      </div>

      <div class="page-header">
        <h3><spring:message code="label.main.license" /></h3>
      </div>
      <div>
        <a href="<c:url value="/static/license.html"/>" target="_blank"><spring:message code="text.license" /></a>
      </div>
    </div>
  </body>
</html>
