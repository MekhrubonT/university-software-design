<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<head>
    <title>Игра</title>
</head>
<body>

<h1 align="center">Шахматная доска</h1>

<form:form modelAttribute="list">
    <table align="center" style="border-spacing: 0px">
        <c:forEach var="i" begin="0" end="7">
            <tr style="border-spacing: 0px">
                <c:forEach var="j" begin="0" end="7">
                    <td style="padding: 0px;">
                        <img style="height: 50px"
                        <c:choose>
                        <c:when test="${(i+j) % 2 == 1}">
                            src="<c:url value="img/empty_white.png"/>"
                        </c:when>
                        <c:otherwise>
                            src="<c:url value="img/empty_black.png"/>"</c:otherwise>
                        </c:choose>/>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>
</form:form>

<table align="center">
    <tr>
        <td>
            <form:form modelAttribute="player" method="POST" action="/logout">
                <input type="submit" value="Выйти">
            </form:form>
        </td>
    </tr>
</table>
<!--/span-->

</body>
</html>
