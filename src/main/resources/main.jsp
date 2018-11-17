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

<table align="center">
    <form:form modelAttribute="player" method="POST" action="/new-game">
        <tr>
            <td><label>Игрок:</label></td>
            <td>${player.getLogin()}</td>
        </tr>
        <tr>
            <td><label>Рейтинг:</label></td>
            <td>${player.getRating()}</td>
        </tr>
        <tr>
            <td><input type="submit" value="Новая игра"></td>
        </tr>
    </form:form>
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
