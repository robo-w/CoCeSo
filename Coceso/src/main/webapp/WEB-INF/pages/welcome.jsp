<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<html>
<head>
    <title><spring:message code="label.coceso"/> - <spring:message code="label.nav.home"/></title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Bootstrap -->
    <c:url var="bootstrap" value="/static/css/bootstrap.css" />
    <link href="${bootstrap}" rel="stylesheet">

    <c:url var="bootstrap_theme" value="/static/css/bootstrap-theme.css" />
    <link href="${bootstrap_theme}" rel="stylesheet">

</head>
<body>

<div class="container">

    <c:set value="active" var="nav_home" />
    <%@include file="parts/navbar.jsp"%>


    <div class="alert alert-success">
            <strong><spring:message code="label.operator"/>:</strong> ${user.given_name} ${user.sur_name} (${user.username})<br>
            Roles: <c:forEach items="${user.authorities}" var="role">${role} - </c:forEach>
    </div>
    <div class="page-header">
        <h2>
            <spring:message code="label.concerns"/>
        </h2>
    </div>
    <div>
        <form action="${pageContext.request.contextPath}/welcome" method="post" role="form">
            <div class="row">
                <div class="col-lg-2">
                    &nbsp;
                </div>
                <div class="col-lg-5">
                    <select name="case_id" size="10" class="form-control">
                        <c:forEach var="caze" items="${concern_list}">
                            <option value="${caze.id}">${caze.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-2">
                    &nbsp;
                </div>
                <div class="col-lg-2 text-center">
                    <input type="submit" value="<spring:message code="label.start"/>" name="start" class="btn btn-success">
                </div>
                <div class="col-lg-2 text-center">
                    <input type="submit" value="<spring:message code="label.edit"/>" name="edit" class="btn btn-warning">
                </div>
            </div>

        </form>

    </div>
    <div class="page-header">
        <h2>
            <spring:message code="label.concern.create"/>
        </h2>
    </div>
    <div>
        <form action="${pageContext.request.contextPath}/edit/create" method="post" role="form">
            <div class="row">
                <div class="col-lg-2">
                    &nbsp;
                </div>
                <div class="col-lg-5">
                    <label class="sr-only" for="new_name"><spring:message code="label.concern.name"/></label>
                    <input type="text" id="new_name" name="name" maxlength="64" class="form-control" placeholder="<spring:message code="label.concern.name"/>"/>
                </div>
                <!--div>
                    <Info of Organiser: <input type="text" name="info" maxlength="64" /><br>>
                </div-->
                <div class="col-lg-3">
                    <input type="submit" value="<spring:message code="label.create"/>" class="btn btn-success"/>
                </div>
                <div class="col-lg-2">
                    &nbsp;
                </div>
            </div>


        </form>
    </div>
    <!-- TODO REMOVE IN RELEASE!!! ###################-->
    <div class="page-header">
        <h2>
            Debugging
        </h2>
    </div>
    <div class="alert alert-danger text-center">
        <strong>Only for Developing!</strong><br />
        You have to set the Cookie first (Open once Edit-Page of Case)
    </div>
    <div>
        <c:url var="debug0" value="/data/incident/getAll" />
        <a href="${debug0}" class="active btn btn-primary">All Incidents</a>

        <c:url var="debug1" value="/data/incident/getAllActive" />
        <a href="${debug1}" class="active btn btn-primary">All Active Incidents</a>

        <c:url var="debug2" value="/data/unit/getAll" />
        <a href="${debug2}" class="active btn btn-primary">All Units</a>

        <c:url var="debug3" value="/data/log/getAll" />
        <a href="${debug3}" class="active btn btn-primary">Full Log</a>


    </div>
    <!--##############-->
</div>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<c:url var="jquery" value="/static/js/jquery.js" />
<script src="${jquery}"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<c:url var="bootstrap_js" value="/static/js/bootstrap.js" />
<script src="${bootstrap_js}"></script>


</body>
</html>