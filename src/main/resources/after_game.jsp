<%@ page import="model.Figure" %>
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

<form:form modelAttribute="table">
    <table align="center" style="border-spacing: 0px">
        <c:forEach var="i" begin="0" end="7">
            <tr style="border-spacing: 0px">
                <c:forEach var="j" begin="0" end="7">
                    <td style="padding: 0px">

                        <c:choose>
                            <c:when test="${table.getFigure(i, j) == null}">
                                <img style="height: 50px" src="
                        <c:choose><c:when test="${(i+j) % 2 == 0}"><c:url value="img/empty_white.png"/></c:when>
                        <c:otherwise><c:url value="img/empty_black.png"/></c:otherwise></c:choose>"
                                />
                            </c:when>
                            <c:otherwise>
                                <img style="height: 50px" src="
                        <c:choose><c:when test="${(i+j) % 2 == 0}"><c:url value="img/${table.getFigure(i, j).colorToString()}_${table.getFigure(i, j).toString()}_white.png"/></c:when>
                        <c:otherwise><c:url value="img/${table.getFigure(i, j).colorToString()}_${table.getFigure(i, j).toString()}_black.png"/></c:otherwise></c:choose>"
                                />
                            </c:otherwise>
                        </c:choose>

                    </td>
                </c:forEach>
                <td>
                        ${1+i}
                </td>
            </tr>
        </c:forEach>
        <tr>
            <% for (char i = 'h'; i >= 'a'; i--) {%>
            <td style="text-align: center">
                <%= i %>
            </td>
            <% } %>
            <td></td>
        </tr>
    </table>
</form:form>

<table align="center">
    <form:form modelAttribute="result">
        <tr >
            <td style="text-align: center">
                <label style="display: block; text-align: center" >${result}</label>
            </td>
        </tr>
    </form:form>
    <tr>
        <td style="text-align: center">
            <form:form modelAttribute="player" method="POST" action="/goto_main">
                <input type="submit" value="На главную">
            </form:form>
            <form:form modelAttribute="player" method="POST" action="/logout">
                <input type="submit" value="Выйти">
            </form:form>
        </td>
    </tr>
</table>
<!--/span-->

</body>
</html>
