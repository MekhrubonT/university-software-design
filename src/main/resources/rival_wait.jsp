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
    <form:form modelAttribute="player" method="POST" action="/rival_wait">
        <tr>
            <td align = center>
                <h3 align="center">Ожидание соперника</h3>
            </td>
        </tr>
        <tr>
            <td align = center>
                <img style="height: 50px" src="<c:url value="img/loading.gif"/>"/>
            </td>
        </tr>
        <tr>
            <td align = center><input type="submit" value="Обновить"></td>
        </tr>
    </form:form>
        <tr>
            <td align = center>
                <form:form modelAttribute="player" method="POST" action="/logout">
                    <input type="submit" value="Выйти">
                </form:form>
            </td>
        </tr>
    </table>


<!--/span-->

</body>
</html>
