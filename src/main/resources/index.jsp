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
    <tr>
        <td>
            <form:form modelAttribute="player" method="POST" action="/goto-login">
                <input type="submit" value="Вход">
            </form:form>
        </td>
        <td>
            <form:form modelAttribute="player" method="POST" action="/goto-register">
                <input type="submit" value="Регистрация">
            </form:form>
        </td>
    </tr>
</table>

<!--/span-->

</body>
</html>
