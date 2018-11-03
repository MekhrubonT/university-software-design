<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<head>
    <title>Регистрация</title>
</head>
<body>

<form:form modelAttribute="player" method="POST" action="/register">
    <table align="center">
        <tr>
            <td><form:label path="login">Логин:</form:label></td>
            <td><form:input path="login"/></td>
        </tr>
        <tr>
            <td><form:label path="password">Пароль:</form:label></td>
            <td><form:input path="password"/></td>
        </tr>
        <tr>
            <td><input type="submit" value="Зарегистрироваться"></td>
        </tr>
    </table>
    <label style="display: block; text-align: center<c:if test="${player.getLogin() != \"\"}">; visibility: hidden</c:if>" >Такой пользователь существует</label>
</form:form>





<!--/span-->

</body>
</html>
