<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<head>
    <title>Шахматы</title>
</head>
<body>

<h2 align="center">Лучшие игроки</h2>

<table align="center">
    <tr>
        <td align="center" style="font-weight: bold">Игрок</td>
        <td align="center" style="font-weight: bold">Рейтинг</td>
        <td align="center" style="font-weight: bold">Победы</td>
        <td align="center" style="font-weight: bold">Ничьи</td>
        <td align="center" style="font-weight: bold">Поражения</td>
    </tr>
    <c:forEach var="player" items="${top}">
        <tr>
            <td align="center">${player.getLogin()}</td>
            <td align="center">${player.getRating()}</td>
            <td align="center">${player.getWins()}</td>
            <td align="center">${player.getDraws()}</td>
            <td align="center">${player.getLoses()}</td>
        </tr>
    </c:forEach>
</table>

<table align="center">
    <tr>
        <td>
            <form:form modelAttribute="player" method="GET" action="/main">
                <input type="submit" value="На главную">
            </form:form>
        </td>
    </tr>
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
